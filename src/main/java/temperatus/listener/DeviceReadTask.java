package temperatus.listener;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * @author aquesada
 */
@Component
@Scope("prototype")
public class DeviceReadTask implements Callable {

    static Logger logger = Logger.getLogger(DeviceReadTask.class.getName());

    @Autowired DeviceSemaphore deviceSemaphore;

    @Override
    public Object call() throws Exception {
        try {
            logger.debug("Reading device... trying to acquire semaphore");
            deviceSemaphore.acquire();
            logger.debug("Read Semaphore adquired!");

            Thread.sleep(2000);

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Semaphore released");
        }

        return null;
    }

}
