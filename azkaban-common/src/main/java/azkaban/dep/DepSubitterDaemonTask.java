package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DepSubitterDaemonTask implements DepDaemonTask {
    private static final Logger logger = LoggerFactory.getLogger(DepSubitterDaemonTask.class);
    private DepService depService;

    public DepSubitterDaemonTask(DepService depService) {
        this.depService = depService;
    }


    @Override
    public void run() throws Exception {
        List<DepFlowInstance> instances = depService.getReadyDepFlowInstances();
        for (int i = 0; i < instances.size(); i++) {
            DepFlowInstance depFlowInstance = instances.get(i);

            if (depFlowInstance.getStatus() != DepFlowInstanceStatus.READY) {
                logger.warn("DepFlowInstance status not READY,skip submit to excute:{}", depFlowInstance);
                continue;
            }
            try {
                depService.submitExecution(depFlowInstance);
            } catch (Exception e) {
                logger.error("Error in submitExecution " + depFlowInstance, e);
            }

        }

    }
}
