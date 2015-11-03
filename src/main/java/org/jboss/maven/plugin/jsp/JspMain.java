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

/**
 * Testing purpose.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JspMain extends JspMojo {
    public static void main(String[] args) {
        try {
            new JspMain(args[0], args[1]).execute();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private final String jspLocation;
    private final String tempDir;

    private JspMain(String jsps, String tempDir) {
        this.jspLocation = jsps;
        this.tempDir = tempDir;
    }

    @Override
    protected File getJspLocation() {
        return new File(jspLocation);
    }

    @Override
    protected File getTempDir() {
        return new File(tempDir);
    }
}
