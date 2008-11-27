package org.openspaces.admin.space;

/**
 * @author kimchy
 */
public interface Spaces extends Iterable<Space> {

    Space[] getSpaces();

    Space getSpaceByUID(String uid);

    Space getSpaceByName(String name);
}
