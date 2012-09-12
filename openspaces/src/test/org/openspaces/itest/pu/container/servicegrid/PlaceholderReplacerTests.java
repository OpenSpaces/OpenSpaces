/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.itest.pu.container.servicegrid;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openspaces.core.util.PlaceholderReplacer;
import org.openspaces.core.util.PlaceholderReplacer.PlaceholderResolutionException;

/**
 * @author Dan Kilman
 */
public class PlaceholderReplacerTests extends TestCase {

    public void test1() throws Exception {
        String key1 = "VALUE";
        String value1 = "AFTER_VALUE_REPLACEMENT";
        
        Map<String, String> env = new HashMap<String, String>();
        env.put(key1, value1);

        String template = wrap(key1);
        String result = PlaceholderReplacer.replacePlaceholders(env, template);
        Assert.assertEquals(value1, result);
    }

    public void test2() throws Exception {
        String key1 = "VALUE";
        String value1 = "AFTER_VALUE_REPLACEMENT";
        
        Map<String, String> env = new HashMap<String, String>();
        env.put(key1, value1);
        
        String template = wrap(key1) + wrap(key1);
        String result = PlaceholderReplacer.replacePlaceholders(env, template);
        Assert.assertEquals(value1 + value1, result);
    }

    public void test3() throws Exception {
        String key1 = "VALUE";
        String value1 = "AFTER_VALUE_REPLACEMENT";
        
        String filler = "####";
        
        Map<String, String> env = new HashMap<String, String>();
        env.put(key1, value1);
        
        String template = wrap(key1) + filler + wrap(key1);
        String result = PlaceholderReplacer.replacePlaceholders(env, template);
        Assert.assertEquals(value1 + filler + value1, result);
    }
    
    public void test4() {
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("INNER", "123");
        env.put("TEST123TEST", "style");
        
        String template = "${TEST${INNER}TEST}";
        String result = PlaceholderReplacer.replacePlaceholders(env, template);
        Assert.assertEquals("style", result);
        
    }
    
    public void testInvalid1() {
        String key1 = "VALUE";
        
        Map<String, String> env = new HashMap<String, String>();

        String template = wrap(key1);
        
        try {
            PlaceholderReplacer.replacePlaceholders(env, template);
            Assert.fail();
        } catch (PlaceholderResolutionException expected) {

        }
        
    }

    public void testInvalid2() {

        Map<String, String> env = new HashMap<String, String>();
        
        String template = "${}";
        
        try {
            PlaceholderReplacer.replacePlaceholders(env, template);
            Assert.fail();
        } catch (PlaceholderResolutionException expected) {

        }
        
    }

    private static String wrap(String key) {
        return "${" + key + "}";
    }
}
