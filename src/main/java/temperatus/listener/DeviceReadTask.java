package temperatus.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger logger = LoggerFactory.getLogger(DeviceReadTask.class.getName());

    @Autowired DeviceSemaphore deviceSemaphore;

    @Override
    public Object call() throws Exception {
        try {
            logger.info("Reading device... trying to acquire semaphore");
            deviceSemaphore.acquire();
            logger.info("Read Semaphore adquired!");

            // TODO replace for readAction
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.info("Semaphore released");
        }

        return null;
    }

}
