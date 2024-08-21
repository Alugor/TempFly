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

    public List<TempFly> getAllTempFlies() throws SQLException {
        return database.executeQuery(
                "SELECT * FROM temp_fly",
                this::mapResultSetToTempFly
        );
    }

    public void initializeTempFlyTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS temp_fly (" +
                "uuid CHAR(36) PRIMARY KEY, " +
                "name VARCHAR(32) NOT NULL, " +
                "duration BIGINT NOT NULL, " +
                "status BOOLEAN NOT NULL" +
                ")";

        database.executeUpdate(createTableSQL);
    }

    public void saveTempFly(TempFly tempFly) throws SQLException {
        database.executeUpdate(
                "INSERT INTO temp_fly (uuid, name, duration, status) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = ?, duration = ?, status = ?",
                tempFly.getUuid().toString(), tempFly.getName(), tempFly.getDuration(), tempFly.getStatus(),
                tempFly.getName(), tempFly.getDuration(), tempFly.getStatus()
        );
    }

    public Optional<TempFly> getTempFlyByUUID(UUID uuid) throws SQLException {
        return database.executeSingleResultQuery(
                "SELECT * FROM temp_fly WHERE uuid = ?",
                this::mapResultSetToTempFly,
                uuid.toString()
        );
    }

    private TempFly mapResultSetToTempFly(ResultSet rs) throws SQLException {
        return new TempFly(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getLong("duration"),
                rs.getBoolean("status")
        );
    }

    public void initialize() throws SQLException {
        initializeTempFlyTable();
    }
}