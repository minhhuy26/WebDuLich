package com.dulich.ticketmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public CommandLineRunner databaseConnectionCheck(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                log.info("==================================================================");
                log.info(" [SUCCESS] KẾT NỐI DATABASE SQL SERVER THÀNH CÔNG!");
                log.info("  Database Product : {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
                log.info("  JDBC Driver      : {} {}", metaData.getDriverName(), metaData.getDriverVersion());
                log.info("  Database URL     : {}", metaData.getURL());
                log.info("  Current User     : {}", metaData.getUserName());
                log.info("==================================================================");
            } catch (Exception ex) {
                log.error("==================================================================");
                log.error(" [ERROR] KHÔNG THỂ KẾT NỐI ĐẾN SQL SERVER!");
                log.error(" Chi tiết lỗi: {}", ex.getMessage());
                log.error(" Vui lòng kiểm tra cổng 1433, user 'sa' và biến môi trường DB_PASSWORD");
                log.error("==================================================================");
            }
        };
    }
}
