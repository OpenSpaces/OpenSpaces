package org.openspaces.persistency.cassandra.helper.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CassandraTestUtils
{
    
    public static void writeToFile(File file, String content) throws IOException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(file);
            writer.write(content);
        }
        finally
        {
            if (writer != null)
                writer.close();
        }
    }
    
    public static void deleteFileOrDirectory(File fileOrDirectory) throws IOException
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File file : fileOrDirectory.listFiles())
                deleteFileOrDirectory(file);
        }
        if (!fileOrDirectory.delete())
        {
            throw new IOException("Failed deleting " + fileOrDirectory);
        }
    }
}
