package com.leyou.search.test;

import com.leyou.LySearchApplication;
import com.leyou.GoodsClient;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.pojo.Goods;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class IndexCreateTest {
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;
	@Autowired
	private GoodsClient goodsClient;
	@Autowired
	private IndexService indexService;
	@Autowired
	private GoodsRepository goodsRepository;
	@Test
	public void createIndex() {
		elasticsearchTemplate.createIndex(Goods.class);
		elasticsearchTemplate.putMapping(Goods.class);
	}
	@Test
	public void loadData() {
		int page = 1;
		while (true) {
			PageResult<SpuBo> spuBoPageResult = goodsClient.querySpuByPage(page, 50, null, null);
			if (null == spuBoPageResult) {
				break;
			}
			page++;
			List<SpuBo> items = spuBoPageResult.getItems();
			List<Goods> goodsList = new ArrayList<>();
			for (SpuBo spuBo:items) {
				//2	华为 G9 青春版 1	骁龙芯片！3GB运行内存！索尼1300万摄像头！<a href='https://sale.jd.com/act/DhKrOjXnFcGL.html' target='_blank'>华为新品全面上线，更多优惠猛戳》》</a>	74	75	76	8557	1	1	2018-04-21 15:55:15	2019-09-03 08:56:32
				//spu-goods
				Goods goods = indexService.buildGoods(spuBo);
				goodsList.add(goods);
			}
			goodsRepository.saveAll(goodsList);
		}
	}
}
