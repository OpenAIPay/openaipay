package cn.openaipay.application.facade;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * application 依赖方向守卫测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
class ApplicationDependencyDirectionTest {

    /** 源码根目录。 */
    private static final Path APPLICATION_MAIN_JAVA = Path.of("src/main/java");

    @Test
    void tradeOrchestrationShouldNotDependOnAccountFacade() throws IOException {
        assertNotImport(
                "cn/openaipay/application/credittrade/service/impl/CreditTradeServiceImpl.java",
                "cn.openaipay.application.creditaccount.facade.CreditAccountFacade"
        );
        assertNotImport(
                "cn/openaipay/application/fundtrade/service/impl/FundTradeServiceImpl.java",
                "cn.openaipay.application.fundaccount.facade.FundAccountFacade"
        );
        assertNotImport(
                "cn/openaipay/application/loantrade/service/impl/LoanTradeServiceImpl.java",
                "cn.openaipay.application.loanaccount.facade.LoanAccountFacade"
        );
        assertNotImport(
                "cn/openaipay/application/payroute/service/impl/PayRouteServiceImpl.java",
                "cn.openaipay.application.creditaccount.facade.CreditAccountFacade"
        );
        assertNotImport(
                "cn/openaipay/application/payroute/service/impl/PayRouteServiceImpl.java",
                "cn.openaipay.application.loanaccount.facade.LoanAccountFacade"
        );
    }

    @Test
    void tradeOrchestrationShouldDependOnAccountService() throws IOException {
        assertHasImport(
                "cn/openaipay/application/credittrade/service/impl/CreditTradeServiceImpl.java",
                "cn.openaipay.application.creditaccount.service.CreditAccountService"
        );
        assertHasImport(
                "cn/openaipay/application/fundtrade/service/impl/FundTradeServiceImpl.java",
                "cn.openaipay.application.fundaccount.service.FundAccountService"
        );
        assertHasImport(
                "cn/openaipay/application/loantrade/service/impl/LoanTradeServiceImpl.java",
                "cn.openaipay.application.creditaccount.service.CreditAccountService"
        );
        assertHasImport(
                "cn/openaipay/application/payroute/service/impl/PayRouteServiceImpl.java",
                "cn.openaipay.application.payroute.port.CreditRouteAccountPort"
        );
        assertHasImport(
                "cn/openaipay/application/payroute/service/impl/PayRouteServiceImpl.java",
                "cn.openaipay.application.payroute.port.LoanRouteAccountPort"
        );
    }

    @Test
    void accountFacadeShouldNotDependOnTradeService() throws IOException {
        assertNotImport(
                "cn/openaipay/application/creditaccount/facade/impl/CreditAccountFacadeImpl.java",
                "cn.openaipay.application.credittrade.service.CreditTradeService"
        );
        assertNotImport(
                "cn/openaipay/application/loanaccount/facade/impl/LoanAccountFacadeImpl.java",
                "cn.openaipay.application.loantrade.service.LoanTradeService"
        );
        assertNotImport(
                "cn/openaipay/application/fundaccount/facade/impl/FundAccountFacadeImpl.java",
                "cn.openaipay.application.fundtrade.service.FundTradeService"
        );
    }

    private void assertNotImport(String relativePath, String forbiddenImport) throws IOException {
        String source = readSource(relativePath);
        assertThat(source)
                .as("%s should not import %s", relativePath, forbiddenImport)
                .doesNotContain("import " + forbiddenImport + ";");
    }

    private void assertHasImport(String relativePath, String expectedImport) throws IOException {
        String source = readSource(relativePath);
        assertThat(source)
                .as("%s should import %s", relativePath, expectedImport)
                .contains("import " + expectedImport + ";");
    }

    private String readSource(String relativePath) throws IOException {
        Path absolutePath = APPLICATION_MAIN_JAVA.resolve(relativePath);
        return Files.readString(absolutePath);
    }
}
