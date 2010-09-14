package org.openspaces.remoting;

/**
 * SpaceRemotingEntryFactory for MetaDataEntry classes.
 * @author Niv Ingberg
 * @since 8.0
 * @deprecated since 8.0
 */
@Deprecated
public class SpaceRemotingEntryMetadataFactory implements SpaceRemotingEntryFactory
{
    public SpaceRemotingEntry createEntry() {
        return new EventDrivenSpaceRemotingEntry();
    }

    public HashedSpaceRemotingEntry createHashEntry() {
        return new HashedEventDrivenSpaceRemotingEntry();
    }
}
