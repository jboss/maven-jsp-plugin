/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.maven.plugin.jsp;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.ServletStackTraces;
import io.undertow.servlet.util.DefaultClassIntrospector;
import io.undertow.testutils.DefaultServer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;

/**
 * This plugin compiles .jsp files.
 *
 * It uses UnderTow's DefaultServer JUnit runner,
 * and own Mojo class as a test class, to execute compile method.
 * It then only touches .jsp pages with ?jsp_precompile,
 * hence not executing the .jsp page, but only pre-compiling it.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @goal jsp
 * @phase test-compile
 * @requiresDependencyResolution
 */
public class JspMojo extends AbstractMojo {
    /**
     * The Maven Project Object
     *
     * @parameter property="project"
     * @required
     */
    protected MavenProject project;

    /**
     * Tests.
     *
     * @parameter
     */
    protected boolean tests = true;

    private static ThreadLocal<JspMojo> TL = new ThreadLocal<>();

    protected File getJspLocation() {
        Build build = project.getBuild();

        final List<Resource> jsps = (tests ? build.getTestResources() : build.getResources());
        if (jsps == null || jsps.isEmpty()) {
            throw new IllegalStateException(String.format("No resources defined, build: %s", build));
        }

        return new File(jsps.get(0).getDirectory());
    }

    protected File getTempDir() {
        Build build = project.getBuild();
        return (new File(tests ? build.getTestOutputDirectory() : build.getOutputDirectory()));
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        final RunNotifier notifier = new RunNotifier();
        try {
            DefaultServer server = new DefaultServer(JspMojo.class);
            TL.set(this);
            try {
                server.run(notifier);
            } finally {
                TL.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Cannot compile JSP files.", e);
        } finally {
            notifier.fireTestRunFinished(new Result());
        }
    }

    @Test
    public void compile() throws Exception {
        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();

        JspMojo mojo = TL.get();

        final File root = mojo.getJspLocation();

        getLog().info(String.format("JSP location: %s", root));

        final FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jsp");
            }
        };

        ServletInfo servlet = JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp");
        servlet.addInitParam("mappedfile", Boolean.TRUE.toString());

        DeploymentInfo builder = new DeploymentInfo()
            .setClassLoader(JspMojo.class.getClassLoader())
            .setContextPath("/tck")
            .setClassIntrospecter(DefaultClassIntrospector.INSTANCE)
            .setDeploymentName("tck.war")
            .setResourceManager(new FileResourceManager(root, Integer.MAX_VALUE))
            .setTempDir(mojo.getTempDir())
            .setServletStackTraces(ServletStackTraces.NONE)
            .addServlet(servlet);
        JspServletBuilder.setupDeployment(builder, new HashMap<String, JspPropertyGroup>(), new HashMap<String, TagLibraryInfo>(), new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (File jsp : root.listFiles(filter)) {
                touchJsp(client, jsp.getName());
            }
        }
    }

    protected void touchJsp(HttpClient client, String name) {
        getLog().info(String.format("Touching %s file.", name));
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/tck/" + name + "?jsp_precompile");
            client.execute(get); // just touch, so it compiles .jsp, ignore any error
        } catch (Throwable ignored) {
        }
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setTests(boolean tests) {
        this.tests = tests;
    }
}
