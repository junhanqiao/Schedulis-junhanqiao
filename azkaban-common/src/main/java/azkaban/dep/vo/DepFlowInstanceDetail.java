package azkaban.dep.vo;

import azkaban.dep.BaseEntity;
import azkaban.dep.DepFlowInstanceStatus;
import azkaban.executor.ExecutableFlow;
import azkaban.executor.Status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

public class DepFlowInstanceDetail extends BaseEntity {
    private int id;
    private int projectId;
    private String projectName;
    private String flowId;
    private LocalDateTime timeId;
    private DepFlowInstanceStatus status;
    private int execId;

    private String submitUser;
    private Long startTime;
    private Long endTime;
    private Long difftime;
    private Status exeStatus;
    private Integer flowType;

    public DepFlowInstanceDetail() {
    }

    public DepFlowInstanceDetail(int id, int projectId,String projectName, String flowId, LocalDateTime timeId, DepFlowInstanceStatus status, int execId, Instant createTime, Instant modifyTime) {
        this.id = id;
        this.projectId = projectId;
        this.projectName=projectName;
        this.flowId = flowId;
        this.timeId = timeId;
        this.status = status;
        this.execId = execId;
        this.setCreateTime(createTime);
        this.setModifyTime(modifyTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepFlowInstanceDetail that = (DepFlowInstanceDetail) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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


    public String getSubmitUser() {
        return submitUser;
    }

    public void setSubmitUser(String submitUser) {
        this.submitUser = submitUser;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getDifftime() {
        return difftime;
    }

    public void setDifftime(Long difftime) {
        this.difftime = difftime;
    }

    public Status getExeStatus() {
        return exeStatus;
    }

    public void setExeStatus(Status exeStatus) {
        this.exeStatus = exeStatus;
    }

    public Integer getFlowType() {
        return flowType;
    }

    public void setFlowType(Integer flowType) {
        this.flowType = flowType;
    }
}
