package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 删除顾郡辅助测试账号，并将反馈单 userId 调整回独立演示标识。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class V67__delete_auxiliary_gujun_user_and_restore_feedback_user_id extends BaseJavaMigration {

    /** 用户ID */
    private static final long GUJUN_ACCOUNT_USER_ID = 880100068483692100L;
    /** 用户ID */
    private static final long GUJUN_FEEDBACK_USER_ID = 880100068483692200L;
    /** 用户ID */
    private static final long GUJUN_AUXILIARY_USER_ID = 8801240301000019L;

    /**
     * 处理业务数据。
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        restoreFeedbackUserId(connection);
        deleteAuxiliaryUserData(connection);
    }

    private void restoreFeedbackUserId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE feedback_ticket SET user_id = ? WHERE user_id = ?")) {
            statement.setLong(1, GUJUN_FEEDBACK_USER_ID);
            statement.setLong(2, GUJUN_ACCOUNT_USER_ID);
            statement.executeUpdate();
        }
    }

    private void deleteAuxiliaryUserData(Connection connection) throws SQLException {
        deleteByUserId(connection, "wallet_account");
        deleteByUserId(connection, "user_privacy_setting");
        deleteByUserId(connection, "user_security_setting");
        deleteByUserId(connection, "user_profile");
        deleteByUserId(connection, "user_account");
    }

    private void deleteByUserId(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM " + tableName + " WHERE user_id = " + GUJUN_AUXILIARY_USER_ID);
        }
    }
}
