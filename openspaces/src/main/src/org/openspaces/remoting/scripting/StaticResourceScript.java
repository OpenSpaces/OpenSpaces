package org.openspaces.remoting.scripting;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A static script that uses Spring {@link Resource} and {@link ResourceLoader} to load
 * a given script (for example, from the classpath).
 *
 * @author kimchy
 */
public class StaticResourceScript extends StaticScript {

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    public StaticResourceScript() {
        super();
    }

    public StaticResourceScript(String name, String type, String resourceLocation) {
        script(resourceLocation);
        type(type);
        name(name);
    }

    public StaticScript script(String resourceLocation) {
        super.script(loadResource(resourceLoader.getResource(resourceLocation)));
        return this;
    }

    private String loadResource(Resource resource) throws ScriptExecutionException {
        try {
            return FileCopyUtils.copyToString(new BufferedReader(new InputStreamReader(resource.getInputStream())));
        } catch (IOException e) {
            throw new ScriptingException("Failed to load script resource [" + resource + "]");
        }
    }
}
