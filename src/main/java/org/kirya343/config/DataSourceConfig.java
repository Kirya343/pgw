package org.kirya343.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // Основные параметры подключения
        config.setJdbcUrl("jdbc:mysql://89.35.130.222:3306/s156_PGWSite?useSSL=false&serverTimezone=UTC");
        config.setUsername("u156_kOg0BtJwSj");
        config.setPassword("IY+BHWniVPjzVtreUER+@KwS");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Детальные настройки пула
        config.setPoolName("MainHikariPool");
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(30000);
        config.setInitializationFailTimeout(10000);

        // Важные параметры для корректного отображения метаданных
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "true");

        return new HikariDataSource(config);
    }
}