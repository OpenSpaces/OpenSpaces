/*
 * Copyright 2006-2007 the original author or authors.
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

package org.openspaces.maven.plugin;

import org.apache.maven.project.MavenProject;

import java.util.Comparator;

/**
 * Used to sort the deployment order of the processing unit
 * when deploying a multi module project.
 */
public class PUProjectSorter implements Comparator {

    static final String PARAM_ORDER = "order";

    static final String GS_TYPE = "gsType";

    static final String GS_TYPE_PU = "PU";

    private boolean assending;

    public PUProjectSorter(boolean assending) {
        this.assending = assending;
    }

    public int compare(Object obj1, Object obj2) {
        MavenProject proj1 = (MavenProject) obj1;
        MavenProject proj2 = (MavenProject) obj2;

        String orderStr1 = (String) proj1.getProperties().get(PARAM_ORDER);
        String orderStr2 = (String) proj2.getProperties().get(PARAM_ORDER);

        // if one of the order values is null
        if (orderStr1 == null) {
            if (orderStr2 == null) {
                return 0;
            } else return (assending ? -1 : 1);
        } else {
            if (orderStr2 == null) {
                return assending ? 1 : -1;
            }
        }

        // both are not null
        int order1;
        int order2;
        try {
            order1 = Integer.parseInt(orderStr1);
        } catch (NumberFormatException e) {
            //getLog().warn("Problem parsing \""+PARAM_ORDER+"\" parameter in project "+proj1.getName(), e);
            order1 = -1;
        }
        try {
            order2 = Integer.parseInt(orderStr2);
        }
        catch (NumberFormatException e) {
            //getLog().warn("Problem parsing \""+PARAM_ORDER+"\" parameter in project "+proj1.getName(), e);
            order2 = -1;
        }
        return assending ? (order1 - order2) : (order2 - order1);
    }
}