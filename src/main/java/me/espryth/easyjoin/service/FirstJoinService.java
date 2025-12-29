package me.espryth.easyjoin.service;

import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.espryth.easyjoin.config.AppConfig;
import me.espryth.easyjoin.config.CredentialsConfig;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class FirstJoinService {

    private final Container<AppConfig> appConfig;
    private final Container<CredentialsConfig> credentialsConfig;
    private HikariDataSource dataSource;
    private final Set<UUID> cache = new HashSet<>();

    public FirstJoinService(Container<AppConfig> appConfig, Container<CredentialsConfig> credentialsConfig) {
        this.appConfig = appConfig;
        this.credentialsConfig = credentialsConfig;
        setupDatabase();
    }

    private void setupDatabase() {
        if (!appConfig.get().firstJoinMode().equalsIgnoreCase("MYSQL")) return;

        CredentialsConfig creds = credentialsConfig.get();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + creds.host() + ":" + creds.port() + "/" + creds.database() + "?useSSL=" + creds.useSsl());
        config.setUsername(creds.username());
        config.setPassword(creds.password());
        config.setMaximumPoolSize(10);

        this.dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS EJFirstJoin(uuid VARCHAR(36) PRIMARY KEY)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Boolean> isFirstJoin(Player player) {
        if (!appConfig.get().firstJoinMode().equalsIgnoreCase("MYSQL")) {
            return CompletableFuture.completedFuture(!player.hasPlayedBefore());
        }

        if (cache.contains(player.getUniqueId())) return CompletableFuture.completedFuture(false);

        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM EJFirstJoin WHERE uuid = ?")) {
                stmt.setString(1, player.getUniqueId().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean exists = rs.next();
                    if (exists) cache.add(player.getUniqueId());
                    return !exists;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return !player.hasPlayedBefore();
            }
        });
    }

    public void markAsJoined(Player player) {
        if (!appConfig.get().firstJoinMode().equalsIgnoreCase("MYSQL")) return;

        cache.add(player.getUniqueId());
        CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT IGNORE INTO EJFirstJoin(uuid) VALUES(?)")) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
