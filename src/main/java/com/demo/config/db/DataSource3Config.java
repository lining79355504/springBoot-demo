package com.demo.config.db;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author mort
 * @Description
 * @date 2023/2/20
 **/
@Configuration
// 指定主数据库扫描对应的Mapper文件，生成代理对象
@MapperScan(basePackages ="com.demo.dao" ,sqlSessionFactoryRef = "dataSource3SqlSessionFactory")
public class DataSource3Config {


    // mapper.xml所在地址
    private static final String MAPPER_LOCATION = "classpath*:mapper/*.xml";


    /**
     * 主数据源，Primary注解必须增加，它表示该数据源为默认数据源
     * 项目中还可能存在其他的数据源，如获取时不指定名称，则默认获取这个数据源，如果不添加，则启动时候回报错
     */
//    @Primary
    @Bean(name = "dataSource3")
    // 读取spring.datasource.datasource3前缀的配置文件映射成对应的配置对象
    @ConfigurationProperties(prefix = "spring.datasource.datasource3")
    public DataSource dataSource() {
        DataSource build = DataSourceBuilder.create().build();
        return build;
    }

    /**
     * 事务管理器，Primary注解作用同上
     */
    @Bean(name = "dataSource3TransactionManager")
//    @Primary
    public PlatformTransactionManager dataSourceTransactionManager(@Qualifier("dataSource3") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * session工厂，Primary注解作用同上
     */

    @Bean(name = "dataSource3SqlSessionFactory")
//    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource3") DataSource dataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(DataSource3Config.MAPPER_LOCATION));
        return sessionFactoryBean.getObject();
    }

}
