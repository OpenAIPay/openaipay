package db.migration;

import cn.openaipay.application.user.id.StructuredUserIdGenerator;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 规范化早期演示/测试账号的无效 userId，并同步所有引用列和派生文本字段。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class V64__normalize_invalid_demo_user_ids extends BaseJavaMigration {

    private static final Set<String> STRING_COLUMN_TYPES = Set.of(
            "char",
            "varchar",
            "tinytext",
            "text",
            "mediumtext",
            "longtext"
    );

    /** 用户ID */
    private static final Map<Long, Long> USER_ID_MAPPING = createUserIdMapping();

    /**
     * 处理业务数据。
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, "user_account")) {
            return;
        }

        validateMapping();
        Set<Long> existingUserIds = loadExistingUserIds(connection);
        Map<Long, Long> applicableMapping = buildApplicableMapping(existingUserIds);
        if (applicableMapping.isEmpty()) {
            return;
        }

        createTemporaryMappingTable(connection);
        try {
            insertMappings(connection, applicableMapping);
            updateAllUserIdReferences(connection);
            replaceStringReferences(connection, applicableMapping);
            syncMappedAipayUid(connection);
        } finally {
            dropTemporaryMappingTable(connection);
        }
    }

    private static Map<Long, Long> createUserIdMapping() {
        Map<Long, Long> mapping = new LinkedHashMap<>();
        mapping.put(880100068483692130L, 880902068943900002L);
        mapping.put(880205068328980205L, 880900068428800000L);
        mapping.put(880306068328980306L, 880913068428800013L);
        mapping.put(880909068328980909L, 880921068428800021L);
        mapping.put(881100068483692110L, 880903068495400103L);
        mapping.put(881101068483692111L, 880911068495400111L);
        mapping.put(881102068483692112L, 880924068495400124L);
        mapping.put(881103068483692113L, 880932068495400132L);
        mapping.put(881104068483692114L, 880940068495400140L);
        mapping.put(881105068483692115L, 880952068495400152L);
        mapping.put(881106068483692116L, 880960068495400160L);
        mapping.put(881107068483692117L, 880973068495400173L);
        mapping.put(881108068483692118L, 880981068495400181L);
        mapping.put(881109068483692119L, 880994068495400194L);
        mapping.put(881110068483692120L, 880902068495400302L);
        mapping.put(881111068483692121L, 880910068495400310L);
        return mapping;
    }

    private void validateMapping() {
        for (Map.Entry<Long, Long> entry : USER_ID_MAPPING.entrySet()) {
            long newUserId = entry.getValue();
            if (!StructuredUserIdGenerator.isStructuredUserId(newUserId)) {
                throw new IllegalStateException("invalid structured userId mapping target: " + newUserId);
            }
            String raw = Long.toString(newUserId);
            if (!StructuredUserIdGenerator.TYPE_SYSTEM.equals(raw.substring(2, 4))) {
                throw new IllegalStateException("demo/test userId must use type 09: " + newUserId);
            }
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = """
                SELECT COUNT(1)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Set<Long> loadExistingUserIds(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM user_account");
             ResultSet rs = statement.executeQuery()) {
            java.util.LinkedHashSet<Long> result = new java.util.LinkedHashSet<>();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        }
    }

    private Map<Long, Long> buildApplicableMapping(Set<Long> existingUserIds) {
        Map<Long, Long> applicable = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> entry : USER_ID_MAPPING.entrySet()) {
            Long oldUserId = entry.getKey();
            Long newUserId = entry.getValue();
            if (!existingUserIds.contains(oldUserId)) {
                continue;
            }
            if (existingUserIds.contains(newUserId)) {
                throw new IllegalStateException("target userId already exists, cannot normalize safely: " + newUserId);
            }
            applicable.put(oldUserId, newUserId);
        }
        return applicable;
    }

    private void createTemporaryMappingTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TEMPORARY TABLE tmp_demo_user_id_mapping (
                        old_user_id BIGINT PRIMARY KEY,
                        new_user_id BIGINT NOT NULL UNIQUE
                    )
                    """);
        }
    }

    private void insertMappings(Connection connection, Map<Long, Long> mapping) throws SQLException {
        String sql = "INSERT INTO tmp_demo_user_id_mapping (old_user_id, new_user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<Long, Long> entry : mapping.entrySet()) {
                statement.setLong(1, entry.getKey());
                statement.setLong(2, entry.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void updateAllUserIdReferences(Connection connection) throws SQLException {
        List<TableColumn> columns = findUserIdColumns(connection);
        try (Statement statement = connection.createStatement()) {
            for (TableColumn column : columns) {
                String sql = "UPDATE " + quoted(column.tableName())
                        + " t JOIN tmp_demo_user_id_mapping m ON t." + quoted(column.columnName())
                        + " = m.old_user_id SET t." + quoted(column.columnName()) + " = m.new_user_id";
                statement.executeUpdate(sql);
            }
        }
    }

    private List<TableColumn> findUserIdColumns(Connection connection) throws SQLException {
        String sql = """
                SELECT TABLE_NAME, COLUMN_NAME
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND COLUMN_NAME LIKE '%user_id'
                  AND DATA_TYPE IN ('bigint', 'int', 'integer', 'decimal', 'numeric')
                ORDER BY TABLE_NAME, COLUMN_NAME
                """;
        List<TableColumn> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                columns.add(new TableColumn(rs.getString("TABLE_NAME"), rs.getString("COLUMN_NAME")));
            }
        }
        return columns;
    }

    private void replaceStringReferences(Connection connection, Map<Long, Long> mapping) throws SQLException {
        List<TableColumn> columns = findStringColumns(connection);
        try (Statement statement = connection.createStatement()) {
            for (TableColumn column : columns) {
                String identifier = quoted(column.columnName());
                String expression = identifier;
                for (Map.Entry<Long, Long> entry : mapping.entrySet()) {
                    expression = "REPLACE(" + expression + ", '" + entry.getKey() + "', '" + entry.getValue() + "')";
                }
                String where = mapping.keySet().stream()
                        .map(oldUserId -> identifier + " LIKE '%" + oldUserId + "%'")
                        .collect(Collectors.joining(" OR "));
                String sql = "UPDATE " + quoted(column.tableName())
                        + " SET " + identifier + " = " + expression
                        + " WHERE " + identifier + " IS NOT NULL AND (" + where + ")";
                statement.executeUpdate(sql);
            }
        }
    }

    private List<TableColumn> findStringColumns(Connection connection) throws SQLException {
        String inClause = STRING_COLUMN_TYPES.stream()
                .sorted()
                .map(type -> "'" + type + "'")
                .collect(Collectors.joining(", "));
        String sql = "SELECT TABLE_NAME, COLUMN_NAME "
                + "FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME <> 'flyway_schema_history' "
                + "AND DATA_TYPE IN (" + inClause + ") "
                + "ORDER BY TABLE_NAME, COLUMN_NAME";
        List<TableColumn> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                columns.add(new TableColumn(rs.getString("TABLE_NAME"), rs.getString("COLUMN_NAME")));
            }
        }
        return columns;
    }

    private void syncMappedAipayUid(Connection connection) throws SQLException {
        String sql = """
                UPDATE user_account u
                JOIN tmp_demo_user_id_mapping m ON u.user_id = m.new_user_id
                SET u.aipay_uid = CAST(u.user_id AS CHAR(32))
                WHERE u.aipay_uid IS NULL
                   OR TRIM(u.aipay_uid) = ''
                   OR u.aipay_uid = CAST(m.old_user_id AS CHAR(32))
                   OR u.aipay_uid = CAST(m.new_user_id AS CHAR(32))
                """;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void dropTemporaryMappingTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TEMPORARY TABLE IF EXISTS tmp_demo_user_id_mapping");
        }
    }

    private String quoted(String identifier) {
        return "`" + Objects.requireNonNull(identifier).replace("`", "``") + "`";
    }

    private record TableColumn(
        /** table名称 */
        String tableName,
        /** column名称 */
        String columnName
    ) {
    }
}
