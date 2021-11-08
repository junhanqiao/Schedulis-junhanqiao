package azkaban.dep;

import azkaban.dep.bo.DepFlowRelation;
import azkaban.dep.bo.FlowNode;
import azkaban.dep.bo.ProjectBrief;
import azkaban.dep.vo.DepFlowInstanceDetail;
import azkaban.dep.vo.DepFlowRelationDetail;
import azkaban.executor.*;
import azkaban.flow.Flow;
import azkaban.flow.FlowUtils;
import azkaban.project.Project;
import azkaban.project.ProjectManager;
import azkaban.user.User;
import org.apache.commons.collections.CollectionUtils;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Singleton
public class DepService {
    private static final Logger logger = LoggerFactory.getLogger(DepService.class);
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

        int exeId = exflow.getExecutionId();

        DepFlowInstance instance = this.depDao.getOneDepFlowInstance(exflow.getProjectId(), exflow.getFlowId(), timeId);

        if (instance != null) {
            logger.warn("DepFlowInstance already exist,will redo,new execId:{},instance:{}", exeId, instance);
            int effectedRows = this.depDao.redoDepFlowInstanceForCron(instance, exeId);
            logger.info("DepFlowInstance redo {} rows, new execId:{},instance:{}", effectedRows, exeId, instance);
        } else {
            instance = new DepFlowInstance(
                    -1,
                    exflow.getProjectId(),
                    exflow.getFlowId(),
                    timeId,
                    DepFlowInstanceStatus.SUBMITTED,
                    exeId,
                    Instant.now(),
                    Instant.now());

            this.depDao.newDepFlowInstance(instance);

            logger.info("cron-triggered DepFlowInstance added :{}", instance);
        }

    }

    public int redoDepFlowInstance(DepFlowInstance instance) throws SQLException {
        int effectedRows = this.depDao.updateStatusForRedoedIntance(instance);
        logger.info("DepFlowInstance redo {} rows, instance:{}", effectedRows, instance);
        return effectedRows;

    }

    public List<DepFlowInstance> getDepFlowInstancesNeedSync() throws SQLException {
        return this.depDao.getDepFlowInstancesNeedSync();
    }

    public void syncExeStatus(DepFlowInstance depFlowInstance) throws ExecutorManagerException, SQLException {
        logger.debug("syncing exe stat for:{}", depFlowInstance);

        ExecutableFlow executableFlow = this.executionFlowDao.fetchExecutableFlow(depFlowInstance.getExecId());
        Status status = executableFlow.getStatus();

        logger.debug("executableFlow status:{}, {}", status, depFlowInstance);

        if (Status.isStatusFinished(status)) {
            DepFlowInstanceStatus newDepFlowStatus = Status.isSucceeded(status) ? DepFlowInstanceStatus.SUCCESS : DepFlowInstanceStatus.FAILED;
            this.depDao.syncExeStat(depFlowInstance, newDepFlowStatus);

            logger.info("flow execution finished, status is:{} ,change depFlowInstance status to :{},{}", status, newDepFlowStatus, depFlowInstance);

            if (newDepFlowStatus == DepFlowInstanceStatus.SUCCESS) {
                int rowsInsert = this.depDao.initDependentInstance(depFlowInstance);
                logger.info("{} dependent instances init success for:{}", rowsInsert, depFlowInstance);
            }
        }
    }

    public int scheduleReadyInstance() throws SQLException {
        int effectedRows = this.depDao.updateStatusForReadyedIntance();
        return effectedRows;
    }

    public int scheduleReadyInstanceNoFather() throws SQLException {
        int effectedRows = this.depDao.updateStatusForInstanceNoFather();
        return effectedRows;
    }

    public List<DepFlowInstance> getReadyDepFlowInstances() throws SQLException {
        return this.depDao.getReadyDepFlowInstances();
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

        logger.info("submit flow execution finish,message:{}", message);

        try {
            this.depDao.updateFlowInstanceSubmitted(depFlowInstance, exflow);
            logger.info("DepFlowInstance changed to status SUBMITTED success,exeId:{},{}", exflow.getExecutionId(), depFlowInstance);
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

    public List<DepFlowRelationDetail> searchFlowRelation(Integer depedProjectId, String depedFlowId, Integer projectId, String flowId, String userName, int pageNum, int pageSize) throws SQLException {
        return depDao.searchFlowRelation(depedProjectId, depedFlowId, projectId, flowId, userName, pageNum, pageSize);
    }

    public int searchFlowRelationCount(Integer depedProjectId, String depedFlowId, Integer projectId, String flowId, String userName) throws SQLException {
        return depDao.searchFlowRelationCount(depedProjectId, depedFlowId, projectId, flowId, userName);
    }

    public void newDepFlowRelation(DepFlowRelation depFlowRelation) throws SQLException, CycleDepRelationException {
        checkCycleDepRelation(depFlowRelation);
        Instant nowInstant = Instant.now();
        depFlowRelation.setCreateTime(nowInstant);
        depFlowRelation.setModifyTime(nowInstant);
        this.depDao.newDepFlowRelation(depFlowRelation);

    }

    public DepFlowRelation getDepFlowRelationByLogicKey(DepFlowRelation condition) throws SQLException {
        DepFlowRelation result = this.depDao.getDepFlowRelationByKey(condition);
        return result;
    }

    public DepFlowRelation getDepFlowRelationByKey(int id) throws SQLException {
        DepFlowRelation result = this.depDao.getDepFlowRelationByKey(id);
        return result;
    }

    public List<ProjectBrief> searchProjectByName(String searchText) {
        List<ProjectBrief> result = new ArrayList<>();
        List<Project> projects = this.projectManager.getProjectsByRegex(searchText);

        for (Project project : projects) {
            result.add(new ProjectBrief(project.getId(), project.getName()));
        }

        return result;

    }

    public List<ProjectBrief> searchUserProjectByName(String searchText, User user) {
        List<ProjectBrief> result = new ArrayList<>();
        List<Project> projects = this.projectManager.getUserPersonProjectsByRegex(user, searchText, null);

        for (Project project : projects) {
            result.add(new ProjectBrief(project.getId(), project.getName()));
        }

        return result;

    }

    public List<String> getFlowsByProject(int projectId) throws Exception {
        Project project = this.projectManager.getProject(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found,id:" + projectId);
        }

        Set<String> flows = project.getFlowMap().keySet();
        return new ArrayList<>(flows);

    }

    public int deleteFlowRelationById(int id) throws SQLException {
        return this.depDao.deleteFlowRelationById(id);
    }

    public List<DepFlowInstanceDetail> searchFlowInstance(Integer projectId, String flowId, List<Integer> statuses, String startTimeId, String endTimeId, String userName, int pageNum, int pageSize) throws SQLException {
        return depDao.searchFlowInstance(projectId, flowId, statuses, startTimeId, endTimeId, userName, pageNum, pageSize);
    }

    public int searchFlowInstanceCount(Integer projectId, String flowId, List<Integer> statuses, String startTimeId, String endTimeId, String userName) throws SQLException {
        return depDao.searchFlowInstanceCount(projectId, flowId, statuses, startTimeId, endTimeId, userName);
    }

    public DepFlowInstance getDepFlowInstance(int id) throws SQLException {
        return depDao.getDepFlowInstanceByKey(id);
    }

    /**
     * @param instance
     * @throws Exception
     */
    public void checkCanRedoFlowInstance(DepFlowInstance instance) throws Exception {
        DepFlowInstance existedInstance = this.getDepFlowInstance(instance.getId());
        if (existedInstance == null) {
            throw new Exception("instance not exist,can`t redo ");
        }
    }

    public void checkCycleDepRelation(DepFlowRelation relation) throws CycleDepRelationException, SQLException {
        DefaultDirectedGraph<FlowNode, DefaultEdge> graph = this.getFlowRelationGraph();
        FlowNode src = new FlowNode(relation.getDependedProjectId(), relation.getDependedFlowId());
        FlowNode dst = new FlowNode(relation.getProjectId(), relation.getFlowId());
        Graphs.addEdgeWithVertices(graph, src, dst);

        CycleDetector<FlowNode, DefaultEdge> cycleDetector = new CycleDetector<>(graph);

        if (cycleDetector.detectCycles()) {
            Set<FlowNode> cycleNodes = cycleDetector.findCycles();
            throw new CycleDepRelationException("Cycle dep not allowed:" + cycleNodes);
        }

    }

    public DefaultDirectedGraph<FlowNode, DefaultEdge> getFlowRelationGraph() throws SQLException {
        List<DepFlowRelation> relations = this.depDao.getAllFlowRelation();

        DefaultDirectedGraph<FlowNode, DefaultEdge> graph = new DefaultDirectedGraph<FlowNode, DefaultEdge>(DefaultEdge.class);

        if (CollectionUtils.isNotEmpty(relations)) {
            for (DepFlowRelation relation : relations) {
                FlowNode src = new FlowNode(relation.getDependedProjectId(), relation.getDependedFlowId());
                FlowNode dst = new FlowNode(relation.getProjectId(), relation.getFlowId());
                Graphs.addEdgeWithVertices(graph, src, dst);
            }
        }
        return graph;
    }
}
