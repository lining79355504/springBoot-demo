package com.example.demo;

import com.service.es.AccTradingFlowDayPo;
import com.service.es.TradingFlowDataToEsRepository;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {


	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;


	@Autowired
	private ElasticsearchRestTemplate elasticsearchRestTemplate;

	@Autowired
	private TradingFlowDataToEsRepository tradingFlowDataToEsRepository;

	@Test
	public void contextLoads() {
	}



	@Test
	public void esSave(){
		List<AccTradingFlowDayPo> esData = new ArrayList<>();
		tradingFlowDataToEsRepository.saveAll(esData);   //根据_id insert or update

		BoolQueryBuilder queryBuilder = boolQuery();
		queryBuilder.must(rangeQuery("ctime")
				.from(System.currentTimeMillis())
				.to(System.currentTimeMillis()));

		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
//				.withIndices("acc_creative_day")
				.withQuery(queryBuilder).build();

		elasticsearchRestTemplate.delete(searchQuery, AccTradingFlowDayPo.class);

		//按时间分索引保存
		elasticsearchRestTemplate.save(esData, IndexCoordinates.of("acc_account_trading_flow_day_v1"));
//		tradingFlowDataToEsRepository.deleteById();  es 删除并不释放磁盘 只是不能查到 发生index merge后才会删除

	}
}
