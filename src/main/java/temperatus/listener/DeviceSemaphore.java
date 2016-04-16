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
    
    void acquire() throws InterruptedException {
        semaphore.acquire();
    }
    
    void release() {
        semaphore.release();
    }
}
