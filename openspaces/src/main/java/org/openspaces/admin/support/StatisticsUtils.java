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
package org.openspaces.admin.support;

/**
 * @author kimchy
 */
public class StatisticsUtils {

    static double megabytesFactor = 9.53674316 * Math.pow(10, -7);

    static double gigabyesFactor = 9.53674316 * Math.pow(10, -10);

    public static double convertToKilobytes(long bytes) {
        return 0.0009765625 * bytes;
    }

    public static double convertToMB(long bytes) {
        return megabytesFactor * bytes;
    }

    public static double convertToGB(long bytes) {
        return gigabyesFactor * bytes;
    }

    public static double computePerc(int value, int max) {
        return ((double) value) / max * 100;
    }

    public static double computePerc(long value, long max) {
        return ((double) value) / max * 100;
    }

    public static double computePercByTime(long currentTime, long previousTime, long currentTimestamp, long previousTimestamp) {
        return ((double) (currentTime - previousTime)) / (currentTimestamp - previousTimestamp);
    }

    public static double computePerSecond(long currentCount, long previousCount, long currentTimestamp, long previousTimestamp) {
        double stat =  ((double) (currentCount - previousCount)) / (currentTimestamp - previousTimestamp) * 1000;
        if (stat < 0) {
            return 0;
        }
        return stat;
    }

    public static String formatPerc(double perc) {
        if (perc == -1) {
            return "NA";
        }
        String p = String.valueOf(perc * 100.0);
        int ix = p.indexOf(".") + 1;
        return p.substring(0, ix) + p.substring(ix, ix + 1) + '%';
    }
}
