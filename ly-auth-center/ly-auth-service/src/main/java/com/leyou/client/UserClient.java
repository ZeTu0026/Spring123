package com.leyou.client;

import com.leyou.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {
	@PostMapping("login")
	User queryUser(@RequestParam("username") String username, @RequestParam("password") String password);
	//http://user-service/login
}
