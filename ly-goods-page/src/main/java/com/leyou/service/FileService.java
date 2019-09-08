package com.leyou.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;

@Service
public class FileService {
	@Autowired
	private PageService pageService;
	@Autowired
	private TemplateEngine templateEngine;

	@Value("${ly.thymeleaf.destPath}")
	private String destPath; //D:/develop/html/item

	public boolean exists(Long spuId) {
		return this.createPath(spuId).exists();
	}

	private File createPath(Long spuId) {
		File file = new File(this.destPath);
		if (!file.exists()) {
			file.mkdir();
		}
		return new File(file, spuId + ".html");
	}

	public void syncCreateHtml(Long spuId) {
		createHtml(spuId);
	}

	private void createHtml(Long spuId) {
		//113.html
		Context context = new Context();
		context.setVariables(this.pageService.loadData(spuId));
		File dir=new File(destPath);
		File filePath= new File(dir,spuId+".html");
		try {
			PrintWriter writer= new PrintWriter(filePath, "utf-8");
			templateEngine.process("item",context,writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
