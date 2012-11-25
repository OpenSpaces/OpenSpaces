package org.openspaces.test.client.executor;

public class ForkProcessException
        extends Exception
{

    private static final long serialVersionUID = 1L;

    public ForkProcessException(String string, Throwable t)
    {
        super(string, t);
    }

}
