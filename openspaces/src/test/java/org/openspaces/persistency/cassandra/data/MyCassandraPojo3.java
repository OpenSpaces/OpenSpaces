package org.openspaces.persistency.cassandra.data;

import java.io.Serializable;

public class MyCassandraPojo3 implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _name;
    private Integer _age;
    private MyCassandraPojo4 _cassandraPojo4_1;
    private MyCassandraPojo4 _cassandraPojo4_2;
    
    public MyCassandraPojo3() { }
    
    public String getName()
    {
        return _name;
    }

    public Integer getAge()
    {
        return _age;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public void setAge(Integer age)
    {
        _age = age;
    }

    public MyCassandraPojo4 getCassandraPojo4_1()
    {
        return _cassandraPojo4_1;
    }

    public void setCassandraPojo4_1(MyCassandraPojo4 cassandraPojo4_1)
    {
        _cassandraPojo4_1 = cassandraPojo4_1;
    }

    public MyCassandraPojo4 getCassandraPojo4_2()
    {
        return _cassandraPojo4_2;
    }

    public void setCassandraPojo4_2(MyCassandraPojo4 cassandraPojo4_2)
    {
        _cassandraPojo4_2 = cassandraPojo4_2;
    }



    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_age == null) ? 0 : _age.hashCode());
        result = prime
                * result
                + ((_cassandraPojo4_1 == null) ? 0
                                              : _cassandraPojo4_1.hashCode());
        result = prime
                * result
                + ((_cassandraPojo4_2 == null) ? 0
                                              : _cassandraPojo4_2.hashCode());
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
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
        MyCassandraPojo3 other = (MyCassandraPojo3) obj;
        if (_age == null)
        {
            if (other._age != null)
                return false;
        }
        else
            if (!_age.equals(other._age))
                return false;
        if (_cassandraPojo4_1 == null)
        {
            if (other._cassandraPojo4_1 != null)
                return false;
        }
        else
            if (!_cassandraPojo4_1.equals(other._cassandraPojo4_1))
                return false;
        if (_cassandraPojo4_2 == null)
        {
            if (other._cassandraPojo4_2 != null)
                return false;
        }
        else
            if (!_cassandraPojo4_2.equals(other._cassandraPojo4_2))
                return false;
        if (_name == null)
        {
            if (other._name != null)
                return false;
        }
        else
            if (!_name.equals(other._name))
                return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MyCassandraPojo3 [_name=" + _name + ", _age=" + _age
                + ", _cassandraPojo4_1=" + _cassandraPojo4_1
                + ", _cassandraPojo4_2=" + _cassandraPojo4_2 + "]";
    }

    
    
}