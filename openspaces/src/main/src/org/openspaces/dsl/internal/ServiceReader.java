package org.openspaces.dsl.internal;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.openspaces.admin.Admin;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.dsl.Application;
import org.openspaces.dsl.Service;
import org.openspaces.dsl.context.ServiceContext;

public class ServiceReader {

    /*****
     * Private Constructor to prevent instantitaion.
     * 
     */
    private ServiceReader() {

    }

    /****************
     * Reads a service object from a groovy DSL file placed in the given directory. The file name
     * must be of the format *-service.groovy, and there must be exactly one file in the directory
     * with a name that matches this format.
     * 
     * @param dir
     *            the directory to scan for the DSL file.
     * @return the service
     * @throws Exception
     */
    public static Service getServiceFromFile(final File dir) {
        final File[] files = dir.listFiles(new FilenameFilter() {

            public boolean accept(final File dir, final String name) {
                return name.endsWith("-service.groovy");
            }
        });

        if (files.length > 1) {
            throw new IllegalArgumentException("Found multiple service configuration files: "
                    + Arrays.toString(files) + ". " +
                            "Only one may be supplied in the ext folder of the PU Jar file.");
        }

        if (files.length == 0) {
            return null;
        }
        return ServiceReader.getServiceFromFile(files[0], dir);

    }

    public static Service getServiceFromFile(final File dslFile, final File workDir) {
        return ServiceReader.getServiceFromFile(dslFile, workDir, null, null);
    }

    // TODO - consider adding a DSL exception
    public static Service getServiceFromFile(final File dslFile, final File workDir, final Admin admin,
            final ClusterInfo clusterInfo) {

        final GroovyShell gs = ServiceReader.createGroovyShellForService();

        Object result = null;
        try {
            result = gs.evaluate(dslFile);
        } catch (final CompilationFailedException e) {
            throw new IllegalArgumentException("The file " + dslFile +
                    " could not be compiled", e);
        } catch (final IOException e) {
            throw new IllegalStateException("The file " + dslFile + " could not be read",
                    e);
        }

        // final Object result = Eval.me(expr);
        if (result == null) {
            throw new IllegalStateException("The file " + dslFile + " evaluates to null, not to a service object");
        }
        if (!(result instanceof Service)) {
            throw new IllegalStateException("The file: " + dslFile + " did not evaluate to the required object type");
        }

        final Service service = (Service) result;

        final ServiceContext ctx = new ServiceContext(service, admin, workDir.getAbsolutePath(), clusterInfo);
        gs.getContext().setProperty("context", ctx);

        return service;
    }

    private static GroovyShell createGroovyShellForService() {
        return ServiceReader.createGroovyShell(BaseServiceScript.class.getName());
    }

    private static GroovyShell createGroovyShellForApplication() {
        return ServiceReader.createGroovyShell(BaseApplicationScript.class.getName());
    }

    private static GroovyShell createGroovyShell(final String baseClassName) {
        final CompilerConfiguration cc = ServiceReader.createCompilerConfiguration(baseClassName);
        final Binding binding = new Binding();

        final GroovyShell gs = new GroovyShell(
                ServiceReader.class.getClassLoader(), // this.getClass().getClassLoader(),
                binding,
                cc);
        return gs;
    }

    private static CompilerConfiguration createCompilerConfiguration(final String baseClassName) {
        final CompilerConfiguration cc = new CompilerConfiguration();
        final ImportCustomizer ic = new ImportCustomizer();
        ic.addStarImports("org.openspaces.dsl", "org.openspaces.dsl.ui", "org.openspaces.dsl.context");
        ic.addImports("org.openspaces.dsl.ui.BarLineChart.Unit");
        // ic.addStaticStars(USMUtils.class.getName());
        // ic.addImports(Unit.class.getName());
        cc.addCompilationCustomizers(ic);

        cc.setScriptBaseClass(baseClassName);

        return cc;
    }

