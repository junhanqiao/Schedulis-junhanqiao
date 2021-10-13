package azkaban.dep;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

public class DepFlowInstance {
    private int id;
    private int projectId;
    private String flowId;
    private LocalDateTime timeId;
    private DepFlowInstanceStatus status;
    private int execId;
    private Instant createTime;
    private Instant modifyTime;

    public DepFlowInstance(int id, int projectId, String flowId, LocalDateTime timeId, DepFlowInstanceStatus status, int execId, Instant createTime, Instant modifyTime) {
        this.id = id;
        this.projectId = projectId;
        this.flowId = flowId;
        this.timeId = timeId;
        this.status = status;
        this.execId = execId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public LocalDateTime getTimeId() {
        return timeId;
    }

    public void setTimeId(LocalDateTime timeId) {
        this.timeId = timeId;
    }

    public DepFlowInstanceStatus getStatus() {
        return status;
    }

    public void setStatus(DepFlowInstanceStatus status) {
        this.status = status;
    }

    public int getExecId() {
        return execId;
    }

    public void setExecId(int execId) {
        this.execId = execId;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Instant modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepFlowInstance that = (DepFlowInstance) o;
        return projectId == that.projectId &&
                flowId.equals(that.flowId) &&
                timeId.equals(that.timeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, flowId, timeId);
    }

    @Override
    public String toString() {
        return "DepFlowInstance{" +
                "projectId=" + projectId +
                ", flowId='" + flowId + '\'' +
                ", timeId=" + timeId +
                ", status=" + status +
                ", execId=" + execId +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
