package temperatus.device;

import java.util.concurrent.Semaphore;

/**
 * Custom semaphore to assure that only one thread is reading from a device at a time.
 * Singleton class, only one instance allowed.
 *
 * @author aquesada
 */
public class DeviceSemaphore {

    private Semaphore semaphore = new Semaphore(1); // Only one access is allowed at a time

    private static class InstanceHolder {
        private static final DeviceSemaphore instance = new DeviceSemaphore();
    }

    public static DeviceSemaphore getInstance() {
        return InstanceHolder.instance;
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    public void release() {
        semaphore.release();
    }
}
