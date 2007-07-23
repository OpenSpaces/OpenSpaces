package org.openspaces.itest.transaction.manager.jta;

/**
 * @author kimchy
 */
public class AtmoikosTwoSpaceJtaTransactionTests extends AbstractTwoSpaceJtaTransactionTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/transaction/manager/jta/atomikos.xml"};
    }

}