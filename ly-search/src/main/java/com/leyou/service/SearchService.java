package com.leyou.service;

import com.leyou.clients.BrandClient;
import com.leyou.clients.CategoryClient;
import com.leyou.clients.SpecClient;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecParam;
import com.leyou.pojo.Goods;
import com.leyou.repository.GoodsRepository;
import com.leyou.utils.SearchRequest;
import com.leyou.utils.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {
	@Autowired
	private GoodsRepository goodsRepository;
	@Autowired
	private CategoryClient categoryClient;
	@Autowired
	private BrandClient brandClient;
	@Autowired
	private SpecClient specClient;

	public SearchResult page(SearchRequest searchRequest) {
		//获取查询条件
		String key = searchRequest.getKey(); //小米
		if (null == key) {
			return null;
		}
		//构建查询
		NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
		//构建查询
		QueryBuilder query = buildBasicQueryWithFilter(searchRequest);
		//添加查询
		queryBuilder.withQuery(query);
		//添加分页
		queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1, searchRequest.getSize()));

		String categoryAggName = "category"; /*按分类聚合*/
		String brandAggName = "brand"; /*按品牌聚合*/

		queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
		queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

		List<Category> categories = new ArrayList<>();
		List<Brand> brands = new ArrayList<>();

		AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());

		//----分类
		LongTerms categoryTerms = (LongTerms) goodsPage.getAggregation(categoryAggName);
		List<LongTerms.Bucket> buckets = categoryTerms.getBuckets();
		List<Long> cids = new ArrayList<>();

		for (LongTerms.Bucket bucket:buckets) {
			cids.add(bucket.getKeyAsNumber().longValue()); //76
		}

		List<String> names = categoryClient.queryNameByIds(cids);

		for (int i = 0; i < cids.size(); i++) {
			Category category = new Category();
			category.setId(cids.get(i)); //76
			category.setName(names.get(i)); //手机
			categories.add(category);
		}

		//品牌
		LongTerms brandTerms = (LongTerms) goodsPage.getAggregation(brandAggName);
		List<LongTerms.Bucket> brandTermsBuckets = brandTerms.getBuckets();
		List<Long> brandIds = new ArrayList<>(); //8557 15127

		for (LongTerms.Bucket bucket:brandTermsBuckets) {
			brandIds.add(bucket.getKeyAsNumber().longValue());
		}

		for (Long b:brandIds) {
			Brand brand = brandClient.queryBrandById1(b);
			brands.add(brand);
		}

		List<Map<String, Object>> specs = null;

		if (categories.size() == 1) {
			specs = getSpecs(categories.get(0).getId(), query);
		}
		return new SearchResult(goodsPage.getTotalElements(), new Long(goodsPage.getTotalPages()), goodsPage.getContent(), categories, brands, specs);
	}

	private List<Map<String, Object>> getSpecs(Long id, QueryBuilder query) {
		List<Map<String, Object>> specList = new ArrayList<>();
		NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
		queryBuilder.withQuery(query);
		List<SpecParam> searchingSpecParam = specClient.querySpecParam(null, id, true, null);

		for (SpecParam s:searchingSpecParam) {
			queryBuilder.addAggregation(AggregationBuilders.terms(s.getName()).field("specs." + s.getName() + ".keyword"));
			//specs.操作系统.keyword
			//specs.CPU品牌.keyword
		}

		AggregatedPage<Goods> page = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());

		for (SpecParam ss:searchingSpecParam) {
			String name = ss.getName();
			StringTerms stringTerms = (StringTerms) page.getAggregation(name);
			List<String> values = new ArrayList<>();
			List<StringTerms.Bucket> buckets = stringTerms.getBuckets();

			for (StringTerms.Bucket bucket:buckets) {
				values.add(bucket.getKeyAsString());
			}

			Map<String, Object> specMap = new HashMap<>();
			specMap.put("k",name);//操作系统
			specMap.put("options",values);//ios ,安卓
			specList.add(specMap);
		}
		return specList;
	}

	private QueryBuilder buildBasicQueryWithFilter(SearchRequest request) {
		//布尔查询对象
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		//基本查询条件
		queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
		BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
		//过滤条件
		Map<String, String> filter = request.getFilter();
		//分类    合约机
		///cid3
		//cpu品牌  高通
		///brandId
		//电池容量（mAh）  3000-4000mAh
		for (Map.Entry<String, String> entry:filter.entrySet()) {
			String key = entry.getKey(); //分类
			String value = entry.getValue(); // 合约机
			//商品分类和品牌可以直接查询
			if (key != "cid3" && key != "brandId") {
				key = "specs." + key + ".keyword";
				//specs.电池容量（mAh）.keyword
				//字符串类型，进行term查询
				filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
			}
		}
		//添加过滤条件
		queryBuilder.filter(filterQueryBuilder);
		return queryBuilder;
	}
}
