/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
package org.openspaces.itest.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.openspaces.core.GigaSpace;
import org.openspaces.remoting.ExecutorRemotingProxyConfigurer;
import org.openspaces.remoting.scripting.ScriptingExecutor;
import org.openspaces.remoting.scripting.StaticScript;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * JPA Native Query dynamic {@link org.openspaces.remoting.scripting.Script} execution test.
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class JpaNativeQueryScriptExecutionTest extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;
    protected EntityManagerFactory entityManagerFactory;
    
    public JpaNativeQueryScriptExecutionTest() {
        setPopulateProtectedVariables(true);
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/jpa/jpa-scripting.xml"};
    }
    
    public void testScriptExecution() {
        ScriptingExecutor<?> executor = new ExecutorRemotingProxyConfigurer<ScriptingExecutor>(gigaSpace, ScriptingExecutor.class).proxy();
        assertNotNull(executor);
        Integer result = (Integer) executor.execute(new StaticScript("groovy-script1", "groovy", "return 1"));
        assertEquals(Integer.valueOf(1), result);        
    }
    
    /**
     * Script execution test.
     */
    public void testJpaScriptExecution() {
        EntityManager em = entityManagerFactory.createEntityManager();
        Query query = em.createNativeQuery("execute ?");
        query.setParameter(1, new StaticScript("groovy-script2", "groovy", "return 1"));
        Integer result = (Integer) query.getSingleResult();
        assertEquals(Integer.valueOf(1), result);
        em.close();
    }
    
    /**
     * Script execution test within a JPA transaction - the script operations aren't transactional.
     */
    public void testJpaScriptExecutionWithTransaction() {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createNativeQuery("execute ?");
        query.setParameter(1, new StaticScript("groovy-script3", "groovy", "return 1"));
        Integer result = (Integer) query.getSingleResult();
        assertEquals(Integer.valueOf(1), result);
        em.getTransaction().commit();
        em.close();
    }
    
    
}
