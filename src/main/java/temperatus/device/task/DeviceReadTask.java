package temperatus.device.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author aquesada
 */
@Component
@Scope("prototype")
public class DeviceReadTask extends DeviceTask {

    private static Logger logger = LoggerFactory.getLogger(DeviceReadTask.class.getName());

    @Override
    public Object call() throws Exception {
        try {
            logger.info("Reading device...");
            deviceSemaphore.acquire();
            logger.debug("Read Semaphore adquired!");

            // TODO replace for readAction
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Read Semaphore released");
        }

        return null;
    }

}
