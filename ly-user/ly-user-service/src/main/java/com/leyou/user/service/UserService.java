package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private StringRedisTemplate redisTemplate;

	private final static String KEY_PREFIX = "user:code:phone:";

	public Boolean check(String data, Integer type) {
		//tom ,1
		//186962232,2
		User user = new User();
		switch (type) {
			case 1: user.setUsername(data);
			break;
			case 2: user.setPhone(data);
			break;
		}
		return userMapper.selectCount(user) != 1; //true //false
		//select Count(*) from tb_user m where m.username=data
		//select Count(*) from tb_user m where m.phone=data
	}

	public Boolean sendVerifyCode(String ph) {
		//产生6位数验证码
		String code = NumberUtils.generateCode(5);
		//调用第三方接口验证码发ph()略过  ph  code
		//  try
		//保存验证码
		//user:code:phone:18696196365
		redisTemplate.opsForValue().set(KEY_PREFIX + ph, code, 5, TimeUnit.MINUTES);
		return true;
	}

	public Boolean createUser(User user, String code) {
		//用户名和手机号码判断略过
		//验证码是否正确
		String s = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
		if (StringUtils.isNoneBlank(s)) {
			//有值
			if (code.equals(s)) {
				String salt = CodeUtils.generateSalt();
				user.setCreated(new Date());
				user.setSalt(salt);
				user.setPassword(CodeUtils.md5Hex(user.getPassword(), salt));
				userMapper.insert(user);
				//
				redisTemplate.delete(KEY_PREFIX + user.getPhone());
				return true;
			}
		}
		return null;
	}

	public User queryUser(String username, String password) {
		User user = new User();
		user.setUsername(username); //tom12312

		User user1 = userMapper.selectOne(user);
		//select * from tb_user m where m.username=tom1

		if (null == user1) {
			return null;
		}
		String s = CodeUtils.md5Hex(password, user1.getSalt());
		if (user1.getPassword().equals(s)) {
			return user1;
		}
		return null;
	}
}
