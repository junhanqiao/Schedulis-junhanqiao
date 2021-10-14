package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DepExeStatSyncDaemonTask implements DepDaemonTask {
    private static final Logger logger = LoggerFactory.getLogger(DepExeStatSyncDaemonTask.class);
    private DepService depService;

    public DepExeStatSyncDaemonTask(DepService depService) {
        this.depService = depService;
    }


    @Override
    public void run() throws Exception {

        List<DepFlowInstance> instances = depService.getDepFlowInstancesNeedSync();
        for (int i = 0; i < instances.size(); i++) {
            DepFlowInstance depFlowInstance = instances.get(i);
            try {
                depService.syncExeStat(depFlowInstance);
            } catch (Exception e) {
                logger.error("Error in syncExeStat: " + depFlowInstance, e);
            }

        }

    }
}
