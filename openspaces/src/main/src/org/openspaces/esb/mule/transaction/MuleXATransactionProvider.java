package org.openspaces.esb.mule.transaction;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.LocalTransactionManager;
import com.j_spaces.core.client.XAResourceImpl;
import net.jini.core.transaction.Transaction;
import org.mule.transaction.TransactionCoordination;
import org.mule.transaction.XaTransaction;
import org.openspaces.core.TransactionDataAccessException;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.transaction.TransactionDefinition;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.rmi.RemoteException;

/**
 * @author kimchy (Shay Banon)
 */
public class MuleXATransactionProvider implements TransactionProvider {

    public Transaction.Created getCurrentTransaction(Object transactionalContext, IJSpace space) {
        org.mule.api.transaction.Transaction tx = TransactionCoordination.getInstance().getTransaction();
        if (!(tx instanceof XaTransaction)) {
            return null;
        }
        XaTransaction xaTransaction = (XaTransaction) tx;
        if (xaTransaction.hasResource(space)) {
            // already bound the space, return
            return ((CustomXaResource) xaTransaction.getResource(space)).transaction;
        }
        LocalTransactionManager localTxManager;
        try {
            localTxManager = (LocalTransactionManager) LocalTransactionManager.getInstance(space);
        } catch (RemoteException e) {
            throw new TransactionDataAccessException("Failed to get local transaction manager for space [" + space + "]", e);
        }
        CustomXaResource xaResourceSpace = new CustomXaResource(new XAResourceImpl(localTxManager, space));

        // enlist the Space xa resource with the current JTA transaction
        // we rely on the fact that this call will start the XA transaction
        try {
            xaTransaction.bindResource(space, xaResourceSpace);
        } catch (Exception e) {
            throw new TransactionDataAccessException("Failed to enlist xa resource [" + xaResourceSpace + "] with space [" + space + "]", e);
        }

        // get the context transaction from the Space and nullify it. We will handle
        // the declarative transaction nature using Spring sync
        Transaction.Created transaction = ((ISpaceProxy) space).getContextTransaction();
        ((ISpaceProxy) space).setContextTansaction(null);

        xaResourceSpace.transaction = transaction;

        return transaction;
    }

    public int getCurrentTransactionIsolationLevel(Object transactionalContext) {
        return TransactionDefinition.ISOLATION_DEFAULT;
    }

    public boolean isEnabled() {
        return true;
    }

    private static class CustomXaResource implements XAResource {

        public final XAResource actual;

        public Transaction.Created transaction;

        private CustomXaResource(XAResource actual) {
            this.actual = actual;
        }

        public void commit(Xid xid, boolean b) throws XAException {
            actual.commit(xid, b);
        }

        public void end(Xid xid, int i) throws XAException {
            actual.end(xid, i);
        }

        public void forget(Xid xid) throws XAException {
            actual.forget(xid);
        }

        public int getTransactionTimeout() throws XAException {
            return actual.getTransactionTimeout();
        }

        public boolean isSameRM(XAResource xaResource) throws XAException {
            return actual.isSameRM(xaResource);
        }

        public int prepare(Xid xid) throws XAException {
            return actual.prepare(xid);
        }

        public Xid[] recover(int i) throws XAException {
            return actual.recover(i);
        }

        public void rollback(Xid xid) throws XAException {
            actual.rollback(xid);
        }

        public boolean setTransactionTimeout(int i) throws XAException {
            return actual.setTransactionTimeout(i);
        }

        public void start(Xid xid, int i) throws XAException {
            actual.start(xid, i);
        }
    }
}
