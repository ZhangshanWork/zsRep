package im.vinci.server.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by henryhome on 2/16/15.
 */
@Configuration
@MapperScan(value = {"im.vinci.server"},annotationClass = Repository.class)
public class DaoConfiguration {

    @Configuration
    @Profile(UserProfile.INTG)
    @PropertySource("classpath:/intg/jdbc.properties")
    static class MysqlIntgConfiguration {
    }

    @Configuration
    @Profile(UserProfile.QACI)
    @PropertySource("classpath:/qaci/jdbc.properties")
    static class MysqlQaciConfiguration {
    }

    @Configuration
    @Profile(UserProfile.PROD)
    @PropertySource("classpath:/prod/jdbc.properties")
    static class MysqlProdAConfiguration {
    }

    @Autowired
    Environment env;

    @Bean(destroyMethod = "close")
    public DataSource mysqlDataSource() throws SQLException {

        System.err.println("init mysql datasource:"+env.getProperty("jdbc.url"));

        DruidDataSource dataSource = new DruidDataSource();
        String dbUrl = env.getProperty("jdbc.url");
        String dbUsername = env.getProperty("jdbc.username");
        String dbPassword = env.getProperty("jdbc.password");
        dataSource.setDriverClassName(env.getProperty("jdbc.driver", "com.mysql.jdbc.Driver"));
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);

        dataSource.setInitialSize(5);
        dataSource.setMaxActive(20);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);

        dataSource.setTestWhileIdle(true);
        dataSource.setValidationQuery("select 1");
        dataSource.setMaxWait(2000);

        dataSource.setFilters("stat");
        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(mysqlDataSource());
        Resource resource = new ClassPathResource("database/mybatis-config.xml");
        sqlSessionFactory.setConfigLocation(resource);
        return sqlSessionFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager() throws SQLException {
    	DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
    	dataSourceTransactionManager.setDataSource(mysqlDataSource());
    	return dataSourceTransactionManager;
    }
    
    @Bean
    public TransactionTemplate transactionTemplate() throws SQLException {
    	TransactionTemplate transactionTemplate = new TransactionTemplate();
    	transactionTemplate.setTransactionManager(dataSourceTransactionManager());
    	return transactionTemplate;
    }

}



