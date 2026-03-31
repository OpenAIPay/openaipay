package cn.openaipay.infrastructure.accounting.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会计科目DO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("acct_subject")
public class AccountingSubjectDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 科目编码 */
    @TableField("subject_code")
    private String subjectCode;

    /** 科目名称 */
    @TableField("subject_name")
    private String subjectName;

    /** 科目类型 */
    @TableField("subject_type")
    private String subjectType;

    /** 余额方向 */
    @TableField("balance_direction")
    private String balanceDirection;

    /** 科目编码 */
    @TableField("parent_subject_code")
    private String parentSubjectCode;

    /** 业务单号 */
    @TableField("level_no")
    private Integer levelNo;

    /** 启用标记 */
    @TableField("enabled")
    private Boolean enabled;

    /** 业务备注 */
    @TableField("remark")
    private String remark;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
