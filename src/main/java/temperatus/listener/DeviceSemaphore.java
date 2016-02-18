package temperatus.listener;

import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 *
 * @author aquesada
 */
@Component
public class DeviceSemaphore {
    
    private Semaphore semaphore = new Semaphore(1);
    
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }
    
    public void release() {
        semaphore.release();
    }
}
