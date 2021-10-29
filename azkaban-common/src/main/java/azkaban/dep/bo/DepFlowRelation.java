package azkaban.dep.bo;

import java.time.Instant;
import java.util.Objects;

public class DepFlowRelation {
    private int id;
    private int dependedProjectId;
    private String dependedFlowId;
    private int projectId;
    private String flowId;
    private Instant createTime;
    private Instant modifyTime;

    public DepFlowRelation(int id, int dependedProjectId, String dependedFlowId, int projectId, String flowId, Instant createTime, Instant modifyTime) {
        this.id = id;
        this.dependedProjectId = dependedProjectId;
        this.dependedFlowId = dependedFlowId;
        this.projectId = projectId;
        this.flowId = flowId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDependedProjectId() {
        return dependedProjectId;
    }

    public void setDependedProjectId(int dependedProjectId) {
        this.dependedProjectId = dependedProjectId;
    }

    public String getDependedFlowId() {
        return dependedFlowId;
    }

    public void setDependedFlowId(String dependedFlowId) {
        this.dependedFlowId = dependedFlowId;
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
        DepFlowRelation that = (DepFlowRelation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DepFlowRelation{" +
                "id=" + id +
                ", dependedProjectId=" + dependedProjectId +
                ", dependedFlowId='" + dependedFlowId + '\'' +
                ", projectId=" + projectId +
                ", flowId='" + flowId + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
