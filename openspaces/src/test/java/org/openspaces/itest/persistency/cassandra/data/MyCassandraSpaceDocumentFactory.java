package org.openspaces.itest.persistency.cassandra.data;

import com.gigaspaces.document.SpaceDocument;

public class MyCassandraSpaceDocumentFactory
{
    
    public static SpaceDocument getMyCassandraDocument1(String firstName, String lastName)   
    {
        return new SpaceDocument("MyCassandraDocument1")
            .setProperty("firstName", firstName)
            .setProperty("lastName", lastName);
    }

    public static SpaceDocument getMyCassandraDocument3(
            Integer intProperty, 
            Long longProperty, 
            Boolean booleanProperty)   
    {
        return new SpaceDocument("MyCassandraDocument3")
            .setProperty("intProperty", intProperty)
            .setProperty("longProperty", longProperty)
            .setProperty("booleanProperty", booleanProperty);
    }

    public static SpaceDocument getMyCassandraDocument4(
            Integer intProperty, 
            MyCassandraPojo4 myCassandraPojo4)   
    {
        return new SpaceDocument("MyCassandraDocument4")
            .setProperty("intProperty", intProperty)
            .setProperty("myCassandraPojo4", myCassandraPojo4);
    }

    public static SpaceDocument getMyCassandraDocument5(
            Integer intProperty, 
            SpaceDocument spaceDocument)   
    {
        return new SpaceDocument("MyCassandraDocument5")
            .setProperty("intProperty", intProperty)
            .setProperty("spaceDocument", spaceDocument);
    }

    public static SpaceDocument getMyCassandraDocument6(
            MyCassandraPojo3 myCassandraPojo3)   
    {
        return new SpaceDocument("MyCassandraDocument6")
            .setProperty("myCassandraPojo3", myCassandraPojo3);
    }
    
    
    
}
