package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepDaemonServiceThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DepDaemonServiceThread.class);
    private DepDaemonTask task;
    private boolean shutdown = false;
    private int runInterval;
    private final String errMsg;

    public DepDaemonServiceThread(DepDaemonTask task, String threadName, int runInterval) {
        super(threadName);
        this.task = task;
        this.runInterval = runInterval;
        this.errMsg = "Error occured in thread " + threadName;
    }

    public void shutdown() {
        this.shutdown = true;
        this.interrupt();
    }

    @Override
    public void run() {
        logger.info("Thread {}  start running", this.getName());
        while (!this.shutdown) {

            try {
                this.task.run();

            } catch (Exception e) {
                logger.error(this.errMsg, e);

            }
            try {
                this.sleep(this.runInterval);
            } catch (InterruptedException e) {
                logger.error(this.errMsg, e);
            }
        }
        logger.warn(this.getName() + " quit ");
    }
}
