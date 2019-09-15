package com.leyou.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoginFilter extends ZuulFilter {
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
		return false;
	}

	@Override
	public Object run() throws ZuulException {
		//http://localhost:10010/api/user/1
		//http://localhost:10010/api/user/1?token=1087
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletRequest request = requestContext.getRequest();
		//获取请求参数
		String token = request.getParameter("token");
		if(StringUtils.isEmpty(token)){
			//不存在
			requestContext.setSendZuulResponse(false);
			//返回响应码
			requestContext.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
		}
		return null;
	}
}
