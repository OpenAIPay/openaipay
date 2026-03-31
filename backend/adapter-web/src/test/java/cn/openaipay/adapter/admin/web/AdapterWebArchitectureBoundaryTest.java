package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * adapter-web 架构边界测试
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
class AdapterWebArchitectureBoundaryTest {

    /** 源码根目录 */
    private static final Path ADAPTER_WEB_MAIN_JAVA = Path.of("src/main/java");
    /** 测试源码根目录 */
    private static final Path ADAPTER_WEB_TEST_JAVA = Path.of("src/test/java");

    @Test
    void adapterWebShouldNotDependOnInfrastructureImplementation() throws IOException {
        List<Path> javaFiles;
        try (Stream<Path> pathStream = Files.walk(ADAPTER_WEB_MAIN_JAVA)) {
            javaFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .toList();
        }

        assertThat(javaFiles).isNotEmpty();

        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile, StandardCharsets.UTF_8);
            assertThat(content)
                    .as("adapter-web should not depend on infrastructure implementation: %s", javaFile)
                    .doesNotContain("import cn.openaipay.infrastructure.")
                    .doesNotContain("cn.openaipay.infrastructure.");
        }
    }

    @Test
    void adapterWebTestsShouldNotKeepServiceUnitTests() throws IOException {
        List<String> forbidden;
        try (Stream<Path> pathStream = Files.walk(ADAPTER_WEB_TEST_JAVA)) {
            forbidden = pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> path.toString().replace('\\', '/'))
                    .filter(path -> path.contains("/service/"))
                    .toList();
        }

        assertThat(forbidden)
                .as("adapter-web tests should converge to controller-focused coverage")
                .isEmpty();
    }
}
