package org.openspaces.itest.persistency.cassandra.data;

import java.io.Serializable;

public class MyCassandraCyclicPojoButtom implements Serializable
{

    private static final long serialVersionUID = 1L;
    
    public MyCassandraCyclicPojoTop top;

    public Integer number = 1;
    
    public MyCassandraCyclicPojoTop getTop()
    {
        return top;
    }

    public void setTop(MyCassandraCyclicPojoTop top)
    {
        this.top = top;
    }

    public Integer getNumber()
    {
        return number;
    }

    public void setNumber(Integer number)
    {
        this.number = number;
    }
    
}
