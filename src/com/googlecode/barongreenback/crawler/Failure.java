package com.googlecode.barongreenback.crawler;

public class Failure {
    private final StagedJob job;
    private final String reason;

    private Failure(StagedJob job, String reason) {
        this.job = job;
        this.reason = reason;
    }

    public static Failure failure(StagedJob job, String reason) {
        return new Failure(job, reason);
    }

    public StagedJob job() {
        return job;
    }

    public String reason() {
        return reason;
    }

    @Override
    public int hashCode() {
        return job.hashCode() * reason.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Failure && job.equals(((Failure) other).job()) && reason.equals(((Failure) other).reason());
    }

    @Override
    public String toString() {
        return String.format("%s, %s", job.toString(), reason);
    }
}
