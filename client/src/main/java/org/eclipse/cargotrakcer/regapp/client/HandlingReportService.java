package org.eclipse.cargotrakcer.regapp.client;

import java.util.concurrent.CompletableFuture;

public interface HandlingReportService {
    CompletableFuture<HandlingResponse> submitReport(HandlingReport report);
}
