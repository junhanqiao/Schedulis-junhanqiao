package azkaban.dep;

import azkaban.executor.*;
import azkaban.flow.Flow;
import azkaban.flow.FlowUtils;
import azkaban.project.Project;
import azkaban.project.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class DepService {
    private static final Logger logger = LoggerFactory.getLogger(DepService.class);
    public static final DateTimeFormatter timeIdFormater=DateTimeFormatter.ofPattern("");
    private final DepDao depDao;
    private final ExecutionFlowDao executionFlowDao;
    private final ProjectManager projectManager;
    private ExecutorManagerAdapter executorManagerAdapter;

    @Inject
    public DepService(DepDao depDao, ExecutionFlowDao executionFlowDao, ProjectManager projectManager, ExecutorManagerAdapter executorManagerAdapter) {
        this.depDao = depDao;
        this.executionFlowDao = executionFlowDao;
        this.projectManager = projectManager;
        this.executorManagerAdapter = executorManagerAdapter;
    }

    /**
     * called by cron trigger
     *
     * @param exflow
     */
    public void newCronFlowExeSubmited(ExecutableFlow exflow) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeId = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0);

        DepFlowInstance instance = this.depDao.getOneDepFlowInstance(exflow.getProjectId(), exflow.getFlowId(), timeId);

        if (instance != null) {
            logger.error("flow instance already exist:" + instance);

        } else {
            this.depDao.newDepFlowInstance(exflow.getProjectId(), exflow.getFlowId(), timeId, DepFlowInstanceStatus.SUBMITED, exflow.getExecutionId());
        }

    }

    public List<DepFlowInstance> getDepFlowInstancesNeedSync() throws SQLException {
        return this.depDao.getDepFlowInstancesNeedSync();
    }

    public void syncExeStat(DepFlowInstance depFlowInstance) throws ExecutorManagerException, SQLException {
        ExecutableFlow executableFlow = this.executionFlowDao.fetchExecutableFlow(depFlowInstance.getExecId());

        Status status = executableFlow.getStatus();
        DepFlowInstanceStatus depFlowInstanceStatus = depFlowInstance.getStatus();

        if (Status.isStatusFinished(status) && depFlowInstanceStatus == DepFlowInstanceStatus.SUBMITED) {
            DepFlowInstanceStatus newDepFlowStatus = Status.isSucceeded(status) ? DepFlowInstanceStatus.SUCCESS : DepFlowInstanceStatus.FAILED;
            this.depDao.syncExeStat(depFlowInstance, newDepFlowStatus);
            if (newDepFlowStatus == DepFlowInstanceStatus.SUCCESS) {
                this.depDao.initDependentInstance(depFlowInstance);
            }
        } else {

        }
    }

    public void scheduleReadyService() throws SQLException {
        this.depDao.updateStatusForReadedIntance();
    }

    public List<DepFlowInstance> getReadyedDepFlowInstances() throws SQLException {
        return this.depDao.getReadyedDepFlowInstances();
    }

    public void submitExecution(DepFlowInstance depFlowInstance) throws ExecutorManagerException {

        int projectId = depFlowInstance.getProjectId();
        String flowId = depFlowInstance.getFlowId();


        final Project project = this.projectManager.getProject(projectId);
        final Flow flow = project.getFlow(flowId);
        final ExecutableFlow exflow = FlowUtils.createExecutableFlow(project, flow);

        final String userId = project.getCreateUser();
        //获取项目默认代理用户
        Set<String> proxyUserSet = project.getProxyUsers();
        //设置用户代理用户
        proxyUserSet.add(userId);
        //设置代理用户
        exflow.addAllProxyUsers(proxyUserSet);

        // set paramter
        Map<String, String> flowParameters = this.calcFlowParameters(depFlowInstance);
        exflow.getExecutionOptions().getFlowParameters().putAll(flowParameters);

        final String message = this.executorManagerAdapter.submitExecutableFlow(exflow, userId);

        logger.info(message);

        try {
            this.depDao.updateFlowInstanceSubmitted(depFlowInstance, exflow);
        } catch (Exception e) {
            String errMsg = String.format("depService submitExecution resolve failed:projectId %d,projectName %s,flowId:%s,exec_id:%d", exflow.getProjectId(), exflow.getProjectName(), exflow.getFlowId(), exflow.getExecutionId());
            logger.error(errMsg, e);
        }

    }

    public Map<String, String> calcFlowParameters(DepFlowInstance instance) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        LocalDateTime timeId = instance.getTimeId();
        String wgby_day = timeId.format(DateTimeFormatter.BASIC_ISO_DATE);
        String wgby_year = wgby_day.substring(0, 4);
        String wgby_timeid = timeId.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        parameters.put("wgby_day", wgby_day);
        parameters.put("wgby_year", wgby_year);
        parameters.put("wgby_timeid", wgby_timeid);
        return parameters;
    }
}
