package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DepSubmitterThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DepSubmitterThread.class);
    private DepService depService;
    private boolean shutdown = false;

    public DepSubmitterThread(DepService depService) {
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
                List<DepFlowInstance> instances = depService.getReadyedDepFlowInstances();
                for (int i = 0; i < instances.size(); i++) {
                    DepFlowInstance depFlowInstance = instances.get(i);
                    try {
                        depService.submitExecution(depFlowInstance);
                    } catch (Exception e) {
                        logger.error("Error in submitExecution " + depFlowInstance, e);
                    }


                }

            } catch (Exception e) {
                logger.error("Error in DepSubmitterThread", e);

            }
            try {
                this.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Error in DepSubmitterThread", e);
            }
        }
    }
}
