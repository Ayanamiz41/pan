package com.easypan.enums;


public enum ResponseCodeEnum {

	CODE_200(200, "请求成功"),
	CODE_404(404, "请求地址不存在"),
	CODE_600(600, "请求地址不存在"),//没有走前端
	CODE_601(601, "请求参数错误"),
	CODE_602(602, "数据已存在"),
	CODE_500(500, "服务器返回错误，请联系管理员"),
	CODE_901(901,"登录超时，请重新登录"),
	CODE_902(902,"分享链接不存在或者已失效"),
	CODE_903(903,"分享验证失败，请重新输入提取码"),
	CODE_904(904,"网盘空间不足，请扩容");

	private Integer code;

	private String msg;

	ResponseCodeEnum(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
}
