package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.clients.CategoryClient;
import com.leyou.clients.GoodsClient;
import com.leyou.clients.SpecClient;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexService {
	@Autowired
	private CategoryClient categoryClient;
	@Autowired
	private GoodsClient goodsClient;
	@Autowired
	private SpecClient specClient;

	private String chooseSegment(String value, SpecParam p) {
		double val = NumberUtils.toDouble(value);
		String result = "其它";
		// 保存数值段
		for (String segment : p.getSegments().split(",")) { //segment:1000-2000
			String[] segs = segment.split("-");
			// 获取数值范围
			double begin = NumberUtils.toDouble(segs[0]);
			double end = Double.MAX_VALUE;
			if (segs.length == 2) {
				end = NumberUtils.toDouble(segs[1]);
			}
			// 判断是否在范围内
			if (val >= begin && val < end) {
				if (segs.length == 1) {
					result = segs[0] + p.getUnit() + "以上";
				} else if (begin == 0) {
					result = segs[1] + p.getUnit() + "以下";
				} else {
					result = segment + p.getUnit(); //添加单位
				}
				break;
			}
		}
		return result;
	}

	public Goods buildGoods(SpuBo spuBo) {
		Goods goods = new Goods();
		BeanUtils.copyProperties(spuBo, goods);
		//all 标题 分类
		List<String> names = categoryClient.queryNameByIds(Arrays.asList(spuBo.getCid1(), spuBo.getCid2(), spuBo.getCid3()));
		String all = spuBo.getTitle() + " " + StringUtils.join(names, " "); //华为 G9
		goods.setAll(all);

		//spuid-sku
		List<Sku> skus = goodsClient.querySkuBySpuId(spuBo.getId());
		List<Long> prices = new ArrayList<Long>();
		List<Map<String, Object>> skuMapList = new ArrayList<>();
		for (Sku sku:skus) {
			prices.add(sku.getPrice());
			Map<String, Object> skuMap = new HashMap<String, Object>();
			skuMap.put("id", sku.getId());
			skuMap.put("title", sku.getTitle());
			skuMap.put("price", sku.getPrice());
			skuMap.put("image", StringUtils.isBlank(sku.getImages())?"":sku.getImages().split(",")[0]);
			skuMapList.add(skuMap);
		}
		goods.setPrice(prices);
		//skuMapList-json
		String s = JsonUtils.serialize(skuMapList);
		goods.setSkus(s);
		//   private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
		List<SpecParam> specParams = specClient.querySpecParam(null, spuBo.getCid3(), true, null);
		//spuId spudetail
		SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuBo.getId());
		//{"1":"其它1","2":"G9青春版（全网通版）","3":2016,"5":143,"6":"其它","7":"Android","8":"骁龙（Snapdragon)","9":"骁龙617（msm8952）","10":"八核","11":1.5,"14":5.2,"15":"1920*1080(FHD)","16":800,"17":1300,"18":3000}
		Map<Long, Object> genericMap = JsonUtils.nativeRead(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
		});
		//{"4":["金","粉","灰","银"],"12":["4GB"],"13":["32GB"]}
		Map<Long, List<String>> specialMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
		});
		Map<String, Object> specs = new HashMap<>();
		for (SpecParam specParam:specParams) {
			Long id = specParam.getId(); //7
			String name = specParam.getName(); //操作系统
			Object value = null;
			if (specParam.getGeneric()) {
				value = genericMap.get(id); //Android
				if (null != value && specParam.getNumeric()) {
					value = this.chooseSegment(value.toString(), specParam); //0-1.5
				}
			} else {
				value = specialMap.get(id);
			}
			if (null == value) {
				value = "其他";
			}
			specs.put(name, value); // 操作系统 Android  //CPU频率 0-1.5
		}
		goods.setSpecs(specs);
		return goods;
	}
}
