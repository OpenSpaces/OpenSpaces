/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.maven.support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.springframework.core.JdkVersion;

import com.j_spaces.kernel.PlatformVersion;

/**
 * @author kimchy
 */
public class POMGenerator {

    
	public static final String GS_GROUP = "com.gigaspaces";
    public static final String CLOUDIFY_GROUP = "org.cloudifysource";
    private static final String SPRING_GROUP = "org.springframework";
    public static final String SPRING_VERSION = "3.2.4.RELEASE";
    public static final String SPRING_SECURITY_VERSION = "3.1.4.RELEASE";
	private static final String SPRING_SECURITY_GROUP = "org.springframework.security";
	private static final String COMMONS_COLLECTIONS_VERSION = "3.2.1";
	private static final String COMMONS_LANG_VERSION = "2.6";
	private static final String COMMONS_POOL_VERSION = "1.6";
    
    public static void main(String[] args) throws Exception {
        String templDir = System.getProperty("java.io.tmpdir");
        if (args.length > 0) {
            templDir = args[0];
        }

        String xapVersion = OutputVersion.computeXapVersion();
        String cloudifyVersion = null;
        boolean isCloudify = PlatformVersion.getEdition().equals(PlatformVersion.EDITION_CLOUDIFY);
        if (isCloudify) {
            cloudifyVersion = OutputVersion.computeCloudifyVersion();
        }
        
        File dir = new File(templDir);
        dir.mkdirs();

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "gs-runtime-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "gs-runtime");
        

        // jmx
        if (!JdkVersion.isAtLeastJava15()) {
        	printDependenciesHeader(writer);
            printDependency(writer, "com.sun.jdmk", "jmxtools", "1.2.1");
            printDependency(writer, "javax.management", "jmxremote", "1.0.1_04");
            printDependency(writer, "javax.management", "jmxri", "1.2.1");
            printDependency(writer, "backport-util-concurrent", "backport-util-concurrent", "3.0");
            printDependenciesFooter(writer);
        }
        printProjectFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "gs-openspaces-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "gs-openspaces");
        printDependenciesHeader(writer);
        printDependency(writer, POMGenerator.GS_GROUP, "gs-runtime", xapVersion);
        printDependency(writer, SPRING_GROUP, "spring-aop");
        printDependency(writer, SPRING_GROUP, "spring-aspects");
        printDependency(writer, SPRING_GROUP, "spring-beans");
        printDependency(writer, SPRING_GROUP, "spring-context");
        printDependency(writer, SPRING_GROUP, "spring-context-support");
        printDependency(writer, SPRING_GROUP, "spring-core");
        printDependency(writer, SPRING_GROUP, "spring-expression");
        printDependency(writer, SPRING_GROUP, "spring-tx");
        printDependency(writer, "commons-logging", "commons-logging");
        // add javax.annotations (@PostConstruct) for JDK 1.5 (no need for 1.6 since it is there)
        if (!JdkVersion.isAtLeastJava16() && JdkVersion.isAtLeastJava15()) {
            printDependency(writer, "org.apache.geronimo.specs", "geronimo-annotation_1.0_spec", "1.1.1");
        }
        printDependenciesFooter(writer);
        
        printDependencyManagementHeader(writer);
        printDependenciesHeader(writer);
        // spring jars in lib/required
        printProvidedDependency(writer, SPRING_GROUP, "spring-aop", SPRING_VERSION);
        printProvidedDependency(writer, SPRING_GROUP, "spring-aspects", SPRING_VERSION);
        printProvidedDependency(writer, SPRING_GROUP, "spring-beans", SPRING_VERSION);
        printProvidedDependency(writer, SPRING_GROUP, "spring-context", SPRING_VERSION);
        printProvidedDependency(writer, SPRING_GROUP, "spring-context-support", SPRING_VERSION);
        printProvidedDependency(writer, SPRING_GROUP, "spring-core", SPRING_VERSION);
        printProvidedDependency(writer, SPRING_GROUP, "spring-expression", SPRING_VERSION);
        printProvidedDependency(writer, SPRING_GROUP, "spring-tx", SPRING_VERSION);
        printProvidedDependency(writer, "commons-logging", "commons-logging", "1.1.3");
        
        // ignore deprecated spring-asm that is needed by older spring-security
        printProvidedDependency(writer, SPRING_GROUP, "spring-asm", "3.0.6");
        
        // commons in lib/platform/commons
        printDependency(writer, SPRING_GROUP, "commons-collections", "commons-collections", COMMONS_COLLECTIONS_VERSION);
        printDependency(writer, SPRING_GROUP, "commons-lang", "commons-lang", COMMONS_LANG_VERSION);
        printDependency(writer, SPRING_GROUP, "commons-pool", "commons-pool", COMMONS_POOL_VERSION);
	
        // spring jars in lib/optional/spring
        printDependency(writer, SPRING_GROUP, "spring-web", SPRING_VERSION);
        printDependency(writer, SPRING_GROUP, "spring-jdbc", SPRING_VERSION);
        printDependency(writer, SPRING_GROUP, "spring-jms", SPRING_VERSION);
        printDependency(writer, SPRING_GROUP, "spring-orm", SPRING_VERSION);
        printDependency(writer, SPRING_GROUP, "spring-oxm", SPRING_VERSION);
        printDependency(writer, SPRING_GROUP, "spring-web", SPRING_VERSION);
        printDependency(writer, SPRING_GROUP, "spring-webmvc", SPRING_VERSION);
		printTestDependency(writer, SPRING_GROUP, "spring-test", SPRING_VERSION);
		printDependency(writer, SPRING_SECURITY_GROUP, "spring-security-core", SPRING_SECURITY_VERSION);
		printDependency(writer, SPRING_SECURITY_GROUP, "spring-security-web", SPRING_SECURITY_VERSION);
		printDependency(writer, SPRING_SECURITY_GROUP, "spring-security-config", SPRING_SECURITY_VERSION);
		
		printDependenciesFooter(writer);
        printDependencyManagementFooter(writer);
        
        printProjectFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "mule-os-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "mule-os");
        printProjectFooter(writer);
        writer.close();
        
        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "jetty-os-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "jetty-os");
        printProjectFooter(writer);
        writer.close();
        
        if ( isCloudify ) {
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "dsl-pom.xml")))));
            printHeader(writer, cloudifyVersion, POMGenerator.CLOUDIFY_GROUP, "dsl");
            printDependenciesHeader(writer);
            printDependency(writer, POMGenerator.GS_GROUP, "gs-openspaces", xapVersion);
            printDependenciesFooter(writer);           
            printProjectFooter(writer);
            writer.close();
            
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "usm-pom.xml")))));
            printHeader(writer, cloudifyVersion, POMGenerator.CLOUDIFY_GROUP, "usm");
            printDependenciesHeader(writer);
            printDependency(writer, POMGenerator.GS_GROUP, "gs-openspaces", xapVersion);
            printDependency(writer, POMGenerator.CLOUDIFY_GROUP, "dsl", cloudifyVersion);
            printDependenciesFooter(writer);
            printProjectFooter(writer);
            writer.close();
            
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "esc-pom.xml")))));
            printHeader(writer, cloudifyVersion, POMGenerator.CLOUDIFY_GROUP, "esc");
            printDependenciesHeader(writer);
            printDependency(writer, POMGenerator.GS_GROUP, "gs-openspaces", xapVersion);
            printDependenciesFooter(writer);
            printProjectFooter(writer);
            writer.close();
        }
        
    }


    public static void printHeader(PrintWriter writer, String version, String groupId, String artifactId) throws Exception {
        writer.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"");
        writer.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.println("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0");
        writer.println("                      http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
        writer.println("  <modelVersion>4.0.0</modelVersion>");
        writer.println("  <groupId>" + groupId + "</groupId>");
        writer.println("  <artifactId>" + artifactId + "</artifactId>");
        writer.println("  <packaging>jar</packaging>");
        writer.println("  <version>" + version + "</version>");
        writer.println("  <url>http://www.gigaspaces.com</url>");
    }
    
    public static void printDependenciesHeader(PrintWriter writer) throws Exception {
        writer.println("  <dependencies>");
    }
    
    public static void printDependencyManagementHeader(PrintWriter writer) throws Exception {
        writer.println("  <dependencyManagement>");
    }
    
    public static void printDependencyManagementFooter(PrintWriter writer) throws Exception {
        writer.println("  </dependencyManagement>");
    }
    
    public static void printDependency(PrintWriter writer, String groupId, String artifactId) {
    	final String version = null;
    	printDependency(writer, groupId, artifactId, version);
    }
    
    public static void printDependency(PrintWriter writer, String groupId, String artifactId, String version) {
    	final String scope = null;
    	printDependency(writer, groupId, artifactId, version, scope);
    }
    
    public static void printTestDependency(PrintWriter writer, String groupId, String artifactId, String version) {
    	final String scope = "test";
    	printDependency(writer, groupId, artifactId, version, scope);
    }

    public static void printProvidedDependency(PrintWriter writer, String groupId, String artifactId, String version) {
    	final String scope = "provided";
    	printDependency(writer, groupId, artifactId, version, scope);
    }
    
    public static void printDependency(PrintWriter writer, String groupId, String artifactId, String version, String scope) {
        writer.println("    <dependency>");
        writer.println("      <groupId>" + groupId + "</groupId>");
        writer.println("      <artifactId>" + artifactId + "</artifactId>");
        if (version != null) {
        	writer.println("      <version>" + version + "</version>");
        }
        if (scope != null) {
        	writer.println("      <scope>" + scope + "</scope>");
        }
        writer.println("    </dependency>");
    }

    public static void printDependenciesFooter(PrintWriter writer) throws Exception {
        writer.println("  </dependencies>");
    }
    
    public static void printProjectFooter(PrintWriter writer) throws Exception {
        writer.println("</project>");
    }
}
