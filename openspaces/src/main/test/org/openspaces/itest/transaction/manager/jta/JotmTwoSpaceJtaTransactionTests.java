package org.openspaces.itest.transaction.manager.jta;

/**
 * @author kimchy
 */
public class JotmTwoSpaceJtaTransactionTests extends AbstractTwoSpaceJtaTransactionTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/transaction/manager/jta/jotm.xml"};
    }

}
