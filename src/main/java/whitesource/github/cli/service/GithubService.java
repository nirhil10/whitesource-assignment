package whitesource.github.cli.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import whitesource.github.cli.config.GithubCliConfig;

@RequiredArgsConstructor
public class GithubService {

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);
    private final GithubCliConfig config;
    private final RestTemplate restTemplate;
    private final HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
    private final String githubApi = "https://api.github.com/repos";
    private final static ParameterizedTypeReference<List<Map<String, Object>>> listOfMapTypeParameterizedReference = new ParameterizedTypeReference<List<Map<String,Object>>>() {
    };
    private final static ParameterizedTypeReference<Map<String, Object>> mapTypeParameterizedReference = new ParameterizedTypeReference<Map<String,Object>>() {
    };

    public void getDownloads() {

        String url = String.format("%s/%s/releases", githubApi, config.getRepo());
        logger.debug("Going to call to: '{}'", url);
        List<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, listOfMapTypeParameterizedReference).getBody();
        String table = createDownloadsTable(response);
        logger.debug(table);
        printTable(table);
    }

    @SuppressWarnings("unchecked")
    private String createDownloadsTable(List<Map<String, Object>> data) {

        StringBuilder sb = new StringBuilder();
        sb.append("+----------------------+------------------------------------------+----------------+").append(System.lineSeparator());
        sb.append(String.format("| %-20s | %-40s | %-14s |%n", "RELEASE NAME", "DISTRIBUTION", "DOWNLAOD COUNT"));
        sb.append("+----------------------+------------------------------------------+----------------+").append(System.lineSeparator());

        data.stream()
                .forEach(release -> ((List<Map<String, Object>>) release.get("assets")).stream().forEach(
                        asset -> sb.append(String.format("| %-20s | %-40s | %-14d |%n", (String) release.get("name"),
                                (String) asset.get("name"), (int) asset.get("download_count")))));

        sb.append("+----------------------+------------------------------------------+----------------+");
        return sb.toString();
    }

    public void getStats() {

        String statsUrl = String.format("%s/%s", githubApi, config.getRepo());
        String contributorsUrl = String.format("%s/%s/contributors", githubApi, config.getRepo());
        logger.debug("Going to call to: '{}'", statsUrl);
        Map<String, Object> statsResponse = restTemplate.exchange(statsUrl, HttpMethod.GET, entity, mapTypeParameterizedReference).getBody();
        logger.debug("Going to call to: '{}'", contributorsUrl);
        List<Map<String, Object>> contributorsResponse = restTemplate.exchange(contributorsUrl, HttpMethod.GET, entity, listOfMapTypeParameterizedReference).getBody();
        String table = createStatsTable(statsResponse, contributorsResponse);
        logger.debug(table);
        printTable(table);
    }

    private String createStatsTable(Map<String, Object> stats, List<Map<String, Object>> contributors) {

        StringBuilder sb = new StringBuilder();
        sb.append("+--------------+-------+").append(System.lineSeparator());
        sb.append(String.format("| %-12s | %-5s |%n", "STAT", "VALUE"));
        sb.append("+--------------+-------+").append(System.lineSeparator());

        sb.append(String.format("| %-12s | %-5d |%n", "Stars", (int) stats.get("stargazers_count"))); // i assumed that stars means to stargazers
        sb.append(String.format("| %-12s | %-5d |%n", "Forks", (int) stats.get("forks_count")));
        sb.append(String.format("| %-12s | %-5d |%n", "Contributirs", contributors.size()));
        sb.append(String.format("| %-12s | %-5s |%n", "Language", (String) stats.get("language")));

        sb.append("+--------------+-------+");
        return sb.toString();
    }

    private void printTable(String table) {
        if (StringUtils.hasText(config.getOutputPath())) {
            logger.debug("Going to write to file: '{}", config.getOutputPath());
            Path path = Paths.get(config.getOutputPath());
            try {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                Files.write(path, table.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            logger.debug("Writing to console");
            System.out.println(table);
        }
    }
}
