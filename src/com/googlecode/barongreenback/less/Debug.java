package com.googlecode.barongreenback.less;

public class Debug {
    public static boolean inDebug() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    }
}
