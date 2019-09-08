package com.leyou.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.*;
import com.leyou.mapper.SkuMapper;
import com.leyou.mapper.SpuDetailMapper;
import com.leyou.mapper.SpuMapper;
import com.leyou.mapper.StockMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
	@Autowired
	private SpuMapper spuMapper;
	@Autowired
	private SpuDetailMapper spuDetailMapper;
	@Autowired
	private SkuMapper skuMapper;
	@Autowired
	private StockMapper stockMapper;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private BrandService brandService;

	public PageResult<SpuBo> querySpuByPage(Integer page, Integer rows,  Boolean saleable, String key) {
		PageHelper.startPage(page,rows);
		Example example = new Example(Spu.class);
		Example.Criteria criteria = example.createCriteria();
		if(StringUtils.isNotBlank(key)){
			//select * from tb_spu where title like "%华为%"
			criteria.andLike("title","%"+key+"%");
		}
		if(null!=saleable){
			//select * from tb_spu where title like "%华为%" and saleable=true
			criteria.andEqualTo("saleable",saleable);
		}
		Page<Spu> spuPage=(Page<Spu>)spuMapper.selectByExample(example);
		//  select * from tb_spu where   title like "%华为%" and saleable=true limit 1,5
		//  select count(*) from tb_spu where   title like "%华为%" and saleable=true
		List<Spu> spus=spuPage.getResult();
		List<SpuBo> spubBos=new ArrayList<SpuBo>();
		spus.forEach(spu -> {
			SpuBo spuBo = new SpuBo();
			//2	华为 G9 青春版 	骁龙芯片！3GB运行内存！索尼1300万摄像头！<a href='https://sale.jd.com/act/DhKrOjXnFcGL.html' target='_blank'>华为新品全面上线，更多优惠猛戳》》</a>	74	75	76	8557	1	1	2018-04-21 15:55:15	2018-06-18 10:36:12
			BeanUtils.copyProperties(spu,spuBo);
//            spuBo.setTitle(spu.getTitle());
//            spuBo.setSubTitle(spu.getSubTitle());
//            spuBo.setValid(spu.getValid());
			List<String>  names= categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
			spuBo.setCname( StringUtils.join(names,"/"));  //手机/手机通讯/手机
			Brand brand=brandService.queryBrandById(spu.getBrandId());
			spuBo.setBname(brand.getName());
			spubBos.add(spuBo);
		});
		// public PageResult(Long total, Long totalPage, List<T> items)
		return new  PageResult<>(spuPage.getTotal(),new Long(spuPage.getPages()),spubBos);
	}

	@Transactional /*新增服务, 添加事物*/
	public void saveGoods(SpuBo spuBo) {
		spuBo.setSaleable(true);
		spuBo.setValid(true);
		spuBo.setCreateTime(new Date());
		spuBo.setLastUpdateTime(new Date());

		spuMapper.insertSelective(spuBo);

		SpuDetail spuDetail = spuBo.getSpuDetail();
		spuDetail.setSpuId(spuBo.getId());

		spuDetailMapper.insertSelective(spuDetail);
		List<Sku> skus = spuBo.getSkus();
		saveSkus(spuBo, skus);
	}

	private void saveSkus(SpuBo spuBo, List<Sku> skus) {
		for (Sku sku:skus) {
			sku.setSpuId(spuBo.getId());
			sku.setCreateTime(new Date());
			sku.setLastUpdateTime(new Date());
			skuMapper.insertSelective(sku);

			Stock stock = new Stock();

			stock.setSkuId(sku.getId());
			stock.setStock(sku.getStock());
			stockMapper.insertSelective(stock);
		}
	}

	public SpuDetail querySpuDetailBySpuId(Long id) {
		return spuDetailMapper.selectByPrimaryKey(id);
	}

	public List<Sku> querySkuBySpuId(Long spuId) {
		Sku sku = new Sku();
		sku.setSpuId(spuId); /*相当于通过spu找到对应sku*/
		List<Sku> skus = skuMapper.select(sku);
		for (Sku sk:skus) {
			Stock stock = stockMapper.selectByPrimaryKey(sk.getId());
			sk.setStock(stock.getStock()); /*更新库存*/
		}
		return skus;
	}

	@Transactional /*修改服务, 添加事物, 全部更新并且删除旧库存和旧sku*/
	public void updateGoods(SpuBo spuBo) {
		spuBo.setLastUpdateTime(new Date()); /*设定最后修改时间*/
		spuMapper.updateByPrimaryKey(spuBo); /*更新spu*/
		spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail()); /*更新spuMapper*/
		//update tb_spu_detail set description=spuBo.getSpuDetail().getdescription where spu_id=2
		Sku sku = new Sku();
		sku.setSpuId(spuBo.getId()); /*sku的id进行同步*/
		List<Sku> skuList = skuMapper.select(sku);
		if (!CollectionUtils.isEmpty(skuList)) {
			List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList()); //2600242 2600248
//          等同于:
//          List<Long> list = new ArrayList<>();
//          for(Sku s:skuList){
//              list.add(s.getId());
//          }
			stockMapper.deleteByIdList(ids); /*删除库存*/
			skuMapper.deleteByIdList(ids); /*删除sku*/
		}
		saveSkus(spuBo, spuBo.getSkus());
	}

	public Spu querySpuById(Long spuId) {
		return spuMapper.selectByPrimaryKey(spuId);
	}
}
