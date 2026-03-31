package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.Position;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * 人工干预处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Component
public class InterveneProcessor extends AbstractDeliverProcessor {

    /** TOP信息 */
    private static final Pattern TOP_CREATIVE_PATTERN = Pattern.compile("\\\"(?:topCreativeCode|top)\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    /**
     * 处理业务数据。
     */
    @Override
    public int sort() {
        return 6;
    }

    /**
     * 处理DO信息。
     */
    @Override
    protected void doProcess(Position position, cn.openaipay.domain.deliver.model.DeliverContext context) {
        if (position.creativeCount() <= 1 || position.getSortRule() == null || position.getSortRule().isBlank()) {
            return;
        }
        String topCreativeCode = extractTopCreativeCode(position.getSortRule());
        if (topCreativeCode == null || topCreativeCode.isBlank()) {
            return;
        }
        List<DeliverCreative> creatives = position.getDeliverCreativeList();
        List<DeliverCreative> reordered = Stream.concat(
                creatives.stream().filter(creative -> topCreativeCode.equals(creative.getCreativeCode())),
                creatives.stream().filter(creative -> !topCreativeCode.equals(creative.getCreativeCode()))
        ).toList();
        position.setDeliverCreativeList(reordered);
    }

    private String extractTopCreativeCode(String sortRule) {
        Matcher matcher = TOP_CREATIVE_PATTERN.matcher(sortRule);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (sortRule.startsWith("TOP:")) {
            return sortRule.substring(4).trim();
        }
        return null;
    }
}
