package whitesource.github.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import whitesource.github.cli.config.GithubCliConfig;

@SpringBootTest(properties = { "spring.config.location=classpath:github-cli-stats-test.yaml" })
public class GithubCliStatsComponentTests {

    @Autowired 
    GithubCliConfig config;

    @Test
    public void testStats() {

        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            Path path = Paths.get(config.getOutputPath());
            Assertions.assertThat(path.toFile().exists()).isTrue();
            assertThat(Files.readAllLines(path)).isNotEmpty();
        });
    }
}
