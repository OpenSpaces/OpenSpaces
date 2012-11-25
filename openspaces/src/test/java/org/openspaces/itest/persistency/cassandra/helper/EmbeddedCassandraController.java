package org.openspaces.itest.persistency.cassandra.helper;

import java.io.File;
import java.net.URL;
import java.util.UUID;

import org.github.jamm.MemoryMeter;
import org.jini.rio.boot.BootUtil;
import org.openspaces.itest.persistency.cassandra.helper.config.CassandraConfigUtils;
import org.openspaces.test.client.executor.Executor;
import org.openspaces.test.client.executor.RemoteAsyncCommandResult;
import org.openspaces.test.client.executor.RemoteJavaCommand;


public class EmbeddedCassandraController
{
    
    private static final String CASSANDRA_YAML = "org/openspaces/itest/persistency/cassandra/cassandra.yaml";
    
    private RemoteAsyncCommandResult<IEmbeddedCassandra> _dbRemoteAdmin;
    private IEmbeddedCassandra                           _db;
    private int                                          _rpcPort = EmbeddedCassandra.DEFAULT_RPC_PORT;
    
    public void initCassandra(boolean isEmbedded)
    {
        if (isEmbedded) 
        {
            System.setProperty("cassandra.config", CASSANDRA_YAML);
            _db = new EmbeddedCassandra();
        }
        else
        {
            try
            {
                URL baseConfiguration = Thread
                        .currentThread()
                        .getContextClassLoader()
                        .getResource(CASSANDRA_YAML);
                String randomUID = UUID.randomUUID().toString().substring(0, 8);
                File configDestination = new File("target/cassandra-" + randomUID + ".yaml");
                String root = "target/cassandra/" + randomUID;
                CassandraConfigUtils.writeCassandraConfig(baseConfiguration, configDestination, root);
                
                String rpcPort = BootUtil.getAnonymousPort();
                _rpcPort = Integer.parseInt(rpcPort);

                RemoteJavaCommand<IEmbeddedCassandra> cmd = 
                        new RemoteJavaCommand<IEmbeddedCassandra>(EmbeddedCassandra.class, null);
                cmd.addJVMArg("-ea");
                cmd.addJVMArg("-javaagent:\"" + 
                    new File(MemoryMeter.class.getProtectionDomain()
                                              .getCodeSource()
                                              .getLocation()
                                              .getPath()) + "\"");
                cmd.addJVMArg("-Xms1G");
                cmd.addJVMArg("-Xmx1G");
                cmd.addJVMArg("-XX:+HeapDumpOnOutOfMemoryError");
                cmd.addJVMArg("-XX:+UseParNewGC");
                cmd.addJVMArg("-XX:+UseConcMarkSweepGC");
                cmd.addJVMArg("-XX:+CMSParallelRemarkEnabled");
                cmd.addJVMArg("-XX:SurvivorRatio=8");
                cmd.addJVMArg("-XX:MaxTenuringThreshold=1");
                cmd.addJVMArg("-XX:CMSInitiatingOccupancyFraction=75");
                cmd.addJVMArg("-XX:+UseCMSInitiatingOccupancyOnly");
                cmd.addSystemPropParameter(EmbeddedCassandra.RPC_PORT_PROP, rpcPort);
                cmd.addSystemPropParameter("cassandra.config", configDestination.toURI().toString());
                cmd.addSystemPropParameter("cassandra.storage_port", BootUtil.getAnonymousPort());
                cmd.addSystemPropParameter("com.sun.management.jmxremote.port", BootUtil.getAnonymousPort());
                cmd.addSystemPropParameter("com.sun.management.jmxremote.ssl", "false");
                cmd.addSystemPropParameter("com.sun.management.jmxremote.authenticate", "false");
                _dbRemoteAdmin = Executor.executeAsync(cmd, null);
                _db = _dbRemoteAdmin.getRemoteStub();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void createKeySpace(String keySpaceName)
    {
        _db.createKeySpace(keySpaceName);
    }
    
    public void dropKeySpace(String keySpaceName)
    {
        _db.dropKeySpace(keySpaceName);
    }
    
    public void stopCassandra()
    {
        _db.destroy();
        if (_dbRemoteAdmin != null)
        {
            try
            {
                _dbRemoteAdmin.stop(true);
                while (_dbRemoteAdmin.getProcessAdmin().isAlive())
                {
                    Thread.sleep(1000);
                }
            }
            catch (Exception e)
            {

            }
        }
    }

    public int getRpcPort()
    {
        return _rpcPort;
    }

}