    // TODO - Support Zip files in application
    public static Application getApplicationFromFile(final File inputFile) throws IOException {
    
        File actualApplicationDslFile = inputFile;

        if (inputFile.isFile()) {
            if (inputFile.getName().endsWith(".zip") || inputFile.getName().endsWith(".jar")) {
                    actualApplicationDslFile = ServiceReader.unzipApplicationFile(inputFile);
            }
        }
        final File dslFile = ServiceReader.getApplicationDslFile(actualApplicationDslFile);

        final Application app = ServiceReader.readApplicationFromFile(dslFile);
        final File appDir = dslFile.getParentFile();
        final List<String> serviceNames = app.getServiceNames();
        final List<Service> services = new ArrayList<Service>(serviceNames.size());
        for (final String serviceName : serviceNames) {
            final Service service = ServiceReader.readApplicationService(app, serviceName, appDir);
            services.add(service);
        }
        app.setServices(services);

        return app;

    }

    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(ServiceReader.class.getName());

    private static File unzipApplicationFile(final File inputFile) throws IOException {

        ZipFile zipFile = null;
        try {
            final File baseDir = ServiceReader.createTempDir();
            zipFile = new ZipFile(inputFile);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) {

                    logger.info("Extracting directory: " + entry.getName());
                    final File dir = new File(baseDir, entry.getName());
                    dir.mkdir();
                    continue;
                }

                logger.info("Extracting file: " + entry.getName());
                final File file = new File(baseDir, entry.getName());
                file.getParentFile().mkdirs();
                ServiceReader.copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(file)));
            }
            return ServiceReader.getApplicationDSLFileFromDirectory(baseDir);

        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (final IOException e) {
                    logger.log(Level.SEVERE, "Failed to close zip file after unzipping zip contents", e);
                }
            }
        }

    }

    public static final void copyInputStream(final InputStream in, final OutputStream out)
            throws IOException {
        final byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    protected static File createTempDir() throws IOException {
        final File tempFile = File.createTempFile("GS_tmp_dir", ".application");
        final String path = tempFile.getAbsolutePath();
        tempFile.delete();
        tempFile.mkdirs();
        final File baseDir = new File(path);
        return baseDir;
    }

    private static File getApplicationDslFile(final File inputFile) throws FileNotFoundException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Could not find file: " + inputFile);
        }

        if (inputFile.isFile()) {
            if (inputFile.getName().endsWith("-application.groovy")) {
                return inputFile;
            }
            if (inputFile.getName().endsWith(".zip") || inputFile.getName().endsWith(".jar")) {
                return ServiceReader.getApplicationDslFileFromZip(inputFile);
            }
        }

        if (inputFile.isDirectory()) {
            return ServiceReader.getApplicationDSLFileFromDirectory(inputFile);
        }

        throw new IllegalStateException("Could not find File: " + inputFile);

    }

    protected static File getApplicationDSLFileFromDirectory(final File dir) {
        final File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return (name.endsWith("-application.groovy"));
            }
        });

        if (files.length != 1) {
            throw new IllegalArgumentException("Expected to find one application file, found " + files.length);
        }

        return files[0];
    }

    /********
     * Given an a
     * 
     * @param inputFile
     * @return
     */
    private static File getApplicationDslFileFromZip(final File inputFile) {
        // TODO Auto-generated method stub
        return null;
    }

    private static Service readApplicationService(final Application app, final String serviceName, final File appDir)
            throws FileNotFoundException {
        final File serviceDir = new File(appDir, serviceName);
        if (!serviceDir.exists()) {
            throw new FileNotFoundException("Could not find directory " + serviceDir + " for service " + serviceName);
        }

        return ServiceReader.getServiceFromFile(serviceDir);

    }

    private static Application readApplicationFromFile(final File dslFile) throws IOException {

        if (!dslFile.exists()) {
            throw new FileNotFoundException(dslFile.getAbsolutePath());
        }
        final GroovyShell gs = ServiceReader.createGroovyShellForApplication();

        Object result = null;
        try {
            result = gs.evaluate(dslFile);
        } catch (final CompilationFailedException e) {
            throw new IllegalArgumentException("The file " + dslFile +
                    " could not be compiled", e);
        } catch (final IOException e) {
            throw new IllegalStateException("The file " + dslFile + " could not be read",
                    e);
        }

        // final Object result = Eval.me(expr);
        if (result == null) {
            throw new IllegalStateException("The file: " + dslFile + " evaluates to null, not to an application object");
        }
        if (!(result instanceof Application)) {
            throw new IllegalStateException("The file: " + dslFile + " did not evaluate to the required object type");
        }

        final Application application = (Application) result;

        // final ServiceContext ctx = new ServiceContext(service, admin, workDir.getAbsolutePath(),
        // clusterInfo);
        // gs.getContext().setProperty("context", ctx);

        return application;

    }
}
