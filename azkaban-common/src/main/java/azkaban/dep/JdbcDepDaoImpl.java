package azkaban.dep;

import azkaban.db.DatabaseOperator;
import azkaban.db.SQLTransaction;
import azkaban.dep.bo.DepFlowRelation;
import azkaban.dep.vo.DepFlowRelationDetail;
import azkaban.executor.ExecutableFlow;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
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
    static final String INSERT_DEP_FLOW_INSTANCE = "insert into dep_flow_instance(project_id,flow_id,time_id,status,exec_id,create_time,modify_time) values (?,?,?,?,?,?,?)";

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
    static final String GET_DEP_FLOW_INSTANCE_BY_LOGIC_KEY = "select * from dep_flow_instance where project_id=? and flow_id=? and time_id=?";

    static final String QUERY_DEP_FLOW_INSTANCE_BY_STATUS = "select * from dep_flow_instance where status=? limit ?";
    static final String UPATE_SUBMITTED_DEP_INSTANCE = "update dep_flow_instance set status=?,exec_id=?,modify_time=now() where id=? and status= ?";
    static final String UPATE_REDOED_DEP_INSTANCE = "update dep_flow_instance set status=?,exec_id=?,modify_time=now() where id=? and status= ? and modify_time=?";
    static final String INTERT_DEP_FLOW_RELATION = "INSERT INTO schedulis.dep_flow_relation(depended_project_id, depended_flow_id, project_id, flow_id, create_user,create_time, modify_time)VALUES(?, ?, ?, ?, ?, ?,?)";

    static final String QUERY_DEP_FLOW_RELATION_BY_LOGIC_KEY = "SELECT * FROM dep_flow_relation WHERE depended_project_id=? AND  depended_flow_id = ? AND   project_id = ? AND  flow_id = ?";
    static final String QUERY_DEP_FLOW_RELATION_BY_KEY = "SELECT * FROM dep_flow_relation WHERE id=?";
    static final String DELETE_DEP_FLOW_RELATION_BY_KEY = "DELETE FROM dep_flow_relation WHERE id=?";
    static final String SEARCH_DEP_FLOW_RELATION_FROM_SQL = "from dep_flow_relation r join projects depedProject on (r.depended_project_id =depedProject .id ) JOIN projects p on (r.project_id =p.id ) ";

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
        int efectRows = this.dbOperator.update(UPDATE_STATUS_FOR_READYED_INSTANCE, DepFlowInstanceStatus.INIT.getValue(), DepFlowInstanceStatus.SUCCESS.getValue(), DepFlowInstanceStatus.READY.getValue());
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
        return this.dbOperator.query(QUERY_DEP_FLOW_INSTANCE_BY_STATUS, new FetchDepFlowInstanceHandler(), DepFlowInstanceStatus.READY.getValue(), 1000);
    }


    public int updateFlowInstanceSubmitted(DepFlowInstance depFlowInstance, ExecutableFlow exflow) throws SQLException {
        int effectRows = this.dbOperator.update(UPATE_SUBMITTED_DEP_INSTANCE, DepFlowInstanceStatus.SUBMITTED.getValue(), exflow.getExecutionId(), depFlowInstance.getId(), DepFlowInstanceStatus.READY.getValue());
        return effectRows;

    }

    public int updateStatusForRedoedIntance(DepFlowInstance instance) throws SQLException {
        int effectRows = this.dbOperator.update(UPATE_REDOED_DEP_INSTANCE, DepFlowInstanceStatus.INIT.getValue(), instance.getId(), instance.getStatus().getValue(), Timestamp.from(instance.getModifyTime()));
        return effectRows;
    }

    public int redoDepFlowInstanceForCron(DepFlowInstance instance, int executionId) throws SQLException {
        int effectRows = this.dbOperator.update(UPATE_REDOED_DEP_INSTANCE, DepFlowInstanceStatus.SUBMITTED.getValue(), executionId, instance.getId(), instance.getStatus().getValue(), Timestamp.from(instance.getModifyTime()));
        return effectRows;
    }


    public void newDepFlowRelation(DepFlowRelation depFlowRelation) throws SQLException {

        final SQLTransaction<Long> insertAndGetLastID = transOperator -> {
            transOperator.update(INTERT_DEP_FLOW_RELATION,
                    depFlowRelation.getDependedProjectId(),
                    depFlowRelation.getDependedFlowId(),
                    depFlowRelation.getProjectId(),
                    depFlowRelation.getFlowId(),
                    depFlowRelation.getCreateUser(),
                    Timestamp.from(depFlowRelation.getCreateTime()),
                    Timestamp.from(depFlowRelation.getModifyTime()));

            transOperator.getConnection().commit();
            return transOperator.getLastInsertId();
        };

        final long id = this.dbOperator.transaction(insertAndGetLastID);
        depFlowRelation.setId((int) id);
    }

    public DepFlowRelation getDepFlowRelationByKey(DepFlowRelation depFlowRelation) throws SQLException {
        List<DepFlowRelation> result = this.dbOperator.query(QUERY_DEP_FLOW_RELATION_BY_LOGIC_KEY, new FetchDepFlowRelationHandler(), depFlowRelation.getDependedProjectId(), depFlowRelation.getDependedFlowId(), depFlowRelation.getProjectId(), depFlowRelation.getFlowId());
        DepFlowRelation relation = CollectionUtils.isEmpty(result) ? null : result.get(0);
        return relation;
    }

    public DepFlowRelation getDepFlowRelationByKey(int id) throws SQLException {
        List<DepFlowRelation> result = this.dbOperator.query(QUERY_DEP_FLOW_RELATION_BY_KEY, new FetchDepFlowRelationHandler(), id);
        DepFlowRelation relation = CollectionUtils.isEmpty(result) ? null : result.get(0);
        return relation;
    }

    @Override
    public int deleteFlowRelationById(int id) throws SQLException {
        int effectRows = this.dbOperator.update(DELETE_DEP_FLOW_RELATION_BY_KEY, id);
        return effectRows;

    }

    @Override
    public int searchFlowRelationCount(Integer depedProjectId, String depedFlowId, Integer projectId, String flowId, String userName) throws SQLException {
        String select = "select count(*) ";
        Object[] whereAndParams = buildWhereAndParams(SEARCH_DEP_FLOW_RELATION_FROM_SQL, depedProjectId, depedFlowId, projectId, flowId, userName);
        String fromAndWhere = (String) whereAndParams[0];
        List<Object> params = (List<Object>) whereAndParams[1];
        //pagination
        String sql = StringUtils.join(select, fromAndWhere);


        logger.debug("sql:{}", sql);
        logger.debug("params:{}", params);
        Integer result = this.dbOperator.query(sql, new IntHandler(), params.toArray());

        return result;
    }

    @Override
    public List<DepFlowRelationDetail> searchFlowRelation(Integer depedProjectId, String depedFlowId, Integer projectId, String flowId, String userName, int pageNum, int pageSize) throws SQLException {
        String select = "select r.*,depedProject.name as depended_project_name,p.name  as project_name \n";
        Object[] whereAndParams = buildWhereAndParams(SEARCH_DEP_FLOW_RELATION_FROM_SQL, depedProjectId, depedFlowId, projectId, flowId, userName);
        String fromAndWhere = (String) whereAndParams[0];
        List<Object> params = (List<Object>) whereAndParams[1];
        //pagination
        String sql = StringUtils.join(select, fromAndWhere, " limit ?,?");

        int startRowNum = (pageNum - 1) * pageSize;
        params.add(startRowNum);
        params.add(pageSize);

        logger.debug("sql:{}", sql);
        logger.debug("params:{}", params);
        List<DepFlowRelationDetail> result = this.dbOperator.query(sql, new FetchDepFlowRelationDetailHandler(), params.toArray());

        return result;
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
                Timestamp createTimeTs = rs.getTimestamp("create_time");
                final Instant createTime = createTimeTs == null ? null : createTimeTs.toInstant();
                Timestamp modifyTimeTs = rs.getTimestamp("modify_time");
                final Instant modifyTime = modifyTimeTs == null ? null : modifyTimeTs.toInstant();
                DepFlowInstance instance = new DepFlowInstance(id, projectId, flowId, timeId, status, execId, createTime, modifyTime);
                instances.add(instance);
            } while (rs.next());

            return instances;
        }
    }


    public static class FetchDepFlowRelationDetailHandler implements
            ResultSetHandler<List<
                    DepFlowRelationDetail>> {

        @Override
        public List<DepFlowRelationDetail> handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return Collections.emptyList();
            }

            final List<DepFlowRelationDetail> instances = new ArrayList<>();
            do {
                final int id = rs.getInt("id");
                final int projectId = rs.getInt("project_id");
                final String projectName = rs.getString("project_name");
                final String flowId = rs.getString("flow_id");
                final int depedProjectId = rs.getInt("depended_project_id");
                final String depedProjectName = rs.getString("depended_project_name");
                final String depedFlowId = rs.getString("depended_flow_id");
                final String createUser = rs.getString("create_user");
                Timestamp createTimeTs = rs.getTimestamp("create_time");
                final Instant createTime = createTimeTs == null ? null : createTimeTs.toInstant();
                Timestamp modifyTimeTs = rs.getTimestamp("modify_time");
                final Instant modifyTime = modifyTimeTs == null ? null : modifyTimeTs.toInstant();
                DepFlowRelationDetail instance = new DepFlowRelationDetail(id, depedProjectId, depedProjectName, depedFlowId, projectId, projectName, flowId, createUser,createTime, modifyTime);
                instances.add(instance);
            } while (rs.next());

            return instances;
        }
    }

    public static class FetchDepFlowRelationHandler implements
            ResultSetHandler<List<
                    DepFlowRelation>> {

        @Override
        public List<DepFlowRelation> handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return Collections.emptyList();
            }

            final List<DepFlowRelation> instances = new ArrayList<>();
            do {
                final int id = rs.getInt("id");
                final int projectId = rs.getInt("project_id");
                final String flowId = rs.getString("flow_id");
                final int depedProjectId = rs.getInt("depended_project_id");
                final String depedFlowId = rs.getString("depended_flow_id");
                final String createUser = rs.getString("create_user");
                Timestamp createTimeTs = rs.getTimestamp("create_time");
                final Instant createTime = createTimeTs == null ? null : createTimeTs.toInstant();
                Timestamp modifyTimeTs = rs.getTimestamp("modify_time");
                final Instant modifyTime = modifyTimeTs == null ? null : modifyTimeTs.toInstant();
                DepFlowRelation instance = new DepFlowRelation(id, depedProjectId, depedFlowId, projectId, flowId,createUser, createTime, modifyTime);
                instances.add(instance);
            } while (rs.next());

            return instances;
        }
    }

    private static class IntHandler implements ResultSetHandler<Integer> {

        @Override
        public Integer handle(final ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return 0;
            }
            return rs.getInt(1);
        }
    }

    private Object[] buildWhereAndParams(String mainSql, Integer depedProjectId, String depedFlowId, Integer projectId, String flowId, String userName) {
        String sql = mainSql;
        String filterStr = "";
        List<Object> params = new ArrayList<>();

        boolean isFirst = true;
        if (depedProjectId != null) {
            filterStr = StringUtils.join(filterStr, isFirst ? "" : " and ", " r.depended_project_id=? ");
            params.add(depedProjectId);
            isFirst = false;
        }
        if (depedFlowId != null) {
            filterStr = StringUtils.join(filterStr, isFirst ? "" : " and ", " r.depended_flow_id like ? ");
            params.add(StringUtils.join('%', depedFlowId, '%'));
            isFirst = false;
        }
        if (projectId != null) {
            filterStr = StringUtils.join(filterStr, isFirst ? "" : " and ", " r.project_id=? ");
            params.add(projectId);
            isFirst = false;
        }
        if (flowId != null) {
            filterStr = StringUtils.join(filterStr, isFirst ? "" : " and ", " r.flow_id like ? ");
            params.add(StringUtils.join('%', flowId, '%'));
            isFirst = false;
        }
        if (userName != null) {
            filterStr = StringUtils.join(filterStr, isFirst ? "" : " and ", " r.create_user = ? ");
            params.add(userName);
            isFirst = false;
        }

        filterStr = filterStr.trim().toLowerCase();
        if (StringUtils.isNotBlank(filterStr)) {
            sql = StringUtils.join(sql, " where ", filterStr);
        }

        Object[] result = new Object[2];
        result[0] = sql;
        result[1] = params;
        return result;

    }
}
