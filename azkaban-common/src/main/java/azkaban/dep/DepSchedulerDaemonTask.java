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

        int effectedRows = this.depService.scheduleReadyInstance();

        if (effectedRows > 0) {
            logger.info("{} instances becamed ready", effectedRows);
        }

        int effecdRowsNoFather = this.depService.scheduleReadyInstanceNoFather();
        if (effectedRows > 0) {
            logger.info("{} no father instances becamed ready", effecdRowsNoFather);
        }
    }
}
