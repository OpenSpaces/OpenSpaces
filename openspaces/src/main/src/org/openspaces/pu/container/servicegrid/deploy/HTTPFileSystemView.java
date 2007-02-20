package org.openspaces.pu.container.servicegrid.deploy;

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
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: Feb 14, 2007
 * Time: 6:13:28 PM
 */
public class HTTPFileSystemView extends FileSystemView {
    private File lastDir;
    private File[] lastResults;

    public File createNewFolder(File containingDir) throws IOException {
        throw new IOException("Not Supported");
    }

    URL root;


    public HTTPFileSystemView(URL root) {
        this.root = root;
    }

    public File[] getFiles(File dir, boolean useFileHiding) {
        //check cache
        if (lastDir != null && lastDir.equals(dir)) {
            System.out.println("cached " + dir);
            return lastResults;
        }

        File[] files = new File[0];
        String line = null;
        try {
            List filesList = new ArrayList();
            URL url = new URL(root, dir.getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                String name = tokenizer.nextToken();
                String type = tokenizer.nextToken();
                String size = tokenizer.nextToken();
                long time = Long.parseLong(tokenizer.nextToken());
                System.out.println("name = " + name + "\t\t type = " + type + "\t\ttime = " + time);
                File add = new HTTPFile(dir, name, time, type.equals("d"));
                filesList.add(add);
            }
            reader.close();
            files = (File[]) filesList.toArray(new File[filesList.size()]);
        } catch (Exception e) {
            System.out.println("line = " + line);
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
