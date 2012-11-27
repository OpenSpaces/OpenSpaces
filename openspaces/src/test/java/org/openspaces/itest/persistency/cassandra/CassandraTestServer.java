package org.openspaces.itest.persistency.cassandra;

import org.openspaces.itest.persistency.cassandra.helper.EmbeddedCassandraController;

import com.gigaspaces.logger.GSLogConfigLoader;

public class CassandraTestServer {

	private String                                      keySpaceName = "space";
    private int                                         rpcPort;
    private final EmbeddedCassandraController           cassandraController = new EmbeddedCassandraController();
    
    /**
     * @param isEmbedded - run Cassandra in this process. Use for debugging only since causes leaks.
     */
	public void initialize(boolean isEmbedded) {
		if (CassandraTestSuite.isSuiteMode())
        {
            keySpaceName = CassandraTestSuite.createKeySpaceAndReturnItsName();
            rpcPort = CassandraTestSuite.getRpcPort();
        }
        else
        {
            GSLogConfigLoader.getLoader();
            cassandraController.initCassandra(isEmbedded);
            cassandraController.createKeySpace(keySpaceName);
            rpcPort = cassandraController.getRpcPort();
        }
	}
	
	public void destroy() {
        if (!CassandraTestSuite.isSuiteMode()) {
            cassandraController.stopCassandra();
        }
	}

	public int getPort() {
		return rpcPort;
	}

	public String getKeySpaceName() {
		return keySpaceName;
	}
	
	public String getHost() {
		return "localhost";
	}
}
