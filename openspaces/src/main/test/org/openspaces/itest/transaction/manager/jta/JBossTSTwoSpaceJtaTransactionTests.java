package org.openspaces.itest.transaction.manager.jta;

/**
 * @author kimchy
 */
public class JBossTSTwoSpaceJtaTransactionTests extends AbstractTwoSpaceJtaTransactionTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/transaction/manager/jta/jbossts.xml"};
    }

}