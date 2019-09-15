package com.leyou.config;

import com.leyou.auth.utils.RsaUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties("ly.jwt")
public class JwtProperties {
	private String secret;//ly@Login(Auth}*^31)&hei%
	private String pubKeyPath;//D:/rsa/rsa.pub
	private  String priKeyPath;//D:/rsa/rsa.pri
	private int expire;//30
	private  String cookieName;//LY_TOKEN
	private Integer cookieMaxAge;//1800
	private PublicKey publicKey;
	private PrivateKey privateKey;

	@PostConstruct
	public void init(){
		try {
			File pubKey = new File(pubKeyPath);
			File priKey = new File(priKeyPath);
			if (!pubKey.exists() || !priKey.exists()) {
				// 生成公钥和私钥
				RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
			}
			// 获取公钥和私钥
			this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
			this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getPubKeyPath() {
		return pubKeyPath;
	}

	public void setPubKeyPath(String pubKeyPath) {
		this.pubKeyPath = pubKeyPath;
	}

	public String getPriKeyPath() {
		return priKeyPath;
	}

	public void setPriKeyPath(String priKeyPath) {
		this.priKeyPath = priKeyPath;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	public String getCookieName() {
		return cookieName;
	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public Integer getCookieMaxAge() {
		return cookieMaxAge;
	}

	public void setCookieMaxAge(Integer cookieMaxAge) {
		this.cookieMaxAge = cookieMaxAge;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
}
