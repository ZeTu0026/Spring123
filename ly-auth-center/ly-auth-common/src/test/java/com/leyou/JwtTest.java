package com.leyou;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {
	private static final String pubKeyPath = "d:/temp/rsa/rsa.pub";
	private static final String priKeyPath = "d:/temp/rsa/rsa.pri";
	private PublicKey publicKey;
	private PrivateKey privateKey;

/*    @Test
    public  void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath,priKeyPath,"1234");
    }*/

	@Before
	public void testGetRsa() throws Exception {
		publicKey = RsaUtils.getPublicKey(pubKeyPath);
		privateKey = RsaUtils.getPrivateKey(priKeyPath);
	}

	@Test
	public  void testGenerateToken() throws Exception {
		String s = JwtUtils.generateToken(new UserInfo(222L, "tom"), privateKey, 5);
		System.out.println(s);
		//eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjIyLCJ1c2VybmFtZSI6InRvbSIsImV4cCI6MTU2ODIwNDQ4NH0.UnGA0qitAov4kAwDV4VeS-y03wF-p5WyF_VcoXXHZkynYo5lnC2wPVkuIYLOklO6t8KwB3d9kEOi40pYvGCKG2uxGgpnNkjrrsWfW2qt8RachSD9BmheqcqGltDWqv5Yjkm_h8-x9woVzmEOFf2x3Tj0vAqKBZpjfF8RD-wU7i8
	}

	@Test
	public  void testParseToken() throws Exception {
		String token="eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjIyLCJ1c2VybmFtZSI6InRvbSIsImV4cCI6MTU2ODI0ODkxNH0.CtACuZvOAN6C6R_Hd93fWsNVFrgh7l_x40RDqCg0rpalsb5skKAZA2KDrV6LvP6Iyu2RGHXhZpA1_t2OCPgCj65hNSyaREY79Wtf0-n5wTqTzjwUKYSBq7j50CxyoWk2lk920_bStAR57GPoSFQY-FAD9eW6AsctASjLT_661d4";
		UserInfo userInfo = JwtUtils.getInfoFromToken(token, publicKey);
		System.out.println(userInfo.getId());
		System.out.println(userInfo.getUsername());
	}
}
