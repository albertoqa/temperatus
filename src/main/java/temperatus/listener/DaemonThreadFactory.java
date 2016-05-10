package temperatus.listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * ThreadFactory that creates daemon threads. (Daemon thread - finish on application exit)
 * <p>
 * Created by alberto on 13/2/16.
 */
public class DaemonThreadFactory implements ThreadFactory {

    /**
     * Create a new thread with the runnable given and set the thread as a daemon
     *
     * @param r runnable
     * @return daemon thread
     */
    public Thread newThread(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    }

}

