package com.wang.enumstatus;

public enum EnumHttpStatus {

    /**
     * 200状态码标识成功
     * 410状态码标识当前无可用资源
     * 主动拒绝还没加上去
     */
    AVAILABLE(200, "nodeId available"),
    SUCCESS(200, "success"),
    RESOURCENOTENOUGH(410, "resource not enough"),
    DELETESUCCESS(200, "delete success"),
    UEIDNOTEXIST(400, "no ueid found in the second request from signal GW"),
    UEIDSTMSINOTMATCH(400, "ueid and stmsi not match in the first and second request from signal GW");

    private final int status;

    private final String description;

    private EnumHttpStatus(int status, String description){
        this.status = status;
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
