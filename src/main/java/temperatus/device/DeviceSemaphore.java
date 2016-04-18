package temperatus.device;

import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 * Custom semaphore to assure that only one thread is reading from a device at a time.
 *
 * @author aquesada
 */
@Component
public class DeviceSemaphore {

    private Semaphore semaphore = new Semaphore(1); // Only one access is allowed at a time

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    public void release() {
        semaphore.release();
    }
}
