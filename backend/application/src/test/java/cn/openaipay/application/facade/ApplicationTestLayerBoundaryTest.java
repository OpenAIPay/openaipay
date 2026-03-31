package cn.openaipay.application.facade;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * application 测试层级边界测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
class ApplicationTestLayerBoundaryTest {

    /** application 测试源码根目录。 */
    private static final Path APPLICATION_TEST_JAVA = Path.of("src/test/java");

    @Test
    void applicationTestsShouldStayAtFacadeBoundary() throws IOException {
        List<Path> javaFiles;
        try (Stream<Path> pathStream = Files.walk(APPLICATION_TEST_JAVA)) {
            javaFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("Test.java"))
                    .toList();
        }

        assertThat(javaFiles).isNotEmpty();

        List<String> forbidden = javaFiles.stream()
                .map(path -> path.toString().replace('\\', '/'))
                .filter(path -> !path.contains("/facade/"))
                .toList();

        assertThat(forbidden)
                .as("application tests should converge to facade boundary")
                .isEmpty();
    }
}
