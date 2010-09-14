package org.openspaces.maven.support;

import org.springframework.core.JdkVersion;

import java.io.*;

/**
 * @author kimchy
 */
public class POMGenerator {

    public static final String GS_GROUP = "com.gigaspaces";

    public static void main(String[] args) throws Exception {
        String templDir = System.getProperty("java.io.tmpdir");
        if (args.length > 0) {
            templDir = args[0];
        }
        String version = OutputVersion.computeVersion();
        if (args.length > 1) {
            version = args[1];
        }
        File dir = new File(templDir);
        dir.mkdirs();

        writeSimplePom(dir, version, POMGenerator.GS_GROUP, "gs-openspaces");

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "gs-runtime-pom.xml")))));
        printHeader(writer, version, POMGenerator.GS_GROUP, "gs-runtime");

        // jmx
        if (!JdkVersion.isAtLeastJava15()) {
            printDependency(writer, "com.sun.jdmk", "jmxtools", "1.2.1");
            printDependency(writer, "javax.management", "jmxremote", "1.0.1_04");
            printDependency(writer, "javax.management", "jmxri", "1.2.1");
            printDependency(writer, "backport-util-concurrent", "backport-util-concurrent", "3.0");
        }

        printFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "gs-openspaces-pom.xml")))));
        printHeader(writer, version, POMGenerator.GS_GROUP, "gs-openspaces");
        printDependency(writer, POMGenerator.GS_GROUP, "gs-runtime");
        printDependency(writer, "org.springframework", "spring-aop", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-asm", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-aspects", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-beans", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-context", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-context-support", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-core", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-expression", "3.0.3.RELEASE");
        printDependency(writer, "org.springframework", "spring-tx", "3.0.3.RELEASE");
        printDependency(writer, "commons-logging", "commons-logging", "1.1.1");
        // add javax.annotations (@PostConstruct) for JDK 1.5 (no need for 1.6 since it is there)
        if (!JdkVersion.isAtLeastJava16() && JdkVersion.isAtLeastJava15()) {
            printDependency(writer, "org.apache.geronimo.specs", "geronimo-annotation_1.0_spec", "1.1.1");
        }
        printFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "mule-os-pom.xml")))));
        printHeader(writer, version, POMGenerator.GS_GROUP, "mule-os");
        printFooter(writer);
        writer.close();
        
        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "jetty-os-pom.xml")))));
        printHeader(writer, version, POMGenerator.GS_GROUP, "jetty-os");
        printFooter(writer);
        writer.close();
        
        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "glassfish-os-pom.xml")))));
        printHeader(writer, version, POMGenerator.GS_GROUP, "glassfish-os");
        printFooter(writer);
        writer.close();

        if (args.length > 2) {
            String directory = args[2];
            replaceVersionInPluginPom(version, directory);
        }
    }

    private static void replaceVersionInPluginPom(String version, String dir) throws IOException {
        File f = new File(dir, "pom.xml");
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line;
        String openTag = "<pluginVersion>";
        String closeTag = "</pluginVersion>";
        String newLine = "\n";
        int index = 0;
        boolean firstVersionFound = false;
        while ((line = br.readLine()) != null) {
            if ((index = line.indexOf(openTag)) > 0) {
                sb.append(line.substring(0, index + openTag.length()));
                sb.append(version);
                sb.append(closeTag);
            } else if ((index = line.indexOf("<version>")) > 0 && !firstVersionFound) {
                firstVersionFound = true;
                sb.append(line.substring(0, index + "<version>".length()));
                sb.append(version);
                sb.append("</version>");
            } else {
                sb.append(line);
            }
            sb.append(newLine);
        }
        File f2 = new File(dir, "pom2.xml");
        FileWriter fw = new FileWriter(f2);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(sb.toString());
        br.close();
        bw.close();
        for (int i = 0; i < 5; i++) {
            if (f.delete()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        for (int i = 0; i < 5; i++) {
            if (f2.renameTo(f)) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }


    private static void writeSimplePom(File dir, String version, String groupId, String artifactId) throws Exception {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, artifactId + "-pom.xml")))));
        printHeader(writer, version, groupId, artifactId);
        printFooter(writer);
        writer.close();
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
        writer.println("  <dependencies>");
    }

    public static void printDependency(PrintWriter writer, String groupId, String artifactId) {
        printDependency(writer, groupId, artifactId, OutputVersion.computeVersion());
    }

    public static void printDependency(PrintWriter writer, String groupId, String artifactId, String version) {
        writer.println("    <dependency>");
        writer.println("      <groupId>" + groupId + "</groupId>");
        writer.println("      <artifactId>" + artifactId + "</artifactId>");
        writer.println("      <version>" + version + "</version>");
        writer.println("    </dependency>");
    }

    public static void printFooter(PrintWriter writer) throws Exception {
        writer.println("  </dependencies>");
        writer.println("</project>");
    }
}
