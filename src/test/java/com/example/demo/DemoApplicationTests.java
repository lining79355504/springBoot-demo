package com.example.demo;

import com.service.es.AccTradingFlowDayPo;
import com.service.es.TradingFlowDataToEsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

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

//		tradingFlowDataToEsRepository.deleteById();  es 删除并不释放磁盘 只是不能查到

	}
}
