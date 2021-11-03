package azkaban.dep.vo;

import java.time.Instant;
import java.util.Objects;

public class DepFlowRelationDetail {
    private int id;
    private int dependedProjectId;
    private String dependedProjectName;
    private String dependedFlowId;
    private int projectId;
    private String projectName;
    private String flowId;
    private String createUser;
    private Instant createTime;
    private Instant modifyTime;

    public DepFlowRelationDetail(int id, int dependedProjectId, String dependedProjectName, String dependedFlowId, int projectId, String projectName, String flowId,String createUser, Instant createTime, Instant modifyTime) {
        this.id = id;
        this.dependedProjectId = dependedProjectId;
        this.dependedProjectName = dependedProjectName;
        this.dependedFlowId = dependedFlowId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.flowId = flowId;
        this.createUser=createUser;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
    }

    public DepFlowRelationDetail() {
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

    public String getDependedProjectName() {
        return dependedProjectName;
    }

    public void setDependedProjectName(String dependedProjectName) {
        this.dependedProjectName = dependedProjectName;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
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
        DepFlowRelationDetail that = (DepFlowRelationDetail) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DepFlowRelationDetail{" +
                "id=" + id +
                ", dependedProjectId=" + dependedProjectId +
                ", dependedProjectName='" + dependedProjectName + '\'' +
                ", dependedFlowId='" + dependedFlowId + '\'' +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", flowId='" + flowId + '\'' +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
