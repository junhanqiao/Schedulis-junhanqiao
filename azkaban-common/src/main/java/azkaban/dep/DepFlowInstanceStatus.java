package azkaban.dep;

public enum DepFlowInstanceStatus {
    INIT("INIT", 0),
    READY("READY", 1),
    SUBMITED("SUBMITED", 2),
    SUCCESS("SUCCESS", 3),
    FAILED("FAILED", 4);

    private String name;
    private int value;

    DepFlowInstanceStatus(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static DepFlowInstanceStatus fromInt(int value) {
        DepFlowInstanceStatus result = null;
        for (DepFlowInstanceStatus status : DepFlowInstanceStatus.values()) {
            if (status.value == value) {
                result = status;
                break;
            }
        }
        return result;
    }

    public int getValue() {
        return value;
    }
}
