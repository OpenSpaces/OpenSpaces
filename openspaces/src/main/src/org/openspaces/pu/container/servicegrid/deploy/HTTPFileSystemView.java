/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.servicegrid.deploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public class HTTPFileSystemView extends FileSystemView {

    private static final Log logger = LogFactory.getLog(HTTPFileSystemView.class);

    private File lastDir;

    private File[] lastResults;

    private URL root;

    public HTTPFileSystemView(URL root) {
        this.root = root;
    }

    public File createNewFolder(File containingDir) throws IOException {
        throw new IOException("Not Supported");
    }

    public File[] getFiles(File dir, boolean useFileHiding) {
        //check cache
        if (lastDir != null && lastDir.equals(dir)) {
            return lastResults;
        }

        File[] files = new File[0];
        String line;
        try {
            List<File> filesList = new ArrayList<File>();
            URL url = new URL(root, dir.getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                String name = tokenizer.nextToken();
                String type = tokenizer.nextToken();
                String size = tokenizer.nextToken();
                long time = Long.parseLong(tokenizer.nextToken());
                File add = new HTTPFile(dir, name, time, type.equals("d"));
                filesList.add(add);
            }
            reader.close();
            files = filesList.toArray(new File[filesList.size()]);
        } catch (Exception e) {
            logger.debug("Error getting file list:" + e.toString());
        }
        //saved for cache
        lastDir = dir;
        lastResults = files;
        return files;
    }

    public boolean isHiddenFile(File f) {
        return false;
    }

    public File[] getRoots() {
        return new File[]{
                new File("/")
        };
    }

    public File getHomeDirectory() {
        return getRoots()[0];
    }

    public boolean isRoot(File f) {
        return f.equals(getRoots()[0]);
    }

    public static void main(String[] args) throws MalformedURLException {
        HTTPFileSystemView view = new HTTPFileSystemView(new URL("http://192.168.1.133:8080"));
        File helloworld = view.createFileObject("helloworld");
        File lib = view.createFileObject(helloworld, "lib");
        File[] jars = view.getFiles(lib, false);
        System.out.println("jars = " + Arrays.asList(jars));

        File classes = view.createFileObject(helloworld, "classes");
        System.out.println("classes = " + classes);

//        JFileChooser jFileChooser = new JFileChooser("/", view);
//
//        jFileChooser.showOpenDialog(null);
    }


    class HTTPFile extends File {
        private long time;
        private boolean dir;

        public HTTPFile(File parent, String pathname, long time, boolean isDir) {
            super(parent, pathname);

            this.time = time;
            dir = isDir;
        }


        public boolean exists() {
            return true;
        }

        public boolean isDirectory() {
            return dir;
        }

        public boolean isAbsolute() {
            return true;
        }

        public boolean isFile() {
            return !dir;
        }

        public long lastModified() {
            return time;
        }
    }
}
