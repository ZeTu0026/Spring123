package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
	@Autowired
	private UserService userService;

	@GetMapping("/check/{data}/{type}")
	private ResponseEntity<Boolean> check(@PathVariable("data") String data, @PathVariable("type") Integer type) {
		Boolean b = userService.check(data, type);
		if (null == b) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		return ResponseEntity.ok(b);
	}

	@PostMapping("code")
	public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String ph) {
		Boolean b = userService.sendVerifyCode(ph);
		if (null == b || !b) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping("register")
	public ResponseEntity<Void> createUser(@Valid User user, @RequestParam("code") String code) {
		Boolean b = userService.createUser(user, code);
		if (null == b || !b) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping("login")
	public ResponseEntity<User> queryUser(@RequestParam("username") String username, @RequestParam("password") String password) {
		User user = userService.queryUser(username, password);
		if (null == user) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(user);
	}
}
