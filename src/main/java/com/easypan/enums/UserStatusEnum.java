package com.easypan.enums;

/**
 * 用户状态枚举类
 * 用于表示用户当前是否启用/禁用
 */
public enum UserStatusEnum {
    // 用户被禁用
    DISABLE(0, "禁用"),

    // 用户启用中
    ENABLE(1, "启用");

    // 状态码（0表示禁用，1表示启用）
    private Integer status;

    // 状态描述（中文说明）
    private String desc;

    /**
     * 构造方法，传入状态值与描述
     * @param status 状态码
     * @param desc   状态描述
     */
    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 根据状态码获取对应枚举值
     * @param status 整型状态码
     * @return 对应的 UserStatusEnum 或 null（未匹配）
     */
    public static UserStatusEnum getByStatus(Integer status) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getDesc() {
        return this.desc;
    }
}
