package azkaban.dep;

import azkaban.db.DatabaseOperator;
import azkaban.db.SQLTransaction;
import azkaban.executor.ExecutableFlow;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class JdbcDepDaoImpl implements DepDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDepDaoImpl.class);
    private final DatabaseOperator dbOperator;
    static final String UPDATE_DEP_FLOW_INSTANCES_STATUS = "UPDATE dep_flow_instance set status=?,modify_time=now() where id=? and status=?";
    static final String INSERT_DEPENDENT_INSTANCE = "insert into dep_flow_instance(project_id,flow_id,time_id,status,create_time,modify_time)\n" +
            " select r.project_id,r.flow_id ,? as time_id,?,now(),now() from  dep_flow_relation r  where r.depended_project_id=? and r.depended_flow_id=?  \n" +
            " ON DUPLICATE KEY update status=?,modify_time=now()";
    static final String INSERT_DEP_FLOW_INSTANCE ="insert into dep_flow_instance(project_id,flow_id,time_id,status,exec_id,create_time,modify_time) values (?,?,?,?,?,?,?)";

    static final String UPDATE_STATUS_FOR_READYED_INSTANCE = "update dep_flow_instance i,\n" +
            "(select i.id \n" +
            "from dep_flow_instance i join dep_flow_relation  r on (i.project_id =r.project_id  and i.flow_id =r.flow_id ) \n" +
            "join dep_flow_instance id on (id.project_id =r.depended_project_id  and id.flow_id =r.depended_flow_id  and id.time_id =i.time_id )\n" +
            "where i.status = ?\n" +
            "group by i.id\n" +
            "HAVING  count(if(id.status=? ,1,null))=count(*)\n" +
            ") as ready_instance\n" +
            "set i.status=?\n" +
            "where i.id=ready_instance.id";
    static final String GET_DEP_FLOW_INSTANCE_BY_LOGIC_KEY="select * from dep_flow_instance where project_id=? and flow_id=? and time_id=?";

    static final String QUERY_DEP_FLOW_INSTANCE_BY_STATUS="select * from dep_flow_instance where status=? limit ?";
    static final String UPATE_SUBMITTED_DEP_INSTANCE="update dep_flow_instance set status=?,exec_id=?,modify_time=now() where id=? and status= ?";
    static final String UPATE_REDOED_DEP_INSTANCE="update dep_flow_instance set status=?,modify_time=now() where id=? and status= ? and modify_time=?";

    @Inject
    public JdbcDepDaoImpl(DatabaseOperator databaseOperator) {
        this.dbOperator = databaseOperator;
    }

    public List<DepFlowInstance> getDepFlowInstancesNeedSync() throws SQLException {
        List<DepFlowInstance> result = this.dbOperator.query(FetchDepFlowInstanceHandler.FETCH_DEP_FLOW_INSTANCES_NEED_SYNC, new FetchDepFlowInstanceHandler(), DepFlowInstanceStatus.SUBMITTED.getValue());
        return result;
    }

    public int syncExeStat(DepFlowInstance depFlowInstance, DepFlowInstanceStatus newDepFlowStatus) throws SQLException {
        int effectRows = this.dbOperator.update(UPDATE_DEP_FLOW_INSTANCES_STATUS, newDepFlowStatus.getValue(), depFlowInstance.getId(), depFlowInstance.getStatus().getValue());
        return effectRows;
    }

    public int initDependentInstance(DepFlowInstance instance) throws SQLException {
        int effectRows = this.dbOperator.update(INSERT_DEPENDENT_INSTANCE, instance.getTimeId(), DepFlowInstanceStatus.INIT.getValue(), instance.getProjectId(), instance.getFlowId(), DepFlowInstanceStatus.INIT.getValue());
        return effectRows;
    }

    public int updateStatusForReadyedIntance() throws SQLException {
        int efectRows = this.dbOperator.update(UPDATE_STATUS_FOR_READYED_INSTANCE, DepFlowInstanceStatus.INIT.getValue(), DepFlowInstanceStatus.SUCCESS.getValue(),DepFlowInstanceStatus.READY.getValue());
        return efectRows;
    }


    public void newDepFlowInstance(DepFlowInstance instance) throws SQLException {

        final SQLTransaction<Long> insertAndGetLastID = transOperator -> {
            transOperator.update(INSERT_DEP_FLOW_INSTANCE,
                    instance.getProjectId(),
                    instance.getFlowId(),
                    instance.getTimeId(),
                    instance.getStatus().getValue(),
                    instance.getExecId(),
                    Timestamp.from(instance.getCreateTime()),
                    Timestamp.from(instance.getModifyTime()));

            transOperator.getConnection().commit();
            return transOperator.getLastInsertId();
        };

        final long id = this.dbOperator.transaction(insertAndGetLastID);
        instance.setId((int) id);

    }

    public DepFlowInstance getOneDepFlowInstance(int projectId, String flowId, LocalDateTime timeId) throws SQLException {
        List<DepFlowInstance> instances = this.dbOperator.query(GET_DEP_FLOW_INSTANCE_BY_LOGIC_KEY, new FetchDepFlowInstanceHandler(), projectId, flowId, timeId);
        DepFlowInstance result = null;
        if (instances.size() == 1) {
            result = instances.get(0);
        }
        return result;
    }

    public List<DepFlowInstance> getReadyDepFlowInstances() throws SQLException {
        return this.dbOperator.query(QUERY_DEP_FLOW_INSTANCE_BY_STATUS,new FetchDepFlowInstanceHandler(),DepFlowInstanceStatus.READY.getValue(),1000);
    }


    public int updateFlowInstanceSubmitted(DepFlowInstance depFlowInstance, ExecutableFlow exflow) throws SQLException {
        int effectRows = this.dbOperator.update(UPATE_SUBMITTED_DEP_INSTANCE, DepFlowInstanceStatus.SUBMITTED.getValue(), exflow.getExecutionId(), depFlowInstance.getId(),DepFlowInstanceStatus.READY.getValue());
        return effectRows;

    }

    public int updateStatusForRedoedIntance(DepFlowInstance instance) throws SQLException {
        int effectRows = this.dbOperator.update(UPATE_REDOED_DEP_INSTANCE, DepFlowInstanceStatus.INIT.getValue(), instance.getId(),instance.getStatus().getValue(),Timestamp.from(instance.getModifyTime()));
        return effectRows;
    }

    public int redoDepFlowInstanceForCron(DepFlowInstance instance) throws SQLException {
        int effectRows = this.dbOperator.update(UPATE_REDOED_DEP_INSTANCE, DepFlowInstanceStatus.SUBMITTED.getValue(), instance.getId(),instance.getStatus().getValue(),Timestamp.from(instance.getModifyTime()));
        return effectRows;
    }

    public static class FetchDepFlowInstanceHandler implements
            ResultSetHandler<List<
                    DepFlowInstance>> {
        static final String FETCH_DEP_FLOW_INSTANCES_NEED_SYNC = "select * from dep_flow_instance where status = ? limit 1000";

        @Override
        public List<DepFlowInstance> handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return Collections.emptyList();
            }

            final List<DepFlowInstance> instances = new ArrayList<>();
            do {
                final int id = rs.getInt("id");
                final int projectId = rs.getInt("project_id");
                final String flowId = rs.getString("flow_id");
                final LocalDateTime timeId = rs.getTimestamp("time_id").toLocalDateTime();
                final DepFlowInstanceStatus status = DepFlowInstanceStatus.fromInt(rs.getInt("status"));
                final int execId = rs.getInt("exec_id");
                final Instant createTime = rs.getTimestamp("create_time").toInstant();
                final Instant modifyTime = rs.getTimestamp("modify_time").toInstant();
                DepFlowInstance instance = new DepFlowInstance(id, projectId, flowId, timeId, status, execId, createTime, modifyTime);
                instances.add(instance);
            } while (rs.next());

            return instances;
        }
    }

}
