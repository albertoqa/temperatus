package temperatus.listener;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by alberto on 17/2/16.
 */
public class DatabaseThreadFactory implements ThreadFactory {
    static final AtomicInteger poolNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
        thread.setDaemon(true);

        return thread;
    }
}
