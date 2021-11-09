package azkaban.dep.bo;

import java.util.Objects;

public class FlowNode {
    private int projectId;
    private String flowId;

    public FlowNode() {
    }

    public FlowNode(int projectId, String flowId) {
        this.projectId = projectId;
        this.flowId = flowId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowNode flowNode = (FlowNode) o;
        return projectId == flowNode.projectId &&
                Objects.equals(flowId, flowNode.flowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, flowId);
    }

    @Override
    public String toString() {
        return "FlowNode{" +
                "projectId=" + projectId +
                ", flowId='" + flowId + '\'' +
                '}';
    }
}
