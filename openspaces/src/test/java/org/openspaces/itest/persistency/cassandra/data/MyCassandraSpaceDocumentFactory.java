/*******************************************************************************
 *
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.openspaces.itest.persistency.cassandra.data;

import com.gigaspaces.document.SpaceDocument;
import org.openspaces.itest.persistency.common.data.TestPojo3;
import org.openspaces.itest.persistency.common.data.TestPojo4;

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
            TestPojo4 myCassandraPojo4)
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
            TestPojo3 myCassandraPojo3)
    {
        return new SpaceDocument("MyCassandraDocument6")
            .setProperty("myCassandraPojo3", myCassandraPojo3);
    }
    
    
    
}
