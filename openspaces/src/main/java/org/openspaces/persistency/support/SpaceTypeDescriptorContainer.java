package org.openspaces.persistency.support;

import com.gigaspaces.internal.io.IOUtils;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorVersionedSerializationUtils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * @author Niv Ingberg
 * @since 9.7
 */
public class SpaceTypeDescriptorContainer implements Externalizable {

    private static final long serialVersionUID = 1L;

    private SpaceTypeDescriptor spaceTypeDescriptor;

    /** Required for Externalizable */
    public SpaceTypeDescriptorContainer() {

    }

    public SpaceTypeDescriptorContainer(SpaceTypeDescriptor typeDescriptor) {
        spaceTypeDescriptor = typeDescriptor;
    }

    public SpaceTypeDescriptor getTypeDescriptor() {
        return spaceTypeDescriptor;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        IOUtils.writeObject(out, SpaceTypeDescriptorVersionedSerializationUtils.toSerializableForm(
                spaceTypeDescriptor));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Serializable typeDescriptorVersionedSerializableWrapper = IOUtils.readObject(in);
        spaceTypeDescriptor = SpaceTypeDescriptorVersionedSerializationUtils.fromSerializableForm(
                typeDescriptorVersionedSerializableWrapper);
    }
}
