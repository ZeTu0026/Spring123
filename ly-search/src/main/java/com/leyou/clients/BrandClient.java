package com.leyou.clients;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface BrandClient extends BrandApi {
	//    @GetMapping("bid/{b}")
	//    public Brand queryBrandById1(@PathVariable("b") Long bid);
}
