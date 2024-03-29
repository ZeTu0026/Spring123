package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {

	@GetMapping("/spu/page")
	PageResult<SpuBo> querySpuByPage(
		@RequestParam(value = "page", defaultValue = "1") Integer page,
		@RequestParam(value = "rows", defaultValue = "10") Integer rows,
		@RequestParam(value = "saleable", required = false) Boolean saleable,
		@RequestParam(value = "key", required = false) String key
	);

	@GetMapping("sku/list")
	List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);

	@GetMapping("spu/detail/{spuId}")
	SpuDetail querySpuDetailBySpuId(@PathVariable("spuId") Long id);

	@GetMapping("spu/{id}")
	Spu querySpuById(@PathVariable("id") Long spuId);
	//http://item-service/spu/113
}
