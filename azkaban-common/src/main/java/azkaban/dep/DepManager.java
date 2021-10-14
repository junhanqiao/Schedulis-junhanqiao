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
    private final DepDaemonServiceThread depExeStatSyncThread;
    private final DepDaemonServiceThread depShedulerThread;
    private final DepDaemonServiceThread depSubmitterThread;

    private final DepDaemonTask depExeStatSyncTask;
    private final DepDaemonTask depSchedulerTask;
    private final DepDaemonTask depSubmitterTask;

    @Inject
    public DepManager(DepService depService) {
        this.depService = depService;
        this.depExeStatSyncTask = new DepExeStatSyncDaemonTask(depService);
        this.depExeStatSyncThread = new DepDaemonServiceThread(this.depExeStatSyncTask,"dep-exe-stat-sync",runInterval);

        this.depSchedulerTask=new DepSchedulerDaemonTask(depService);
        this.depShedulerThread = new DepDaemonServiceThread(this.depSchedulerTask,"dep-scheduler",runInterval);

        this.depSubmitterTask=new DepSubitterDaemonTask(depService);
        this.depSubmitterThread = new DepDaemonServiceThread(this.depSubmitterTask,"dep-submmiter",runInterval);
    }

    public void start() {
        this.depExeStatSyncThread.start();
        this.depShedulerThread.start();
        this.depSubmitterThread.start();
    }

    public DepService getDepService() {
        return depService;
    }
}
