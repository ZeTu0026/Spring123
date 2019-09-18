package com.leyou.filter;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({FilterProperties.class, JwtProperties.class})
public class LoginFilter extends ZuulFilter {

	@Autowired
	private JwtProperties jwtProperties;
	@Autowired
	private FilterProperties filterProperties;

	@Override
	public String filterType() {
		//请求在被路由之前执行
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		//请求头之前，查看请求参数
		return FilterConstants.PRE_DECORATION_FILTER_ORDER-1;
	}

	@Override
	public boolean shouldFilter() {
		//如果在白名单，不进行过滤
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletRequest request = requestContext.getRequest(); /*得到请求*/
		//http://api.leyou.com/api/auth/verify
		String requestURI = request.getRequestURI(); /*请求请求地址*/
		List<String> allowPaths = filterProperties.getAllowPaths();
		for (String allowPath:allowPaths) {
			return false;
		}
		//如果不在白名单，进行过滤
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		//http://localhost:10010/api/user/1
		//http://localhost:10010/api/user/1?token=1087
		RequestContext requestContext = RequestContext.getCurrentContext();
		try {
			HttpServletRequest request = requestContext.getRequest();
			//获取请求参数
			String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName()); //LY_TOKEN
			//eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MzIsInVzZXJuYW1lIjoibWlrZTEyMyIsImV4cCI6MTU2ODU5NzE5NX0.UwQSWSNqRxn7VqP0rS748PrgfmiJmHb6vCNwdfbJ5-HBCGt_278RySM7eX6jAsPvbdoKjF3Ud3EeOfb1HfLkjk8YWCR9J9vfrATnUzbVUbz70dkyqFrPfEbt52C7gumYaHuJLMDU9hF8m_gppJfAdujYU644YIHt8qyQBc9sYDI
			UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
		} catch (Exception e) {
			e.printStackTrace();
			//失败
			requestContext.setResponseStatusCode(401);
			//说明没有登录，不响应
			requestContext.setSendZuulResponse(false);
		}
		return null;
	}
}
