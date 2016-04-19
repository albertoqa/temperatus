package temperatus.device.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author aquesada
 */
@Component
@Scope("prototype") // TODO???
public class DeviceReadTask extends DeviceTask {

    private static Logger logger = LoggerFactory.getLogger(DeviceReadTask.class.getName());

    @Override
    public File call() throws Exception {
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

    private File readDataAndSaveToFile() {


        return null;
    }

}
