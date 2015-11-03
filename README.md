# maven-jsp-plugin
Compile .jsp file.

Maven usage example:

            <plugin>
                <groupId>org.jboss.maven.plugins</groupId>
                <artifactId>jsp-maven-plugin</artifactId>
                <version>${version.org.jboss.maven.jsp.plugin}</version>
                <executions>
                    <execution>
                        <id>jsp-compile</id>
                        <goals>
                            <goal>jsp</goal>
                        </goals>
                        <phase>test-compile</phase>
                    </execution>
                </executions>
                <dependencies>
                    <dependency>
                        <groupId>com.google.appengine</groupId>
                        <artifactId>appengine-api-1.0-sdk</artifactId>
                        <version>${version.com.google.gae.api}</version>
                    </dependency>
                </dependencies>
            </plugin>
