package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DepExeStatusSyncDaemonTask implements DepDaemonTask {
    private static final Logger logger = LoggerFactory.getLogger(DepExeStatusSyncDaemonTask.class);
    private DepService depService;

    public DepExeStatusSyncDaemonTask(DepService depService) {
        this.depService = depService;
    }


    @Override
    public void run() throws Exception {

        List<DepFlowInstance> instances = depService.getDepFlowInstancesNeedSync();
        for (int i = 0; i < instances.size(); i++) {
            DepFlowInstance depFlowInstance = instances.get(i);

            if (depFlowInstance.getStatus() != DepFlowInstanceStatus.SUBMITTED) {
                logger.warn("DepFlowInstance status not SUBMITTED,skip sync:{}", depFlowInstance);
                continue;
            }

            try {
                depService.syncExeStatus(depFlowInstance);
            } catch (Exception e) {
                logger.error("Error in syncExeStatus: " + depFlowInstance, e);
            }

        }

    }
}
