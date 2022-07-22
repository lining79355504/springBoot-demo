package com.service.es;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author mort
 * @Description
 * @date 2022/2/14
 *
 *
 * <dependency>
 * <groupId>org.springframework.data</groupId>
 * <artifactId>spring-data-elasticsearch</artifactId>
 * <version>4.3.1</version>
 * </dependency>
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 * <beans xmlns="http://www.springframework.org/schema/beans"
 *        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *        xmlns:elasticsearch="http://www.springframework.org/schema/data/elasticsearch"
 *        xsi:schemaLocation="http://www.springframework.org/schema/beans
 *        https://www.springframework.org/schema/beans/spring-beans-3.1.xsd
 *        http://www.springframework.org/schema/data/elasticsearch
 *        https://www.springframework.org/schema/data/elasticsearch/spring-elasticsearch-1.0.xsd">
 *
 *   transport 配置 es 8.0 会移除
 *   <!--es dao repository 扫描 -- >
 *   <elasticsearch:repositories base-package="com.acme.repositories" />
 *     <!--transport 集群配置 -->
 *   <elasticsearch:transport-client id="client" cluster-nodes="localhost:9300,someip:9300" />
 *
 *      <!--ElasticsearchTemplate bean-->
 *       <bean name="elasticsearchTemplateSkywalk" class="org.springframework.data.elasticsearch.core.ElasticsearchTemplate">
 *         <constructor-arg name="client" ref="client"/>
 *     </bean>
 * </beans>
 *
 *    @Autowired
 *    private ElasticsearchTemplate elasticsearchTemplate;
 *
 **/
public class EsDevUtils {

    private static final Logger logger = LoggerFactory.getLogger(EsDevUtils.class);

    public void createIndexByMonth(Timestamp date, String indexName, String indexAliasesName,
                                   int day, int shards, int replicas, ElasticsearchRestTemplate elasticsearchTemplate) {

        Timestamp someDayAfterToday = new Timestamp(date.getTime() + day * 24 * 60 * 60 * 1000L);
        String newIndexName = indexName + "_" + new SimpleDateFormat("yyyy_MM").format(new Date(someDayAfterToday.getTime()));
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(IndexCoordinates.of(indexName));
        if (!indexOperations.exists()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("number_of_shards", shards);
            map.put("number_of_replicas", replicas);
            map.put("refresh_interval", "30s");  //设置自动refresh时间30s
            indexOperations.putMapping(AccTradingFlowDayPo.class);
            boolean result = indexOperations.create(map);
            logger.info("job putMapping currentDateIndex newIndex is {}.", newIndexName);

            AliasActions aliasActions = new AliasActions();
            aliasActions.add(new AliasAction.Add(AliasActionParameters.builder()
                    .withAliases(indexAliasesName)
                    .withIndices(indexName)
//                    .withIsHidden()
//                    .withIsWriteIndex()
//                    .withRouting()
                    .build()));
            boolean aliasResult = indexOperations.alias(aliasActions);
//            indexOperations.refresh();
            logger.info("job create currentDateIndex newIndex is {} , create result  is {}  aliasResult is {} ", newIndexName, result, aliasResult);
        }
    }

    public void bulk(ElasticsearchRestTemplate elasticsearchTemplate, List<AccTradingFlowDayPo> datas) {
        elasticsearchTemplate.save(datas);
    }

}
