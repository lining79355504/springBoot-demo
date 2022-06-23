package com.service.es;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

/**
 * @author mort
 * @Description
 * @date 2022/1/25
 **/
public class CustomerEsRepository<T, ID> extends SimpleElasticsearchRepository<T, ID> {


    public CustomerEsRepository(ElasticsearchEntityInformation<T, ID> metadata, ElasticsearchOperations operations) {
        super(metadata, operations);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {

        super.deleteAll(entities);
    }
}
