package whitesource.github.cli.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubCliConfig {

    private String command;
    private String repo;
    private String outputPath;
    private boolean testContext = false; // indicates whether the flow is in test context or not
}
