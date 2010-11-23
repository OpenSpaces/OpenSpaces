package org.openspaces.jpa.openjpa.query;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.openspaces.jpa.openjpa.GSStoreManager;

import com.gigaspaces.internal.transport.IEntryPacket;

/**
 * A wrapper for holding JPQL queries result set.
 * The getResultObject() method should initiate a state manager for the loaded object.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class SpaceResultObjectProvider implements ResultObjectProvider {
    //
    private Object[] _result;
    private int _currentIndex;
    private GSStoreManager _store;
    private ClassMetaData _classMetaData;
    
    public SpaceResultObjectProvider(ClassMetaData classMetaData, Object[] result, GSStoreManager store) {
        _result = result;
        _currentIndex = -1;
        _store = store;
        _classMetaData = classMetaData;
    }
    
    public boolean absolute(int pos) throws Exception {
        if (pos >= 0 && pos < _result.length) {
            _currentIndex = pos;
            return true;
        }
        return false;
    }

    public void close() throws Exception {
        reset();
    }

    /**
     * Gets the current result as a Pojo initiated with a state manager.
     */
    public Object getResultObject() throws Exception {
        return _store.loadObject(_classMetaData, (IEntryPacket) _result[_currentIndex]);
    }

    public void handleCheckedException(Exception e) {
        // openjpa: shouldn't ever happen
        throw new NestableRuntimeException(e);
    }

    public boolean next() throws Exception {
        return absolute(_currentIndex + 1);
    }

    public void open() throws Exception {                
    }

    public void reset() throws Exception {
        _currentIndex = -1;
    }

    public int size() throws Exception {
        return _result.length;
    }

    public boolean supportsRandomAccess() {
        return true;
    }

}
