package azkaban.dep;

import azkaban.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DepManager {
    private static final Logger logger = LoggerFactory.getLogger(DepManager.class);
    private static final int runInterval = 10 * 1000;
    private final Props props;
    private boolean wgby_dep_enable;

    private DepService depService;
    private DepDaemonServiceThread depExeStatusSyncThread;
    private DepDaemonServiceThread depSchedulerThread;
    private DepDaemonServiceThread depSubmitterThread;

    private DepDaemonTask depExeStatusSyncTask;
    private DepDaemonTask depSchedulerTask;
    private DepDaemonTask depSubmitterTask;


    @Inject
    public DepManager(Props props, DepService depService) {
        this.props = props;
        this.wgby_dep_enable = props.getBoolean("wgby.dep.enable", true);
        int wgby_status_syncer_interval = props.getInt("wgby.dep.thread.syncer.interval", runInterval);
        int wgby_scheduler_interval = props.getInt("wgby.dep.thread.scheduler.interval", runInterval);
        int wgby_submitter_interval = props.getInt("wgby.dep.thread.submitter.interval", runInterval);

        if (this.wgby_dep_enable) {
            this.depService = depService;
            this.depExeStatusSyncTask = new DepExeStatusSyncDaemonTask(depService);
            this.depExeStatusSyncThread = new DepDaemonServiceThread(this.depExeStatusSyncTask, "dep-exe-status-syncer", wgby_status_syncer_interval);

            this.depSchedulerTask = new DepSchedulerDaemonTask(depService);
            this.depSchedulerThread = new DepDaemonServiceThread(this.depSchedulerTask, "dep-scheduler", wgby_scheduler_interval);

            this.depSubmitterTask = new DepSubitterDaemonTask(depService);
            this.depSubmitterThread = new DepDaemonServiceThread(this.depSubmitterTask, "dep-submitter", wgby_submitter_interval);
        } else {
            logger.warn("not enable,skip");
        }
    }

    public void start() {
        if (this.wgby_dep_enable) {
            this.depExeStatusSyncThread.start();
            this.depSchedulerThread.start();
            this.depSubmitterThread.start();
        } else {
            logger.warn("not enable,skip");
        }
    }

    public DepService getDepService() {
        return depService;
    }
}
