package org.openspaces.persistency.cassandra.cache;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.Assert;

import org.junit.Test;
import org.openspaces.persistency.cassandra.NamedLockProvider;

import com.gigaspaces.internal.utils.collections.ConcurrentHashSet;

public class NamedLockProviderTest
{
    private final Random random = new Random();
    
    private final int _numberOfConcurrentClients = 10;
    private final int _maxID = 500;
    
    private final NamedLockProvider _namedLock = new NamedLockProvider();
    private final Set<ReentrantLock> _locks = new ConcurrentHashSet<ReentrantLock>();
    private final Set<Integer> _lockIDs = new ConcurrentHashSet<Integer>();

    private volatile boolean _wait = true;
    private volatile boolean _run = true;
    
    @Test
    public void test() throws Exception
    {
        LockProviderClient[] clients = new LockProviderClient[_numberOfConcurrentClients];
        for (int i = 0; i < clients.length; i++)
        {
            clients[i] = new LockProviderClient();
            clients[i].start();
        }
        
        _wait = false;
        
        Thread.sleep(100);
        
        _run = false;

        for (int i = 0; i < clients.length; i++)
            clients[i].join();
        
        Assert.assertEquals("Unexpected number of locks created", _lockIDs.size(), _locks.size());
    }
    
    private class LockProviderClient extends Thread
    {
        @Override
        public void run()
        {
            while (_wait);
            
            while (_run)
            {
                int lockID = random.nextInt(_maxID);
                ReentrantLock lock = _namedLock.forName("" + lockID);
                _locks.add(lock);
                _lockIDs.add(lockID);
            }
        }
    }
    
}
