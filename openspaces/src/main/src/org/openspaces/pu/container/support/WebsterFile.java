package org.openspaces.pu.container.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author kimchy
 */
public class WebsterFile extends File {

    private URL root;

    private URL url;

    private boolean directory = true;

    private long time = -1;

    public WebsterFile(URL url) {
        super("");
        this.root = url;
        this.url = url;
    }

    WebsterFile(URL root, URL url, String name, long time, boolean directory) throws MalformedURLException {
        super("");
        String fullUrl = url.toExternalForm();
        if (!fullUrl.endsWith("/")) {
            fullUrl += "/" + name;
        } else {
            fullUrl += name;
        }
        this.root = root;
        this.url = new URL(fullUrl);
        this.time = time;
        this.directory = directory;
    }

    public URL toURL() throws MalformedURLException {
        return this.url;
    }

    public String getPath() {
        return url.toExternalForm().substring(root.toExternalForm().length());
    }

    public String getAbsolutePath() {
        return url.toExternalForm();
    }

    public File getAbsoluteFile() {
        return this;
    }

    public boolean exists() {
        return true;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isAbsolute() {
        return true;
    }

    public File[] listFiles() {
        String line;
        try {
            List<File> filesList = new ArrayList<File>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                String name = tokenizer.nextToken();
                String type = tokenizer.nextToken();
                String size = tokenizer.nextToken();
                long time = Long.parseLong(tokenizer.nextToken());
                File add = new WebsterFile(root, url, name, time, type.equals("d"));
                filesList.add(add);
            }
            reader.close();
            return filesList.toArray(new File[filesList.size()]);
        } catch (Exception e) {
            return new File[0];
        }
    }

    public boolean isFile() {
        return !directory;
    }

    public long lastModified() {
        return time;
    }
}
