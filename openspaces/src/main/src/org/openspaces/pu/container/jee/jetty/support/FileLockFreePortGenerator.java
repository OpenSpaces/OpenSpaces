package org.openspaces.pu.container.jee.jetty.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import com.j_spaces.kernel.Environment;

/**
 * A free port generator that uses file locks in order to obtain a port. Aimed at trying to fix the problem
 * that several jetty connectors can be opened on the same port on windows. 
 *
 * @author kimchy
 */
public class FileLockFreePortGenerator implements FreePortGenerator {

    private static final Log logger = LogFactory.getLog(FileLockFreePortGenerator.class);

    private static final File portDirectory;

    static {
        File jettyWork = new File(System.getProperty("com.gs.work", Environment.getHomeDirectory() + "/work") + "/jetty");
        portDirectory = new File(jettyWork, "ports");
        portDirectory.mkdirs();
    }

    public PortHandle nextAvailablePort(int startFromPort, int retryCount) {
        int portNumber = -1;
        FileLock portFileLock = null;
        RandomAccessFile portFile = null;
        for (int i = 0; i < retryCount; i++) {
            portNumber = startFromPort + i;
            File portF = new File(portDirectory, portNumber + ".port");
            try {
                portFile = new RandomAccessFile(portF, "rw");
            } catch (FileNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to open port marker file [" + portF.getAbsolutePath() + "]", e);
                }
                // can't get this one, continue
                continue;
            }
            try {
                portFileLock = portFile.getChannel().tryLock();
                if (portFileLock == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Can't get lock for [" + portF.getAbsolutePath() + "], try another");
                    }
                    try {
                        portFile.close();
                    } catch (Exception e) {
                        // ignore
                    }
                    continue;
                }
            } catch (OverlappingFileLockException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Can't get lock for [" + portF.getAbsolutePath() + "], try another, " + e.getMessage());
                }
                try {
                    portFile.close();
                } catch (Exception e1) {
                    // ignore
                }
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to get lock file for [" + portF.getAbsolutePath() + "]", e);
                }
                try {
                    portFile.close();
                } catch (Exception e1) {
                    // ignore
                }
                // failed to get the lock, continue
                continue;
            }
            // all is well, break
            break;
        }
        if (portFileLock == null) {
            throw new IllegalStateException("Failed to acquire port file lock, tried for [" + retryCount + "], basePort [" + startFromPort + "]");
        }
        return new FileLockPortHandle(portFile, portFileLock, portNumber);
    }

    public static class FileLockPortHandle implements PortHandle {

        private final RandomAccessFile portFile;

        private final FileLock portFileLock;

        private final int portNumber;

        public FileLockPortHandle(RandomAccessFile portFile, FileLock portFileLock, int portNumber) {
            this.portFile = portFile;
            this.portFileLock = portFileLock;
            this.portNumber = portNumber;
        }

        public int getPort() {
            return portNumber;
        }

        public void release() {
            try {
                portFileLock.release();
            } catch (IOException e) {
                logger.debug("Failed to release port file lock for port [" + portNumber + "]", e);
            }

            try {
                portFile.close();
            } catch (IOException e) {
                logger.debug("Failed to close port file for port [" + portNumber + "]", e);
            }
        }
    }
}
