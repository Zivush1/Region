package me.zivush.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private Connection connection;

    public DatabaseManager(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

                createTables();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS regions (" +
                    "name VARCHAR(64) PRIMARY KEY," +
                    "world VARCHAR(64)," +
                    "pos1_x DOUBLE," +
                    "pos1_y DOUBLE," +
                    "pos1_z DOUBLE," +
                    "pos2_x DOUBLE," +
                    "pos2_y DOUBLE," +
                    "pos2_z DOUBLE," +
                    "owner VARCHAR(36)," +
                    "BLOCK_BREAK VARCHAR(20) DEFAULT 'NONE'," +
                    "BLOCK_PLACE VARCHAR(20) DEFAULT 'NONE'," +
                    "INTERACT VARCHAR(20) DEFAULT 'NONE'," +
                    "ENTITY_DAMAGE VARCHAR(20) DEFAULT 'NONE'" +
                    ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS region_members (" +
                    "region_name VARCHAR(64)," +
                    "player_uuid VARCHAR(36)," +
                    "PRIMARY KEY (region_name, player_uuid)," +
                    "FOREIGN KEY (region_name) REFERENCES regions(name) ON DELETE CASCADE" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRegion(Region region) {
        try {
            String sql = "INSERT INTO regions (name, world, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z, owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, region.getName());
                stmt.setString(2, region.getPos1().getWorld().getName());
                stmt.setDouble(3, region.getPos1().getX());
                stmt.setDouble(4, region.getPos1().getY());
                stmt.setDouble(5, region.getPos1().getZ());
                stmt.setDouble(6, region.getPos2().getX());
                stmt.setDouble(7, region.getPos2().getY());
                stmt.setDouble(8, region.getPos2().getZ());
                stmt.setString(9, region.getOwner().toString());
                stmt.executeUpdate();
            }

            updateFlags(region);
            updateMembers(region);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRegion(Region region) {
        try {
            connection.setAutoCommit(false);

            String currentName = getCurrentRegionName(region.getName());
            if (!currentName.equals(region.getName())) {
                renameRegion(currentName, region.getName());
            }

            String sql = "UPDATE regions SET world = ?, pos1_x = ?, pos1_y = ?, pos1_z = ?, pos2_x = ?, pos2_y = ?, pos2_z = ?, owner = ? WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, region.getPos1().getWorld().getName());
                pstmt.setDouble(2, region.getPos1().getX());
                pstmt.setDouble(3, region.getPos1().getY());
                pstmt.setDouble(4, region.getPos1().getZ());
                pstmt.setDouble(5, region.getPos2().getX());
                pstmt.setDouble(6, region.getPos2().getY());
                pstmt.setDouble(7, region.getPos2().getZ());
                pstmt.setString(8, region.getOwner().toString());
                pstmt.setString(9, region.getName());
                pstmt.executeUpdate();
            }

            updateFlags(region);
            updateMembers(region);

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCurrentRegionName(String name) throws SQLException {
        String sql = "SELECT name FROM regions WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return name;
    }

    private void updateFlags(Region region) {
        StringBuilder sql = new StringBuilder("UPDATE regions SET ");
        List<String> flagUpdates = new ArrayList<>();
        for (Map.Entry<String, RegionFlag> entry : region.getFlags().entrySet()) {
            flagUpdates.add(entry.getKey() + " = ?");
        }
        sql.append(String.join(", ", flagUpdates));
        sql.append(" WHERE name = ?");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (RegionFlag flag : region.getFlags().values()) {
                pstmt.setString(index++, flag.getState().name());
            }
            pstmt.setString(index, region.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateMembers(Region region) throws SQLException {
        String deleteSql = "DELETE FROM region_members WHERE region_name = ?";
        String insertSql = "INSERT INTO region_members (region_name, player_uuid) VALUES (?, ?)";

        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setString(1, region.getName());
            deleteStmt.executeUpdate();
        }

        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            for (UUID member : region.getMembers()) {
                insertStmt.setString(1, region.getName());
                insertStmt.setString(2, member.toString());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    public List<Region> loadRegions() {
        List<Region> regions = new ArrayList<>();
        String sql = "SELECT * FROM regions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String world = rs.getString("world");
                Location pos1 = new Location(Bukkit.getWorld(world),
                        rs.getDouble("pos1_x"),
                        rs.getDouble("pos1_y"),
                        rs.getDouble("pos1_z"));
                Location pos2 = new Location(Bukkit.getWorld(world),
                        rs.getDouble("pos2_x"),
                        rs.getDouble("pos2_y"),
                        rs.getDouble("pos2_z"));
                UUID owner = UUID.fromString(rs.getString("owner"));

                Region region = new Region(name, pos1, pos2, owner);
                loadFlags(region, rs);
                loadMembers(region);
                regions.add(region);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return regions;
    }

    private void loadFlags(Region region, ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            if (columnName.equals("name") || columnName.equals("world") ||
                    columnName.startsWith("pos") || columnName.equals("owner")) {
                continue;
            }
            String stateStr = rs.getString(columnName);
            if (stateStr != null) {
                region.setFlag(columnName, RegionFlag.State.valueOf(stateStr));
            }
        }
    }

    private void loadMembers(Region region) throws SQLException {
        String sql = "SELECT player_uuid FROM region_members WHERE region_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, region.getName());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UUID memberUUID = UUID.fromString(rs.getString("player_uuid"));
                    region.addMember(memberUUID);
                }
            }
        }
    }

    public void addFlagColumn(String flagName) {
        if (!isValidColumnName(flagName)) {
            throw new IllegalArgumentException("Invalid flag name: " + flagName);
        }
        try {
            String sql = "ALTER TABLE regions ADD COLUMN `" + flagName + "` VARCHAR(20) DEFAULT 'NONE'";
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidColumnName(String name) {
        return name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    public void deleteRegion(String regionName) {
        try {
            String sql = "DELETE FROM regions WHERE name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, regionName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean renameRegion(String oldName, String newName) {
        try {
            connection.setAutoCommit(false);

            List<String> members = new ArrayList<>();
            String selectMembersSql = "SELECT player_uuid FROM region_members WHERE region_name = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectMembersSql)) {
                selectStmt.setString(1, oldName);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        members.add(rs.getString("player_uuid"));
                    }
                }
            }


            String deleteMembersSql = "DELETE FROM region_members WHERE region_name = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteMembersSql)) {
                deleteStmt.setString(1, oldName);
                deleteStmt.executeUpdate();
            }


            String updateRegionSql = "UPDATE regions SET name = ? WHERE name = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateRegionSql)) {
                updateStmt.setString(1, newName);
                updateStmt.setString(2, oldName);
                updateStmt.executeUpdate();
            }

            String insertMembersSql = "INSERT INTO region_members (region_name, player_uuid) VALUES (?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertMembersSql)) {
                for (String member : members) {
                    insertStmt.setString(1, newName);
                    insertStmt.setString(2, member);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
