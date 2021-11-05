package azkaban.dep;

import azkaban.dep.bo.DepFlowRelation;
import azkaban.dep.vo.DepFlowInstanceDetail;
import azkaban.dep.vo.DepFlowRelationDetail;
import azkaban.executor.ExecutableFlow;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface DepDao {
    List<DepFlowInstance> getDepFlowInstancesNeedSync() throws SQLException;

    int syncExeStat(DepFlowInstance depFlowInstance, DepFlowInstanceStatus newDepFlowStatus) throws SQLException;

    int initDependentInstance(DepFlowInstance instance) throws SQLException;

    int updateStatusForReadyedIntance() throws SQLException;

    void newDepFlowInstance(DepFlowInstance instance) throws SQLException;

    DepFlowInstance getOneDepFlowInstance(int projectId, String flowId, LocalDateTime timeId) throws SQLException;

    List<DepFlowInstance> getReadyDepFlowInstances() throws SQLException;

    int updateFlowInstanceSubmitted(DepFlowInstance depFlowInstance, ExecutableFlow exflow) throws SQLException;

    int updateStatusForRedoedIntance(DepFlowInstance instance) throws SQLException;

    int redoDepFlowInstanceForCron(DepFlowInstance instance, int executionId) throws SQLException;

    //for ui
    List<DepFlowRelationDetail> searchFlowRelation(Integer depedProjectId, String depedFlowId, Integer projectId, String flowId,String username,int pageNum,int pageSize) throws SQLException;

    void newDepFlowRelation(DepFlowRelation depFlowRelation) throws SQLException;
    DepFlowRelation getDepFlowRelationByKey(DepFlowRelation depFlowRelation) throws  SQLException;
    DepFlowRelation getDepFlowRelationByKey(int id) throws  SQLException;

    int deleteFlowRelationById(int id) throws SQLException;

    int searchFlowRelationCount(Integer depedProjectId, String depedFlowId, Integer projectId, String flowId, String userName) throws SQLException;

    List<DepFlowInstanceDetail> searchFlowInstance(Integer projectId, String flowId, List<Integer> statuses, String startTimeId, String endTimeId, String userName, int pageNum, int pageSize) throws  SQLException;

    int searchFlowInstanceCount(Integer projectId, String flowId, List<Integer> statuses, String startTimeId, String endTimeId, String userName) throws SQLException;
}
