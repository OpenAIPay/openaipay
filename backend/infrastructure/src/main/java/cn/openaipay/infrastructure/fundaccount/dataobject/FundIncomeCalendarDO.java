package cn.openaipay.infrastructure.fundaccount.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import cn.openaipay.domain.shared.number.FundAmount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 基金收益日历持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("fund_income_calendar")
public class FundIncomeCalendarDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 基金产品编码 */
    @TableField("fund_code")
    private String fundCode;

    /** 业务日期 */
    @TableField("biz_date")
    private LocalDate bizDate;

    /** 净值 */
    @TableField("nav")
    private FundAmount nav;

    /** 收益万份 */
    @TableField("income_per_10k")
    private FundAmount incomePer10k;

    /** 日历状态 */
    @TableField("calendar_status")
    private String calendarStatus;

    /** 发布时间 */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    /** 结算时间 */
    @TableField("settled_at")
    private LocalDateTime settledAt;

    /** 锁版本 */
    @Version
    @TableField("lock_version")
    private Long lockVersion;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
