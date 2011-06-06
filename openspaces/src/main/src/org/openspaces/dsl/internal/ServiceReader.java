package org.openspaces.dsl.internal;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.openspaces.dsl.Service;
import org.openspaces.dsl.ui.BarLineChart.Unit;


public class ServiceReader {

	public static Service getServiceFromFile(final File dslFile) throws Exception {

		final GroovyShell gs = createGroovyShell();

		Object result = null;
		try {
			result = gs.evaluate(dslFile);
		} catch (final CompilationFailedException e) {
			throw new Exception("The file " + dslFile +
					" could not be compiled", e);
		} catch (final IOException e) {
			throw new Exception("The file " + dslFile + " could not be read",
					e);
		}

		// final Object result = Eval.me(expr);
		if (result == null) {
			throw new Exception("The file " + dslFile + " evaluates to null, not to a service object");
		}
		if (!(result instanceof Service)) {
			throw new Exception("The file: " + dslFile + " did not evaluate to the required object type");
		}

		final Service service = (Service) result;
		return service;
	}

	@SuppressWarnings("unchecked")
	private static GroovyShell createGroovyShell() {
		final CompilerConfiguration cc = createCompilerConfiguration();
		final Binding binding = new Binding();
		final ServiceContext context = new ServiceContext(null);
		binding.getVariables().put("context", context);

		final GroovyShell gs = new GroovyShell(
				ServiceReader.class.getClassLoader(), // this.getClass().getClassLoader(),
				binding,
				cc);
		return gs;
	}

	private static CompilerConfiguration createCompilerConfiguration() {
		final CompilerConfiguration cc = new CompilerConfiguration();
		final ImportCustomizer ic = new ImportCustomizer();
		ic.addStarImports("org.openspaces.dsl", "org.openspaces.dsl.ui");
		//ic.addStaticStars(USMUtils.class.getName());
		ic.addImports(Unit.class.getName());
		cc.addCompilationCustomizers(ic);

		cc.setScriptBaseClass(BaseServiceScript.class.getName());

		return cc;
	}

}
