package cn.openaipay.infrastructure.agreement.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * 协议签约明细持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("agreement_sign_item")
public class AgreementSignItemDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 签约单号。 */
    @TableField("sign_no")
    private String signNo;

    /** 模板编码。 */
    @TableField("template_code")
    private String templateCode;

    /** 模板版本。 */
    @TableField("template_version")
    private String templateVersion;

    /** 协议标题。 */
    @TableField("title")
    private String title;

    /** 协议地址。 */
    @TableField("content_url")
    private String contentUrl;

    /** 协议摘要。 */
    @TableField("content_hash")
    private String contentHash;

    /** 是否同意。 */
    @TableField("accepted")
    private Boolean accepted;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
