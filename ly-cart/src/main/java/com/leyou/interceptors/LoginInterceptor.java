package com.leyou.interceptors;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private JwtProperties jwtProperties;
	//定义一个线程域，存放登录用户
	public static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

	public LoginInterceptor(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
		if (StringUtils.isBlank(token)) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return false;
		}
		try {
			UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
			//把userinfo 放入到线程域
			tl.set(userInfo);
			//t1(userinfo(1232,张三))
			//tl(userinfo(123423,李四))
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static UserInfo getLoginUser() {
		return tl.get();
	}
}
