package com.leyou.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.client.UserClient;
import com.leyou.config.JwtProperties;
import com.leyou.user.pojo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
	@Autowired
	private UserClient userClient;
	@Autowired
	private  JwtProperties jwtProperties;

	public String accredit(String username, String password) {
		User user = userClient.queryUser(username, password);
		if(null!=user){
			UserInfo userInfo = new UserInfo();
			BeanUtils.copyProperties(user,userInfo);
			try {
				String token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
				return  token;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
