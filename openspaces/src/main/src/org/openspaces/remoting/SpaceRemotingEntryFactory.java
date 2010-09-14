package org.openspaces.remoting;

/**
 * 
 * @author Niv Ingberg
 * @since 8.0
 */
public interface SpaceRemotingEntryFactory
{
    SpaceRemotingEntry createEntry();

    HashedSpaceRemotingEntry createHashEntry();
}
