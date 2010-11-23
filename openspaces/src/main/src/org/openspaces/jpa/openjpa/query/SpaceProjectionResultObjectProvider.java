package org.openspaces.jpa.openjpa.query;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.openjpa.lib.rop.ResultObjectProvider;


/**
 * A wrapper for holding JPQL projection query result set (aggregation...)
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class SpaceProjectionResultObjectProvider implements ResultObjectProvider {
    //
    private Object[][] _result;
    private int _currentIndex;
    
    public SpaceProjectionResultObjectProvider(Object[][] result) {
        _result = result;
        _currentIndex = -1;
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
        return _result[_currentIndex];
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
