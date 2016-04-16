package temperatus.listener;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory to create threads to access DB. Only one database operation at a time.
 * <p>
 * Created by alberto on 17/2/16.
 */
public class DatabaseThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);   // only one access to DB at a time

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
        thread.setDaemon(true);
        return thread;
    }
}
