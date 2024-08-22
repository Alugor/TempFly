package xyz.alugor.tempfly;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.alugor.tempfly.commands.TempFlyCommand;
import xyz.alugor.tempfly.database.Database;
import xyz.alugor.tempfly.database.entity.TempFly;
import xyz.alugor.tempfly.database.service.TempFlyService;
import xyz.alugor.tempfly.events.PlayerJoinListener;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class TempFlyPlugin extends JavaPlugin {
    @Getter
    private static TempFlyPlugin instance;
    @Getter
    private static Database database;
    @Getter
    private static TempFlyService service;

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onEnable() {
        instance = this;
        //credentials are only for local xampp database
        database = new Database("localhost", 3306, "tempfly", "tempfly", "lN6X!j]P*@D/81CU");
        service = new TempFlyService(database);

        createTable();
        insertTestData();
        initEvents();
        runThread();
    }

    @Override
    public void onDisable() {
        //stopping the scheduler
        executor.shutdownNow();
        //properly closing the database connection
        database.close();
    }

    private void initEvents() {
        //register commands
        new TempFlyCommand(this);

        //register events
        new PlayerJoinListener(this);
    }

    private void runThread() {
        Runnable task = () -> {
            //iterating every player
            Bukkit.getOnlinePlayers().forEach(player -> {
                //checking if the player has a database entry
                service.getTempFlyByUUID(player.getUniqueId()).ifPresent(tempFly -> {
                    /*
                    removing 1 second of the duration
                    ensure with Math.abs(); that duration never goes negative
                     */
                    tempFly.setDuration(Math.abs(tempFly.getDuration() - 1));
                    //saving object to the database
                    service.saveTempFly(tempFly);
                });
            });
        };
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
    }

    private void createTable() {
        service.initialize();
    }

    private void insertTestData() {
        //adding temporary test data
        TempFly tempFly = new TempFly(UUID.randomUUID(), 3600L, true);
        service.saveTempFly(tempFly);
    }
}