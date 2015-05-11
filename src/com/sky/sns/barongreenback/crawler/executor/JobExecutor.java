package sky.sns.barongreenback.crawler.executor;

import sky.sns.barongreenback.crawler.StatusMonitor;

import java.io.Closeable;

public interface JobExecutor<R extends Runnable> extends StatusMonitor, Closeable {

    public void execute(R command);

}
