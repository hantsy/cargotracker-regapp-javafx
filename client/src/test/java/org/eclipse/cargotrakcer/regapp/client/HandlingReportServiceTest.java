package org.eclipse.cargotrakcer.regapp.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//@ExtendWith(WeldJunit5AutoExtension.class)
@EnableAutoWeld
@AddPackages({HandlingReportService.class})
class HandlingReportServiceTest {

    @Inject
    HandlingReportService handlingReportService;

    //No-args constructor will start on port 8080, no HTTPS
    static WireMockServer wireMockServer = new WireMockServer(options().port(8080));

    @BeforeAll
    public static void setUp() {
        wireMockServer.start();
    }

    @AfterAll
    public static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testHandlingReportServiceExists() {
        assertThat(handlingReportService).isNotNull();
    }


    @Test
    void testOK() throws Exception {
        String url = "/cargo-tracker/rest/handling/reports";
        stubFor(post(urlEqualTo(url))
                .willReturn(aResponse().withStatus(202)));

        assertThat(handlingReportService.submitReport(new HandlingReport()).get().toString()).isEqualTo("OK");

        verify(
                postRequestedFor(urlEqualTo(url))
        );
    }

    @Test
    void testFailed() throws Exception {
        String url = "/cargo-tracker/rest/handling/reports";
        stubFor(post(urlEqualTo(url))
                .willReturn(aResponse().withStatus(400).withBody("failed")));
        HandlingResponse handlingResponse = handlingReportService.submitReport(new HandlingReport()).get();
        assertThat(handlingResponse).isInstanceOf(HandlingResponse.FAILED.class);

        var failed = (HandlingResponse.FAILED) handlingResponse;
        assertThat(failed.getMessage()).isEqualTo("failed");

        verify(
                postRequestedFor(urlEqualTo(url))
        );
    }
}