package org.openspaces.itest.utils;


public class TestUtils {

    public static void repetitive(Runnable repeatedAssert, int timeout) {
        for (int delay = 0; delay < timeout; delay += 5) {
            try {
                repeatedAssert.run();
                return;
            } catch (AssertionError e) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e1) {
                }
            }
        }
        repeatedAssert.run();
    }
}
