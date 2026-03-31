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

/**
 * 处理未落 user_account 主档、但仍保留旧 userId 的孤立钱包演示数据。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class V65__normalize_orphan_demo_wallet_user_ids extends BaseJavaMigration {

    /** 用户ID */
    private static final Map<Long, Long> USER_ID_MAPPING = createUserIdMapping();

    /**
     * 处理业务数据。
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        validateMapping();
        createTemporaryMappingTable(connection);
        try {
            insertMappings(connection);
            updateAllUserIdReferences(connection);
        } finally {
            dropTemporaryMappingTable(connection);
        }
    }

    private static Map<Long, Long> createUserIdMapping() {
        Map<Long, Long> mapping = new LinkedHashMap<>();
        mapping.put(880205068328980205L, 880900068428800000L);
        mapping.put(880306068328980306L, 880913068428800013L);
        mapping.put(880909068328980909L, 880921068428800021L);
        return mapping;
    }

    private void validateMapping() {
        for (Long newUserId : USER_ID_MAPPING.values()) {
            if (!StructuredUserIdGenerator.isStructuredUserId(newUserId)) {
                throw new IllegalStateException("invalid structured userId mapping target: " + newUserId);
            }
            String raw = Long.toString(newUserId);
            if (!StructuredUserIdGenerator.TYPE_SYSTEM.equals(raw.substring(2, 4))) {
                throw new IllegalStateException("orphan demo wallet userId must use type 09: " + newUserId);
            }
        }
    }

    private void createTemporaryMappingTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TEMPORARY TABLE tmp_orphan_demo_user_id_mapping (
                        old_user_id BIGINT PRIMARY KEY,
                        new_user_id BIGINT NOT NULL UNIQUE
                    )
                    """);
        }
    }

    private void insertMappings(Connection connection) throws SQLException {
        String sql = "INSERT INTO tmp_orphan_demo_user_id_mapping (old_user_id, new_user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<Long, Long> entry : USER_ID_MAPPING.entrySet()) {
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
                        + " t JOIN tmp_orphan_demo_user_id_mapping m ON t." + quoted(column.columnName())
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

    private void dropTemporaryMappingTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TEMPORARY TABLE IF EXISTS tmp_orphan_demo_user_id_mapping");
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
