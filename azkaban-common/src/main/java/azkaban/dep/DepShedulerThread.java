package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepShedulerThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DepShedulerThread.class);
    private DepService depService;
    private boolean shutdown = false;

    public DepShedulerThread(DepService depService) {
        this.depService = depService;
    }

    public void shutdown() {
        this.shutdown = true;
        this.interrupt();
    }

    @Override
    public void run() {
        while (!this.shutdown) {

            try {
                this.depService.scheduleReadyService();

            } catch (Exception e) {
                logger.error("Error occured", e);

            }
            try {
                this.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Error occured", e);
            }
        }
    }
}
