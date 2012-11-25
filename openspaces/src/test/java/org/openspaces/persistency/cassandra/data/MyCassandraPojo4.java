package org.openspaces.persistency.cassandra.data;

import java.util.Date;

public class MyCassandraPojo4
{
    private Long _longProperty;
    private Date _dateProperty;
    
    public Long getLongProperty()
    {
        return _longProperty;
    }
    
    public void setLongProperty(Long longProperty)
    {
        _longProperty = longProperty;
    }
    
    public Date getDateProperty()
    {
        return _dateProperty;
    }
    
    public void setDateProperty(Date dateProperty)
    {
        _dateProperty = dateProperty;
    }
    


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_dateProperty == null) ? 0 : _dateProperty.hashCode());
        result = prime * result
                + ((_longProperty == null) ? 0 : _longProperty.hashCode());
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
        MyCassandraPojo4 other = (MyCassandraPojo4) obj;
        if (_dateProperty == null)
        {
            if (other._dateProperty != null)
                return false;
        }
        else
            if (!_dateProperty.equals(other._dateProperty))
                return false;
        if (_longProperty == null)
        {
            if (other._longProperty != null)
                return false;
        }
        else
            if (!_longProperty.equals(other._longProperty))
                return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MyCassandraPojo4 [_longProperty=" + _longProperty
                + ", _dateProperty=" + _dateProperty + "]";
    }
 
}
