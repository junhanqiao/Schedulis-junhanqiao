package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DepExeStatSyncThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DepExeStatSyncThread.class);
    private DepService depService;
    private boolean shutdown = false;

    public DepExeStatSyncThread(DepService depService) {
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
                List<DepFlowInstance> instances = depService.getDepFlowInstancesNeedSync();
                for (int i = 0; i < instances.size(); i++) {
                    DepFlowInstance depFlowInstance = instances.get(i);
                    try {
                        depService.syncExeStat(depFlowInstance);
                    } catch (Exception e) {
                        logger.error("Error in syncExeStat " + depFlowInstance, e);
                    }


                }

            } catch (Exception e) {
                logger.error("Error in DepExeStatSyncThread", e);

            }
            try {
                this.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Error in DepExeStatSyncThread", e);
            }
        }
    }
}
