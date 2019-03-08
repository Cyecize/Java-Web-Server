package com.cyecize.toyote.models;

public class FrequencyCounter {

    private long count;

    public FrequencyCounter() {
        this.count = 0L;
    }

    public long getCount() {
        return count;
    }

    public void count() {
        this.count++;
    }

    public void clear() {
        this.count = 0L;
    }
}
