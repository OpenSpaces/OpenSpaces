package org.openspaces.itest.persistency.cassandra.data;

import java.io.Serializable;
import java.util.UUID;

public class MyCassandraPojo2 implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _name;
    private Integer _age;
    private UUID _uuid;

    public MyCassandraPojo2() { }
    
    public MyCassandraPojo2(String name, int age)
    {
        _name = name;
        _age = age;
        _uuid = UUID.randomUUID();
    }

    public String getName()
    {
        return _name;
    }

    public Integer getAge()
    {
        return _age;
    }

    public UUID getUuid()
    {
        return _uuid;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public void setAge(Integer age)
    {
        _age = age;
    }

    public void setUuid(UUID uuid)
    {
        _uuid = uuid;
    }
    


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_age == null) ? 0 : _age.hashCode());
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        result = prime * result + ((_uuid == null) ? 0 : _uuid.hashCode());
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
        MyCassandraPojo2 other = (MyCassandraPojo2) obj;
        if (_age == null)
        {
            if (other._age != null)
                return false;
        }
        else
            if (!_age.equals(other._age))
                return false;
        if (_name == null)
        {
            if (other._name != null)
                return false;
        }
        else
            if (!_name.equals(other._name))
                return false;
        if (_uuid == null)
        {
            if (other._uuid != null)
                return false;
        }
        else
            if (!_uuid.equals(other._uuid))
                return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MyCassandraPojo2 [_name=" + _name + ", _age=" + _age
                + ", _uuid=" + _uuid + "]";
    }


    
}