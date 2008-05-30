package org.openspaces.pu.container.support;

import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author kimchy
 */
public class WebsterResoruce extends UrlResource {

    public WebsterResoruce(URL url) {
        super(url);
    }

    public WebsterResoruce(URI uri) throws MalformedURLException {
        super(uri);
    }

    public WebsterResoruce(String path) throws MalformedURLException {
        super(path);
    }

    public File getFile() throws IOException {
        return new WebsterFile(getURL());
    }
}
