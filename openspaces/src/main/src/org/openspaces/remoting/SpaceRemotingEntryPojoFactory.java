package org.openspaces.remoting;

/**
 * SpaceRemotingEntryFactory for Pojo classes.
 * @author Niv Ingberg
 * @since 8.0
 */
public class SpaceRemotingEntryPojoFactory implements SpaceRemotingEntryFactory  {

    public SpaceRemotingEntry createEntry() {
        throw new UnsupportedOperationException("This operation is currently not supported.");
    }

    public HashedSpaceRemotingEntry createHashEntry() {
        throw new UnsupportedOperationException("This operation is currently not supported.");
    }
}
