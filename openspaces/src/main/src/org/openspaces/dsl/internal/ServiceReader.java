package org.openspaces.dsl.internal;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        return createGroovyShell(BaseServiceScript.class.getName());
    }
    
    private static GroovyShell createGroovyShellForApplication() {
        return createGroovyShell(BaseApplicationScript.class.getName());
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
    
    

    private static CompilerConfiguration createCompilerConfiguration(String baseClassName) {
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
    public static Application getApplicationFromFile(File inputFile) throws IOException {
        File dslFile = getApplicationDslFile(inputFile);
        Application app = readApplicationFromFile(dslFile);
        File appDir = dslFile.getParentFile();
        List<String> serviceNames = app.getServiceNames();
        List<Service> services = new ArrayList<Service>(serviceNames.size());
        for (String serviceName : serviceNames) {
            Service service = readApplicationService(app, serviceName, appDir);
            services.add(service);
        }
        app.setServices(services);
        
        return app;
        
    }
    private static File getApplicationDslFile(File inputFile) throws FileNotFoundException {
        if(!inputFile.exists()) {
            throw new FileNotFoundException("Could not find file: " + inputFile);
        }
        
        if(inputFile.isFile()) { 
            return inputFile;
        }
        
        if(inputFile.isDirectory()) {
            File[] files = inputFile.listFiles(new FilenameFilter() {                
                public boolean accept(File dir, String name) {
                    return (name.endsWith("-application.groovy"));
                }
            });
            
            if(files.length != 1) {
                throw new IllegalArgumentException("Expected to find one application file, found " + files.length);
            }
            
            return files[0];
        }
        
        throw new IllegalStateException("Could not find File: " + inputFile);
        
        
    }

    private static Service readApplicationService(Application app, String serviceName, File appDir) throws FileNotFoundException {
        File serviceDir = new File(appDir, serviceName);
        if(!serviceDir.exists()) {
            throw new FileNotFoundException("Could not find directory " + serviceDir + " for service " + serviceName);
        }
        
        return getServiceFromFile(serviceDir);
        
    }

    private static Application readApplicationFromFile(File dslFile) throws IOException {
        
        if(!dslFile.exists()) {
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

//        final ServiceContext ctx = new ServiceContext(service, admin, workDir.getAbsolutePath(), clusterInfo);
//        gs.getContext().setProperty("context", ctx);

        return application;

    }
}
