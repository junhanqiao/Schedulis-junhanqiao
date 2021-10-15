package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DepManager {
    private static final Logger logger = LoggerFactory.getLogger(DepManager.class);
    private static final int runInterval=10*1000;
    private final DepService depService;
    private final DepDaemonServiceThread depExeStatusSyncThread;
    private final DepDaemonServiceThread depSchedulerThread;
    private final DepDaemonServiceThread depSubmitterThread;

    private final DepDaemonTask depExeStatusSyncTask;
    private final DepDaemonTask depSchedulerTask;
    private final DepDaemonTask depSubmitterTask;

    @Inject
    public DepManager(DepService depService) {
        this.depService = depService;
        this.depExeStatusSyncTask = new DepExeStatusSyncDaemonTask(depService);
        this.depExeStatusSyncThread = new DepDaemonServiceThread(this.depExeStatusSyncTask,"dep-exe-status-syncer",runInterval);

        this.depSchedulerTask=new DepSchedulerDaemonTask(depService);
        this.depSchedulerThread = new DepDaemonServiceThread(this.depSchedulerTask,"dep-scheduler",runInterval);

        this.depSubmitterTask=new DepSubitterDaemonTask(depService);
        this.depSubmitterThread = new DepDaemonServiceThread(this.depSubmitterTask,"dep-submitter",runInterval);
    }

    public void start() {
        this.depExeStatusSyncThread.start();
        this.depSchedulerThread.start();
        this.depSubmitterThread.start();
    }

    public DepService getDepService() {
        return depService;
    }
}
