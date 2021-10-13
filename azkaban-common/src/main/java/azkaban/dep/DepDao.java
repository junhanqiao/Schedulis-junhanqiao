package azkaban.dep;

import azkaban.executor.ExecutableFlow;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface DepDao {
    List<DepFlowInstance> getDepFlowInstancesNeedSync() throws SQLException;

    int syncExeStat(DepFlowInstance depFlowInstance, DepFlowInstanceStatus newDepFlowStatus) throws SQLException;

    int initDependentInstance(DepFlowInstance instance) throws SQLException;

    int updateStatusForReadedIntance() throws SQLException;

    void newDepFlowInstance(int projectId, String flowId, LocalDateTime timeId, DepFlowInstanceStatus status, int executionId) throws SQLException;

    DepFlowInstance getOneDepFlowInstance(int projectId, String flowId, LocalDateTime timeId) throws SQLException;

    List<DepFlowInstance> getReadyedDepFlowInstances() throws SQLException;

    int updateFlowInstanceSubmitted(DepFlowInstance depFlowInstance, ExecutableFlow exflow) throws SQLException;
}
