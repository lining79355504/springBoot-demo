package com.service.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author mort
 * @Description
 * @date 2022/7/22
 **/
@Component
public class EsOpService {

     private static final Logger logger = LoggerFactory.getLogger(EsOpService.class);

    @Qualifier("elasticsearchClientAlpha")
    @Autowired
    RestHighLevelClient highLevelClient;


    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * @param entity
     * @param indexName
     * @param pageSize
     * @param scrollMinutes
     * @param <T>
     * @return
     */
    public <T> List<T> scrollQuery(Class<T> entity, String indexName, Integer pageSize, Integer scrollMinutes) {

        List<T> data = new ArrayList<>();

        SearchRequest searchRequest = new SearchRequest(indexName);
//        searchRequest.types("acctradingflowdaypo");
        searchRequest.scroll(TimeValue.timeValueMinutes(scrollMinutes));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        ///3、指定每页查询条数
        searchSourceBuilder.size(pageSize);
        //3.1、排序
        searchSourceBuilder.sort("id", SortOrder.ASC);
        //3.2、查询全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();
            logger.info("scrollId {} ", scrollId);
            for (org.elasticsearch.search.SearchHit hit : searchResponse.getHits().getHits()) {
                //4.1、首页数据
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();

                T object = JSON.parseObject(hit.getSourceAsString(), entity);
                data.add(object);
            }


            while (true) {
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
                searchScrollRequest.scroll(TimeValue.timeValueMinutes(scrollMinutes));

                SearchResponse scroll = highLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);

                scrollId = scroll.getScrollId();

                org.elasticsearch.search.SearchHit[] hits = scroll.getHits().getHits();

                if (null == hits || hits.length < 1) {
                    break;
                }

                for (org.elasticsearch.search.SearchHit hit : hits) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();

                    T object = JSON.parseObject(hit.getSourceAsString(), entity);
                    data.add(object);
                }
            }


        } catch (Exception e) {
            logger.error("error ", e);
        }

        return data;
    }

    /**
     *
     * @param indexName
     * @param pos
     * @param <?>
     */
    public void bulk(String indexName, List<?> pos) {

        if (CollectionUtils.isEmpty(pos)) {
            return;
        }

        BulkRequest request = new BulkRequest();
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);

        pos.forEach(
                item -> {
                    String string = JSON.toJSONString(item);
                    Map<String, Object> map = JSON.parseObject(string, new TypeReference<Map<String, Object>>() {
                    }.getType());

                    request.add(new IndexRequest(indexName)
                            .id(null == map.get("id") ? null : String.valueOf(map.get("id")))
                            .source(map));
                }
        );

        try {
            highLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Working with Spring Data Repositories
     * @param pos
     * @param <T>
     * @return
     */
    public <T> Iterable<T> saveAll(Iterable<T> pos) {
        return elasticsearchRestTemplate.save(pos);
    }


    /**
     * Working with Spring Data Repositories
     */
    @PostConstruct
    public void setElasticsearchRestTemplate() {
        this.elasticsearchRestTemplate = new ElasticsearchRestTemplate(highLevelClient);
    }
}
