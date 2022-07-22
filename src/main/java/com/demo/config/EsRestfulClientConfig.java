package com.demo.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

/**
 * @author mort
 * @Description
 * @date 2022/7/22
 **/
@Configuration
public class EsRestfulClientConfig extends AbstractElasticsearchConfiguration {


    @Override
    @Bean("elasticsearchClientAlpha")
    public RestHighLevelClient elasticsearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("127.0.0.1:9200")
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

}
