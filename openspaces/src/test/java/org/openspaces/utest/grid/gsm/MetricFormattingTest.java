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
package org.openspaces.utest.grid.gsm;

import static org.openspaces.grid.gsm.autoscaling.AutoScalingSlaUtils.formatMetricValue;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class MetricFormattingTest extends TestCase {

    public void test() {
        Assert.assertEquals("null", formatMetricValue(null));
        Assert.assertEquals("0", formatMetricValue(0));
        Assert.assertEquals("1", formatMetricValue(1));
        Assert.assertEquals("1", formatMetricValue(1.001));
        Assert.assertEquals("0.1", formatMetricValue(0.1));
        Assert.assertEquals("4,567,000.12", formatMetricValue(4567000.123));
        Assert.assertEquals("-4,567,000.12", formatMetricValue(-4567000.123));
    }
}

