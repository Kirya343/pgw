package org.workswap.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "org.workswap.datasource.stats.repository",
    entityManagerFactoryRef = "statsEntityManagerFactory",
    transactionManagerRef = "statsTransactionManager"
)
public class StatsDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.statistics-datasource")
    public DataSource statsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean statsEntityManagerFactory(
            EntityManagerFactoryBuilder builder
    ) {
        return builder
                .dataSource(statsDataSource())
                .packages("org.workswap.datasource.stats.model") // Пакет с @Entity статистики
                .persistenceUnit("stats")
                .build();
    }

    @Bean
    public PlatformTransactionManager statsTransactionManager(
            @Qualifier("statsEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}
