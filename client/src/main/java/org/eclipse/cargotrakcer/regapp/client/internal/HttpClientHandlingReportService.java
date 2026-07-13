package org.eclipse.cargotrakcer.regapp.client.internal;

import org.eclipse.cargotrakcer.regapp.client.HandlingReport;
import org.eclipse.cargotrakcer.regapp.client.HandlingReportService;
import org.eclipse.cargotrakcer.regapp.client.HandlingResponse;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class HttpClientHandlingReportService implements HandlingReportService {

    private final static Logger LOGGER = Logger.getLogger(HttpClientHandlingReportService.class.getName());

    private final static String DEFAULT_SERVICE_URL = "http://localhost:8080/cargo-tracker/rest/handling/reports";

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final HttpClient client = HttpClient.newBuilder()
            .executor(executorService)
            .version(Version.HTTP_1_1)
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            // .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
            // .authenticator(Authenticator.getDefault())
            .build();

    private String handlingServiceUrl = DEFAULT_SERVICE_URL;

    @PostConstruct
    public void init() {
        String sysHandlingServiceUrl = System.getenv("HANDLING_REPORT_SERVICE_URL");

        LOGGER.log(Level.INFO, "env 'HANDLING_REPORT_SERVICE_URL': {0}", sysHandlingServiceUrl);

        if (null != sysHandlingServiceUrl) {
            handlingServiceUrl = sysHandlingServiceUrl;
        }
    }

    public CompletableFuture<HandlingResponse> submitReport(HandlingReport report) {
        // Create Jsonb and serialize
        Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withNullValues(false));

        return this.client
                .sendAsync(HttpRequest.newBuilder()
                                .POST(HttpRequest.BodyPublishers.ofString(jsonb.toJson(report)))
                                .uri(URI.create(handlingServiceUrl))
                                .header("Content-Type", "application/json")
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenApply(res -> res.statusCode() == 202 ? new HandlingResponse.OK() : new HandlingResponse.FAILED(res.body()))
                .toCompletableFuture();
    }

}
