package com.service.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author: mort
 * @time: 2021/3/17 2:43 下午
 */
public interface TradingFlowDataToEsRepository extends ElasticsearchRepository<AccTradingFlowDayPo, String> {
}
