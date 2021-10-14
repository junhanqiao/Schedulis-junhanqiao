package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepSchedulerDaemonTask implements DepDaemonTask {
    private static final Logger logger = LoggerFactory.getLogger(DepSchedulerDaemonTask.class);
    private DepService depService;

    public DepSchedulerDaemonTask(DepService depService) {
        this.depService = depService;
    }


    @Override
    public void run() throws Exception {

        this.depService.scheduleReadyService();

    }
}
