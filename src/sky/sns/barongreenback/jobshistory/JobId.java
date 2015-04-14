package com.googlecode.barongreenback.jobshistory;

import com.googlecode.totallylazy.Value;

import java.util.UUID;

public class JobId implements Value<UUID> {
    private UUID id;

    public JobId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID value() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobId jobId = (JobId) o;

        if (!id.equals(jobId.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
