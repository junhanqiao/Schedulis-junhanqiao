package azkaban.dep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DepManager {
    private static final Logger logger = LoggerFactory.getLogger(DepManager.class);

    private final DepService depService;
    private final DepExeStatSyncThread depExeStatSyncThread;
    private final DepShedulerThread depShedulerThread;
    private final DepSubmitterThread depSubmitterThread;

    @Inject
    public DepManager(DepService depService) {
        this.depService = depService;
        this.depExeStatSyncThread = new DepExeStatSyncThread(depService);
        this.depExeStatSyncThread.setName("dep-exe-stat-sync");

        this.depShedulerThread = new DepShedulerThread(depService);
        this.depShedulerThread.setName("dep-scheduler");
        this.depSubmitterThread = new DepSubmitterThread(depService);
        this.depSubmitterThread.setName("dep-submmiter");
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
