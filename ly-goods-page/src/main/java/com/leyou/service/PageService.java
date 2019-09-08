package com.leyou.service;

import com.leyou.clients.GoodsClient;
import com.leyou.clients.SpecClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {
	@Autowired
	private GoodsClient goodsClient;
	@Autowired
	private SpecClient specClient;
	public Map<String, Object> loadData(Long spuId) {
		Map<String, Object> results = new HashMap<>();
		//查询spu
		Spu spu = goodsClient.querySpuById(spuId);

		results.put("spu", spu);

		//查询spudetail
		SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);
		results.put("spuDetail",spuDetail);
		List<Sku> skus = goodsClient.querySkuBySpuId(spuId);
		results.put("skus",skus);
		//特有规格参数
		List<SpecParam> specDetailParams = specClient.querySpecParam(null, spu.getCid3(), null, false);
		Map<Long, Object> specParams1 = new HashMap<>();
		for (SpecParam specParam:specDetailParams) {
			specParams1.put(specParam.getId(), specParam.getName());
			//4  机身颜色
			//12 内存
			//13  机身存储
		}
		results.put("specParams", specParams1);
		List<SpecGroup> specGroups = specClient.querySpecGroups(spu.getCid3());
		results.put("specGroups", specGroups);
		return results;
	}
}
