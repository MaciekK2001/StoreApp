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
            @Value("${spring.datasource.driver-class-name:}") String testDriverClassName
    ) {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl)) {
            databaseUrl = testUrl;
        }

        if (!StringUtils.hasText(databaseUrl)) {
            throw new IllegalStateException(
                    "Brak konfiguracji bazy danych! Ustaw zmienną środowiskową DATABASE_URL z Rendera."
            );
        }

        HikariConfig config = new HikariConfig();

        // Obsługa formatu Render: postgresql://user:password@host:port/dbname lub postgres://...
        if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
            log.info("Wykryto format URL bazy danych Render (postgres://)");
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
                }

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (StringUtils.hasText(uri.getQuery())) {
                    jdbcUrl += "?" + uri.getQuery();
                }

                config.setDriverClassName("org.postgresql.Driver");
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                log.info("Skonfigurowano połączenie JDBC: jdbc:postgresql://{}:{}{}", host, port, path);
            } catch (Exception e) {
                log.error("Błąd parsowania adresu URL bazy: {}, używam bezpośredniej konfiguracji", databaseUrl, e);
                config.setJdbcUrl(databaseUrl);
            }
        } else {
            // Dla bazy testowej (np. H2) lub bezpośredniego adresu JDBC
            if (StringUtils.hasText(testDriverClassName)) {
                config.setDriverClassName(testDriverClassName);
            } else if (databaseUrl.startsWith("jdbc:postgresql:")) {
                config.setDriverClassName("org.postgresql.Driver");
            }
            config.setJdbcUrl(databaseUrl);
        }

        return new HikariDataSource(config);
    }
}
