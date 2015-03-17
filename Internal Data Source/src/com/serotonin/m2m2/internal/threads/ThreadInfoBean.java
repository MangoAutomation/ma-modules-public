package com.serotonin.m2m2.internal.threads;

import com.serotonin.metrics.PeriodSum;

public class ThreadInfoBean {
    private long id;
    private String name;
    private final PeriodSum cpuSum = new PeriodSum(10 * 1000);
    private long lastCpuTime;
    private long cpuTime;
    private String state;
    private StackTraceElement[] stackTrace;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
        if (lastCpuTime > 0)
            cpuSum.hit(cpuTime - lastCpuTime);
        lastCpuTime = cpuTime;
    }

    public long getTenSecCpuTime() {
        return cpuSum.getSum();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
