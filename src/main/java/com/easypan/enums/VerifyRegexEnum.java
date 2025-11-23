package com.easypan.enums;

public enum VerifyRegexEnum {
    NONE("", "不校验"),
    EMAIL("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$", "邮箱"),
    PASSWORD("^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$", "密码（8~18位，数字字母特殊字符）");

    private String regex;
    private String description;

    VerifyRegexEnum(String regex, String description) {
        this.regex = regex;
        this.description = description;
    }

    public String getRegex() {
        return regex;
    }

    public String getDescription() {
        return description;
    }
}
