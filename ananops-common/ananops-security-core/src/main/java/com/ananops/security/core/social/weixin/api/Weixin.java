package com.ananops.security.core.social.weixin.api;

/**
 * 微信API调用接口
 *
 * @author ananops.net @gmail.com
 */
public interface Weixin {

	/**
	 * Gets user info.
	 *
	 * @param openId the open id
	 *
	 * @return the user info
	 */
	WeixinUserInfo getUserInfo(String openId);

}
