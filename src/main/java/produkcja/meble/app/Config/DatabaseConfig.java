package produkcja.meble.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Slf4j
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url:}") String testUrl,
            @Value("${spring.datasource.username:}") String testUsername,
            @Value("${spring.datasource.password:}") String testPassword,
            @Value("${spring.datasource.driver-class-name:}") String testDriverClassName
    ) {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl)) {
            databaseUrl = testUrl;
        }

        if (!StringUtils.hasText(databaseUrl)) {
            throw new IllegalStateException(
                    "Brak konfiguracji bazy danych! Ustaw zmienną środowiskową DATABASE_URL z Rendera " +
                    "lub skonfiguruj właściwości spring.datasource.url w application-dev.properties."
            );
        }

        HikariConfig config = new HikariConfig();

        // Obsługa formatu Render/Docker: postgresql://user:password@host:port/dbname lub postgres://...
        if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
            log.info("Wykryto format URL bazy danych postgres:// lub postgresql://");
            try {
                String cleanUrl = databaseUrl;
                if (cleanUrl.startsWith("postgres://")) {
                    cleanUrl = "postgresql://" + cleanUrl.substring("postgres://".length());
                }
                URI uri = new URI(cleanUrl);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String userInfo = uri.getUserInfo();

                String username = "";
                String password = "";
                if (StringUtils.hasText(userInfo)) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    if (parts.length > 1) {
                        password = parts[1];
                    }
                } else {
                    username = testUsername;
                    password = testPassword;
                }

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (StringUtils.hasText(uri.getQuery())) {
                    jdbcUrl += "?" + uri.getQuery();
                }

                config.setDriverClassName("org.postgresql.Driver");
                config.setJdbcUrl(jdbcUrl);
                if (StringUtils.hasText(username)) {
                    config.setUsername(username);
                }
                if (StringUtils.hasText(password)) {
                    config.setPassword(password);
                }
                log.info("Skonfigurowano połączenie JDBC: jdbc:postgresql://{}:{}{}", host, port, path);
            } catch (Exception e) {
                log.error("Błąd parsowania adresu URL bazy: {}, używam bezpośredniej konfiguracji", databaseUrl, e);
                config.setJdbcUrl(databaseUrl);
                if (StringUtils.hasText(testUsername)) config.setUsername(testUsername);
                if (StringUtils.hasText(testPassword)) config.setPassword(testPassword);
            }
        } else {
            // Dla bazy testowej (np. H2) lub standardowego adresu JDBC (np. jdbc:postgresql://localhost:5432/mebledb)
            if (StringUtils.hasText(testDriverClassName)) {
                config.setDriverClassName(testDriverClassName);
            } else if (databaseUrl.startsWith("jdbc:postgresql:")) {
                config.setDriverClassName("org.postgresql.Driver");
            }
            config.setJdbcUrl(databaseUrl);
            if (StringUtils.hasText(testUsername)) {
                config.setUsername(testUsername);
            }
            if (StringUtils.hasText(testPassword)) {
                config.setPassword(testPassword);
            }
        }

        return new HikariDataSource(config);
    }
}
