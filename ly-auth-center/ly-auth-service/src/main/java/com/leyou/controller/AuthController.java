package com.leyou.controller;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
	@Autowired
	private AuthService authService;
	@Autowired
	private  JwtProperties jwtProperties;

	@PostMapping("accredit")
	public ResponseEntity<Void> accredit(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response){
		String token = authService.accredit(username,password);
		if(StringUtils.isBlank(token)){
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		//向浏览器写入cookie
		CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getCookieMaxAge(),null,true);
		return  ResponseEntity.ok().build();
	}

	@GetMapping("verify")
	public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token, HttpServletRequest request, HttpServletResponse response) throws Exception {
		UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
		//重新生成cookie
		String t = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
		//向浏览器写入cookie
		CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),t,jwtProperties.getCookieMaxAge(),null,true);
		return ResponseEntity.ok(userInfo);
	}
}
