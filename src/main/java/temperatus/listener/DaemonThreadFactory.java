package temperatus.listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by alberto on 13/2/16.
 */
public class DaemonThreadFactory implements ThreadFactory {

    public Thread newThread(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    }

}

