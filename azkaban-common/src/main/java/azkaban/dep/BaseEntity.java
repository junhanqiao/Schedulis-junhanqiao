package azkaban.dep;

import java.time.Instant;

public class BaseEntity {
    public BaseEntity() {
    }
    private Instant createTime;
    private Instant modifyTime;

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
}
