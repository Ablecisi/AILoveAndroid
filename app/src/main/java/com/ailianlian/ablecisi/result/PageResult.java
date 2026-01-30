package com.ailianlian.ablecisi.result;

import java.util.List;

/**
 * AILoveBacked <br>
 * com.ablecisi.ailovebacked.result <br>
 *
 * @author Ablecisi
 * @version 0.0.1
 * 2025/8/29
 * 星期五
 * 00:00
 **/
public class PageResult<T> {
    private long total;
    private List<T> records;

    public PageResult() {
    }

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
