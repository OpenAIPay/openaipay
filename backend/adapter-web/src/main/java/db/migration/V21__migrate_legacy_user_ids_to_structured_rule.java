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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 将历史用户ID迁移到18位复合型新规则，并同步所有引用列。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class V21__migrate_legacy_user_ids_to_structured_rule extends BaseJavaMigration {

    /** 待迁移的目标字段集合。 */
    private static final Set<String> TARGET_COLUMNS = Set.of("user_id", "payer_user_id", "payee_user_id");

    /**
     * 处理业务数据。
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, "user_account")) {
            return;
        }

        List<LegacyUser> legacyUsers = loadLegacyUsers(connection);
        if (legacyUsers.isEmpty()) {
            return;
        }

        Set<Long> usedUserIds = loadAllUserIds(connection);
        StructuredUserIdGenerator generator = new StructuredUserIdGenerator();
        Map<Long, Long> idMapping = buildIdMapping(legacyUsers, usedUserIds, generator);
        if (idMapping.isEmpty()) {
            return;
        }

        createMappingTable(connection);
        insertMappings(connection, idMapping);
        updateAllUserIdReferences(connection);
        syncAipayUidForNumericRows(connection);
        dropMappingTable(connection);
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

    private List<LegacyUser> loadLegacyUsers(Connection connection) throws SQLException {
        String sql = "SELECT user_id, aipay_uid, login_id FROM user_account ORDER BY id ASC";
        List<LegacyUser> users = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                long userId = rs.getLong("user_id");
                if (StructuredUserIdGenerator.isStructuredUserId(userId)) {
                    continue;
                }
                users.add(new LegacyUser(
                        userId,
                        rs.getString("aipay_uid"),
                        rs.getString("login_id")
                ));
            }
        }
        return users;
    }

    private Set<Long> loadAllUserIds(Connection connection) throws SQLException {
        String sql = "SELECT user_id FROM user_account";
        Set<Long> userIds = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                userIds.add(rs.getLong(1));
            }
        }
        return userIds;
    }

    private Map<Long, Long> buildIdMapping(List<LegacyUser> legacyUsers,
                                           Set<Long> usedUserIds,
                                           StructuredUserIdGenerator generator) {
        Map<Long, Long> mapping = new LinkedHashMap<>();
        for (LegacyUser legacyUser : legacyUsers) {
            String userTypeCode = resolveTypeCode(legacyUser);
            long newUserId = generateUniqueUserId(generator, userTypeCode, usedUserIds);
            usedUserIds.add(newUserId);
            mapping.put(legacyUser.userId(), newUserId);
        }
        return mapping;
    }

    private long generateUniqueUserId(StructuredUserIdGenerator generator,
                                      String userTypeCode,
                                      Set<Long> usedUserIds) {
        for (int attempt = 0; attempt < 10_000; attempt++) {
            long candidate = generator.generate(userTypeCode);
            if (!usedUserIds.contains(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("failed to generate unique userId for migration");
    }

    private String resolveTypeCode(LegacyUser user) {
        String loginId = normalize(user.loginId());
        String aipayUid = normalize(user.aipayUid());
        if (containsAny(loginId, "agent", "bot", "robot") || containsAny(aipayUid, "agent", "bot", "robot")) {
            return StructuredUserIdGenerator.TYPE_AGENT;
        }
        if (containsAny(loginId, "merchant", "corp", "company") || containsAny(aipayUid, "merchant", "corp", "company")) {
            return StructuredUserIdGenerator.TYPE_MERCHANT;
        }
        if (containsAny(loginId, "test", "mock", "system") || containsAny(aipayUid, "test", "mock", "system")) {
            return StructuredUserIdGenerator.TYPE_SYSTEM;
        }
        return StructuredUserIdGenerator.TYPE_PERSONAL;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private void createMappingTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TEMPORARY TABLE tmp_user_id_mapping (
                    old_user_id BIGINT PRIMARY KEY,
                    new_user_id BIGINT NOT NULL UNIQUE
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void insertMappings(Connection connection, Map<Long, Long> mapping) throws SQLException {
        String sql = "INSERT INTO tmp_user_id_mapping (old_user_id, new_user_id) VALUES (?, ?)";
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
        List<TableColumn> tableColumns = findTargetColumns(connection);
        try (Statement statement = connection.createStatement()) {
            for (TableColumn tableColumn : tableColumns) {
                String updateSql = "UPDATE " + quoted(tableColumn.tableName())
                        + " t JOIN tmp_user_id_mapping m ON t." + quoted(tableColumn.columnName())
                        + " = m.old_user_id SET t." + quoted(tableColumn.columnName()) + " = m.new_user_id";
                statement.executeUpdate(updateSql);
            }
        }
    }

    private List<TableColumn> findTargetColumns(Connection connection) throws SQLException {
        String sql = """
                SELECT TABLE_NAME, COLUMN_NAME
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND COLUMN_NAME IN ('user_id', 'payer_user_id', 'payee_user_id')
                ORDER BY TABLE_NAME, COLUMN_NAME
                """;
        List<TableColumn> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (!TARGET_COLUMNS.contains(columnName)) {
                    continue;
                }
                columns.add(new TableColumn(
                        rs.getString("TABLE_NAME"),
                        columnName
                ));
            }
        }
        return columns;
    }

    private void syncAipayUidForNumericRows(Connection connection) throws SQLException {
        String sql = """
                UPDATE user_account u
                JOIN tmp_user_id_mapping m ON u.user_id = m.new_user_id
                SET u.aipay_uid = CAST(m.new_user_id AS CHAR(64))
                WHERE u.aipay_uid = CAST(m.old_user_id AS CHAR(64))
                """;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void dropMappingTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TEMPORARY TABLE IF EXISTS tmp_user_id_mapping");
        }
    }

    private String quoted(String identifier) {
        return "`" + Objects.requireNonNull(identifier).replace("`", "``") + "`";
    }

    private record LegacyUser(
            /** 用户ID */
            long userId,
            /** 爱支付UID */
            String aipayUid,
            /** 登录账号ID */
            String loginId
    ) {
    }

    private record TableColumn(
            /** table名称 */
            String tableName,
            /** column名称 */
            String columnName
    ) {
    }
}
