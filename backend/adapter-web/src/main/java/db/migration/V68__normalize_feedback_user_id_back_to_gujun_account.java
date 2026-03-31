package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 将反馈工单 userId 统一回顾郡主账号。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class V68__normalize_feedback_user_id_back_to_gujun_account extends BaseJavaMigration {

    /** 用户ID */
    private static final long GUJUN_ACCOUNT_USER_ID = 880100068483692100L;
    /** 用户ID */
    private static final long GUJUN_LEGACY_FEEDBACK_USER_ID = 880100068483692200L;

    /**
     * 处理业务数据。
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE feedback_ticket SET user_id = ? WHERE user_id = ?")) {
            statement.setLong(1, GUJUN_ACCOUNT_USER_ID);
            statement.setLong(2, GUJUN_LEGACY_FEEDBACK_USER_ID);
            statement.executeUpdate();
        }
    }
}
