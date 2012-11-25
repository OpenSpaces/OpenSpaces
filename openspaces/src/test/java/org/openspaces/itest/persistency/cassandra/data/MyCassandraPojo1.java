package org.openspaces.itest.persistency.cassandra.data;

import java.io.Serializable;

import com.gigaspaces.document.SpaceDocument;

public class MyCassandraPojo1 implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private SpaceDocument _spaceDocument; 
    private String _str;
    
    public MyCassandraPojo1() { }
    
    public MyCassandraPojo1(String str) { this.setStr(str); }
    
    public String getStr()
    {
        return _str;
    }
    public void setStr(String str)
    {
        this._str = str;
    }

    public SpaceDocument getSpaceDocument()
    {
        return _spaceDocument;
    }

    public void setSpaceDocument(SpaceDocument spaceDocument)
    {
        _spaceDocument = spaceDocument;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_spaceDocument == null) ? 0 : _spaceDocument.hashCode());
        result = prime * result + ((_str == null) ? 0 : _str.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyCassandraPojo1 other = (MyCassandraPojo1) obj;
        if (_spaceDocument == null)
        {
            if (other._spaceDocument != null)
                return false;
        }
        else
            if (!_spaceDocument.equals(other._spaceDocument))
                return false;
        if (_str == null)
        {
            if (other._str != null)
                return false;
        }
        else
            if (!_str.equals(other._str))
                return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        return "MyCassandraPojo1 [_spaceDocument=" + _spaceDocument + ", _str="
                + _str + "]";
    }
}