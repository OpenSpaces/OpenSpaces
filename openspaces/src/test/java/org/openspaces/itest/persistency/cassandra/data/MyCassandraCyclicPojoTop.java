package org.openspaces.itest.persistency.cassandra.data;

import java.io.Serializable;

public class MyCassandraCyclicPojoTop implements Serializable
{

    private static final long serialVersionUID = 1L;

    public MyCassandraCyclicPojoButtom buttom;

    public Integer number = 1;
    
    public MyCassandraCyclicPojoButtom getButtom()
    {
        return buttom;
    }

    public void setButtom(MyCassandraCyclicPojoButtom buttom)
    {
        this.buttom = buttom;
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
