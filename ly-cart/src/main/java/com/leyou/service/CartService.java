package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.interceptors.LoginInterceptor;
import com.leyou.pojo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
	@Autowired
	private StringRedisTemplate redisTemplate;
	public static final String KEY_PREFIX = "ly.cart.uid:";

	public void addCart(Cart cart) {
		//从线程域取
		UserInfo userInfo = LoginInterceptor.getLoginUser();
		BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
		//ly.cart.uid:32
		Object skuObj = ops.get(cart.getSkuId().toString()); //25667125294
		if (null != skuObj) {
			Cart cart1 = JsonUtils.nativeRead(skuObj.toString(), new TypeReference<Cart>() {
			});
			cart1.setNum(cart.getNum() + cart1.getNum());
			ops.put(cart.getSkuId().toString(), JsonUtils.serialize(cart1));
		} else {
			ops.put(cart.getSkuId().toString(), JsonUtils.serialize(cart)); //25667125294
//            image: "http://image.leyou.com/images/7/7/1524297537014.jpg"
//            num: 1
//            ownSpec: "{"4":"黑色","12":"6GB","13":"64GB"}"
//            price: 362800
//            skuId: 25667125294
//            title: "小米（MI） 小米mix2S 手机 黑色 全网通 6GB+64GB"
		}
	}

	public List<Cart> queryCarts() {
		UserInfo loginUser = LoginInterceptor.getLoginUser();
		BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + loginUser.getId());
		List<Object> skuObj = ops.values();
		List<Cart> carts = new ArrayList<>();
		if (null != skuObj) {
			for (Object o:skuObj) {
				Cart c = JsonUtils.nativeRead(o.toString(), new TypeReference<Cart>() {});
				carts.add(c);
			}
		}
		return carts;
	}

	public void updateIncrementCart(Cart cart) {
		UserInfo loginUser = LoginInterceptor.getLoginUser();
		BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + loginUser.getId());
		String skuJson = ops.get(cart.getSkuId().toString()).toString();
		Cart c1 = JsonUtils.nativeRead(skuJson, new TypeReference<Cart>() {
		});
		c1.setNum(c1.getNum() + 1);
		ops.put(cart.getSkuId().toString(), JsonUtils.serialize(c1));
	}

	public void deleteCart(Long skuId) {
		UserInfo loginUser = LoginInterceptor.getLoginUser();
		BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + loginUser.getId());
		ops.delete(skuId.toString());
	}
}
