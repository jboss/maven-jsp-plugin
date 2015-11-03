# maven-jsp-plugin
Compile .jsp file.

Maven usage:

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
            </plugin>
