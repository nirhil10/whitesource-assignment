package whitesource.github.cli;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import whitesource.github.cli.config.GithubCliConfig;
import whitesource.github.cli.service.GithubService;

@SpringBootApplication
@EnableConfigurationProperties
public class GithubCliApp {

    private static final Logger logger = LoggerFactory.getLogger(GithubCliApp.class);

    public static void main(String[] args) {
        SpringApplication.run(GithubCliApp.class, args);
    }

    @Bean
    @ConfigurationProperties("whitesource.github.cli")
    public GithubCliConfig githubCliConfig() {

        return new GithubCliConfig();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, GithubCliConfig config, ConfigurableApplicationContext ctx) {

        ResponseErrorHandler errorHandler = new ResponseErrorHandler() {

            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return Optional.ofNullable(response.getStatusCode())
                        .map(v -> !v.is2xxSuccessful())
                        .orElse(true);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                logger.warn("Failed HTTP request: ({}) {}", response.getStatusCode(), response.getStatusCode()
                        .getReasonPhrase());
                if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    System.out.println(String.format("Repo '%s' does not exist or empty.", config.getRepo()));
                    System.exit(SpringApplication.exit(ctx, () -> 0));
                }
                else {
                    throw new RuntimeException(String.format("Failed HTTP request: (%s) %s", response.getStatusCode(), response.getStatusCode()
                            .getReasonPhrase()));
                }
            }
        };
        return builder.errorHandler(errorHandler).build();
    }

    @Bean
    public GithubService githubService(GithubCliConfig config, RestTemplate restTemplate) {

        return new GithubService(config, restTemplate);
    }

    @Bean
    public CommandLineRunner commandLineRunner(GithubService githubService, GithubCliConfig config, ConfigurableApplicationContext ctx) {

        return args -> {
            if ("downloads".equals(config.getCommand())) {
                logger.info("Going to fetch downloads information");
                githubService.getDownloads();
            }
            else if ("stats".equals(config.getCommand())) {
                logger.info("Going to fetch downloads information");
                githubService.getStats();
            }
            else {
                throw new RuntimeException(String.format("Command '%s' is not valid! please use 'downloads' or 'stats'", config.getCommand()));
            }
            if (!config.isTestContext()) {
                // if not test flow, shutdwon gracefully
                logger.info("Going to shutdown");
                System.exit(SpringApplication.exit(ctx, () -> 0));
            }
        };
    }
}
