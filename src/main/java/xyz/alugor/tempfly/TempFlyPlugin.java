package xyz.alugor.tempfly;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.alugor.tempfly.database.Database;
import xyz.alugor.tempfly.database.entity.TempFly;
import xyz.alugor.tempfly.database.service.TempFlyService;

import java.sql.SQLException;
import java.util.UUID;

public final class TempFlyPlugin extends JavaPlugin {
    @Getter
    private static TempFlyPlugin instance;
    @Getter
    private static Database database;
    private static TempFlyService service;

    @Override
    public void onEnable() {
        instance = this;
        database = new Database("localhost", 3306, "tempfly", "tempfly", "lN6X!j]P*@D/81CU");
        service = new TempFlyService(database);
        createTable();
        insertTestData();
    }

    @Override
    public void onDisable() {
        database.close();
    }

    private void createTable() {
        try {
            service.initialize();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertTestData() {
        TempFly newTempFly = new TempFly(UUID.randomUUID(), "Test TempFly", 3600L, true);
        try {
            service.saveTempFly(newTempFly);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}