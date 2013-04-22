/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openspaces.pu.container.servicegrid;

/**
 * Helper class to tell whether scala libary is included in classpath or not.
 * 
 * @since 9.6
 * @author Dan Kilman
 * 
 */
public class ScalaIdentifier {

    private static final Class<?> SCALA_UNIT_CLASS = tryLoadClass("scala.Unit");
    
    private static Class<?> tryLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isScalaLibInClassPath() {
        return SCALA_UNIT_CLASS != null;
    }
    
}
