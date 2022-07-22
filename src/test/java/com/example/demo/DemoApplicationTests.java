package com.example.demo;

import com.google.common.collect.Lists;
import com.service.es.AccTradingFlowDayPo;
import com.service.es.EsOpService;
import com.service.es.TradingFlowDataToEsRepository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	 private static final Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);


	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	RestHighLevelClient highLevelClient;

	@Autowired
	private EsOpService esOpService;


	@Autowired
	private ElasticsearchRestTemplate elasticsearchRestTemplate;

	@Autowired
	private TradingFlowDataToEsRepository tradingFlowDataToEsRepository;

	@Test
	public void contextLoads() {
	}

	@Test
	public void highLevelClientTest(){

		List<AccTradingFlowDayPo> pos = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			pos.add(AccTradingFlowDayPo.builder()
					.id(String.valueOf(UUID.randomUUID().hashCode()))
					.cash_balance(0L)
					.account_id(1)
					.ctime(new Timestamp(System.currentTimeMillis()))
					.mtime(new Timestamp(System.currentTimeMillis()))
					.build());
		}
		esOpService.bulk("acc_account_trading_flow_day_v1", pos);
	}



	@Test
	public void esSave(){
		List<AccTradingFlowDayPo> esData = new ArrayList<>();
		//根据_id insert or update 索引根据po里的设置注解获取 index之后会refresh
		tradingFlowDataToEsRepository.saveAll(esData);

		BoolQueryBuilder queryBuilder = boolQuery();
		RangeQueryBuilder rangeQueryBuilder = rangeQuery("ctime")
				.from(System.currentTimeMillis())
				.to(System.currentTimeMillis());
		queryBuilder.must(rangeQueryBuilder);

		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
//				.withIndices("acc_creative_day")
				.withQuery(queryBuilder).build();
		// scroll delete_by_query
		elasticsearchRestTemplate.delete(searchQuery, AccTradingFlowDayPo.class);



		//指定索引保存 不refresh
		elasticsearchRestTemplate.save(esData, IndexCoordinates.of("acc_account_trading_flow_day_v1"));
//		tradingFlowDataToEsRepository.deleteById();  es 删除并不释放磁盘 只是不能查到 发生index merge后才会删除




		// scroll query
		IndexCoordinates index = IndexCoordinates.of("sample-index");
		NativeSearchQuery scrollSearchQuery = new NativeSearchQueryBuilder()
				.withQuery(rangeQueryBuilder)
				.withFields("message")
				.withPageable(PageRequest.of(0, 10))
				.build();

		SearchHitsIterator<AccTradingFlowDayPo> stream = elasticsearchTemplate.searchForStream(scrollSearchQuery, AccTradingFlowDayPo.class, index);

		List<AccTradingFlowDayPo> sampleEntities = new ArrayList<>();
		while (stream.hasNext()) {
			SearchHit<AccTradingFlowDayPo> next = stream.next();
			AccTradingFlowDayPo content = next.getContent();
			sampleEntities.add(content);
		}
		stream.close();



		// scroll查询 可以获取带scrollId的操作
		SearchScrollHits<AccTradingFlowDayPo> scroll = elasticsearchTemplate.searchScrollStart(1000, searchQuery, AccTradingFlowDayPo.class, index);

		String scrollId = scroll.getScrollId();
		List<AccTradingFlowDayPo> sampleEntities1 = new ArrayList<>();
		while (scroll.hasSearchHits()) {
			List<SearchHit<AccTradingFlowDayPo>> searchHits = scroll.getSearchHits();
			List<AccTradingFlowDayPo> flowDayPos = searchHits.stream().map(r -> r.getContent()).collect(Collectors.toList());
			sampleEntities1.addAll(flowDayPos);
			scrollId = scroll.getScrollId();
			scroll = elasticsearchTemplate.searchScrollContinue(scrollId, 1000, AccTradingFlowDayPo.class, index);
		}
		elasticsearchTemplate.searchScrollClear(Lists.newArrayList(scrollId));



		SearchRequest searchRequest = new SearchRequest("acc_account_trading_flow_day_v1");
		searchRequest.scroll(TimeValue.timeValueMinutes(2L));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		///3、指定每页查询条数
		searchSourceBuilder.size(20);
		//3.1、排序
		searchSourceBuilder.sort("id", SortOrder.ASC);
		//3.2、查询全部
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());

		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
			String scrollId1 = searchResponse.getScrollId();
			logger.info("scrollId1 {} ", scrollId1);
			for (org.elasticsearch.search.SearchHit hit : searchResponse.getHits().getHits()) {
				//4.1、首页数据
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//				list.add(sourceAsMap);
			}


			while (true){
				SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId1);
				searchScrollRequest.scroll(TimeValue.timeValueMinutes(2L));

				SearchResponse scroll1 = highLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
				org.elasticsearch.search.SearchHit[] hits = scroll1.getHits().getHits();

				if (null == hits || hits.length < 1) {
					break;
				}

				for (org.elasticsearch.search.SearchHit hit : hits) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();
					//				list.add(sourceAsMap);
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}
