package org.eclipse.cargotrakcer.regapp.client;

import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@ExtendWith(WeldJunit5Extension.class)
@EnableWeld
class HandlingReportServiceMockFailedTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(HandlingReportService.class).addBeans(createBarBean()).build();

    static Bean<?> createBarBean() {
        return MockBean.builder()
                .types(HandlingReportService.class)
                .scope(ApplicationScoped.class)
                .creating(
                        // Mock object provided by Mockito
                        when(mock(HandlingReportService.class).submitReport(any(HandlingReport.class)))
                                .thenReturn(CompletableFuture.completedFuture(new HandlingResponse.FAILED("failed")))
                                .getMock()
                )
                .build();
    }

    @Inject
    HandlingReportService handlingReportService;

    @Test
    void testFailed() throws ExecutionException, InterruptedException {
        HandlingResponse handlingResponse = handlingReportService.submitReport(new HandlingReport()).get();
        assertThat(handlingResponse).isInstanceOf(HandlingResponse.FAILED.class);

        var failed = (HandlingResponse.FAILED) handlingResponse;
        assertThat(failed.getMessage()).isEqualTo("failed");
    }
}