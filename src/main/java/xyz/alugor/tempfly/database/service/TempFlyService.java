package xyz.alugor.tempfly.database.service;

import lombok.RequiredArgsConstructor;
import xyz.alugor.tempfly.database.Database;
import xyz.alugor.tempfly.database.entity.TempFly;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TempFlyService {
    private final Database database;

    public List<TempFly> getAllTempFlies() {
        try {
            return database.executeQuery(
                    "SELECT * FROM temp_fly",
                    this::mapResultSetToTempFly
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeTempFlyTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS temp_fly (" +
                "uuid CHAR(36) PRIMARY KEY, " +
                "duration BIGINT NOT NULL, " +
                "status BOOLEAN NOT NULL" +
                ")";

        try {
            database.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createIfNotExists(UUID uuid) {
        if (getTempFlyByUUID(uuid).isEmpty()) {
            TempFly tempFly = new TempFly(UUID.randomUUID(), 0L, false);
            saveTempFly(tempFly);
        }
    }

    public void saveTempFly(TempFly tempFly) {
        try {
            database.executeUpdate(
                    "INSERT INTO temp_fly (uuid, duration, status) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE duration = ?, status = ?",
                    tempFly.getUuid().toString(), tempFly.getDuration(), tempFly.getStatus(),
                    tempFly.getDuration(), tempFly.getStatus()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<TempFly> getTempFlyByUUID(UUID uuid) {
        try {
            return database.executeSingleResultQuery(
                    "SELECT * FROM temp_fly WHERE uuid = ?",
                    this::mapResultSetToTempFly,
                    uuid.toString()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private TempFly mapResultSetToTempFly(ResultSet rs) {
        try {
            return new TempFly(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getLong("duration"),
                    rs.getBoolean("status")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize() {
        initializeTempFlyTable();
    }
}