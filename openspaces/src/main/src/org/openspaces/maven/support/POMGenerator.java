package org.openspaces.maven.support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * @author kimchy
 */
public class POMGenerator {

    public static void main(String[] args) throws Exception {
        String templDir = System.getProperty("java.io.tmpdir");
        if (args.length > 0) {
            templDir = args[0];
        }
        File dir = new File(templDir);
        dir.mkdirs();

        writeSimplePom(dir, "jini-start");
        writeSimplePom(dir, "jini-jsk-lib");
        writeSimplePom(dir, "jini-jsk-platform");
        writeSimplePom(dir, "jini-jsk-resources");
        writeSimplePom(dir, "jini-reggie");
        writeSimplePom(dir, "jini-mahalo");
        writeSimplePom(dir, "gs-boot");
        writeSimplePom(dir, "gs-service");
        writeSimplePom(dir, "gs-lib");

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "JSpaces-pom.xml")))));
        printHeader(writer, "JSpaces");
        printDependency(writer, "jini-start");
        printDependency(writer, "jini-jsk-lib");
        printDependency(writer, "jini-jsk-platform");
        printDependency(writer, "jini-jsk-resources");
        printDependency(writer, "jini-reggie");
        printDependency(writer, "gs-boot");
        printDependency(writer, "gs-service");
        printDependency(writer, "gs-lib");
        printFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "openspaces-pom.xml")))));
        printHeader(writer, "openspaces");
        printDependency(writer, "JSpaces");
        printDependency(writer, "org.springframework", "spring", "2.0.7");
        printFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "mule-os-pom.xml")))));
        printHeader(writer, "mule-os");
        printDependency(writer, "openspaces");
        printFooter(writer);
        writer.close();
    }

    private static void writeSimplePom(File dir, String artifactId) throws Exception {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, artifactId + "-pom.xml")))));
        printHeader(writer, artifactId);
        printFooter(writer);
        writer.close();
    }

    public static void printHeader(PrintWriter writer, String artifactId) throws Exception {
        writer.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"");
        writer.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.println("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0");
        writer.println("                      http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
        writer.println("  <modelVersion>4.0.0</modelVersion>");
        writer.println("  <groupId>gigaspaces</groupId>");
        writer.println("  <artifactId>" + artifactId + "</artifactId>");
        writer.println("  <packaging>jar</packaging>");
        writer.println("  <version>" + OutputVersion.VERSION + "</version>");
        writer.println("  <url>http://www.gigaspaces.com</url>");
        writer.println("  <dependencies>");
    }

    public static void printDependency(PrintWriter writer, String dependencyId) {
        printDependency(writer, "gigaspaces", dependencyId, OutputVersion.VERSION);
    }

    public static void printDependency(PrintWriter writer, String groupId, String dependencyId, String version) {
        writer.println("    <dependency>");
        writer.println("      <groupId>" + groupId + "</groupId>");
        writer.println("      <artifactId>" + dependencyId + "</artifactId>");
        writer.println("      <version>" + version + "</version>");
        writer.println("    </dependency>");
    }

    public static void printFooter(PrintWriter writer) throws Exception {
        writer.println("  </dependencies>");
        writer.println("</project>");
    }
}
