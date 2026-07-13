package org.eclipse.cargotrakcer.regapp.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import net.rgielen.fxweaver.core.FxmlView;
import org.eclipse.cargotrakcer.regapp.client.HandlingReport;
import org.eclipse.cargotrakcer.regapp.client.HandlingReportService;
import org.eclipse.cargotrakcer.regapp.client.HandlingResponse;
import org.eclipse.cargotrakcer.regapp.util.DateUtil;
import tornadofx.control.DateTimePicker;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent// this is a must, set to ApplicationScoped does not work.
@FxmlView("HandlingReport.fxml")
public class HandlingReportController {
    private final static Logger LOGGER = Logger.getLogger(HandlingReportController.class.getName());

    @Inject
    private HandlingReportService handlingReportService;

    @Inject
    private Validator validator;

    @FXML
    private DateTimePicker completionTimeField;

    @FXML
    private TextField trackingIdField;

    @FXML
    private ComboBox<String> eventTypeField;

    @FXML
    private TextField unLocodeField;

    @FXML
    private TextField voyageNumberField;

    @FXML
    private Text message;

    @FXML
    private Hyperlink githubLink;

    @FXML
    private Hyperlink twitterLink;

    public HandlingReportController() {
        LOGGER.log(Level.INFO, "calling constructor method.");
    }

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "calling @PostConstruct method.");
    }

    @FXML
    private void initialize() {
        LOGGER.log(Level.INFO, "calling @FXML initialize method.");
        eventTypeField.getItems().addAll(
                "LOAD",
                "UNLOAD",
                "RECEIVE",
                "CLAIM",
                "CUSTOMS"
        );
        Consumer<String> openUrl = url -> {
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        githubLink.setOnAction(e -> openUrl.accept("https://github.com/hantsy/cargotracker-regapp"));
        twitterLink.setOnAction(e -> openUrl.accept("https://twitter.com/@hantsy"));
    }

    @FXML
    private void onSubmit() {
        LOGGER.log(Level.INFO, "injected HandlingReportService: {0}", this.handlingReportService);
        var completionTime = completionTimeField.getDateTimeValue();
        var trackingId = trackingIdField.getText();
        var eventType = eventTypeField.getValue();
        var unLocode = unLocodeField.getText();
        var voyageNumber = voyageNumberField.getText();

        //Jsonb has no option to remove empty values.
        if (null != voyageNumber && "".equals(voyageNumber.trim())) {
            voyageNumber = null;
        }

        var report = HandlingReport.builder()
                .completionTime(DateUtil.toString(completionTime))
                .eventType(eventType)
                .trackingId(trackingId)
                .unLocode(unLocode)
                .voyageNumber(voyageNumber)
                .build();
        LOGGER.log(Level.INFO, "submitting report: {0}", report);

        // Validate report data.
        Set<ConstraintViolation<HandlingReport>> violations = validator.validate(report);
        if (!violations.isEmpty()) {
            LOGGER.log(Level.WARNING, "validation failed: {0}", violations);
            showValidationErrors(violations);
            return;
        }

        clearValidationErrors();
        this.handlingReportService.submitReport(report)
                .thenAccept(handlingResponse -> {

                    if (handlingResponse instanceof HandlingResponse.OK) {
                        message.setText("Submitted successfully!");
                        message.setFill(Color.GREEN);
                    } else {
                        var res = (HandlingResponse.FAILED) handlingResponse;
                        message.setText(res.getMessage());
                        message.setFill(Color.RED);
                    }

                })
                .exceptionally(e -> {
                    LOGGER.log(Level.WARNING, "exception caught: {0}", e.getMessage());
                    if (e instanceof ConnectException || e.getCause() instanceof ConnectException) {
                        LOGGER.info("Connection failed.");
                        message.setText("Connection error, please check if 'HANDLING_REPORT_SERVICE_URL' is set \n" +
                                "when submitting to a remote host.");
                        message.setFill(Color.RED);
                    }
                    return null;
                })
                .join();
    }

    private void showValidationErrors(Set<ConstraintViolation<HandlingReport>> violations) {
        clearValidationErrors();
        for (ConstraintViolation<HandlingReport> violation : violations) {
            Control field = resolveField(violation.getPropertyPath().toString());
            if (field != null) {
                field.getStyleClass().add("error");
                field.setTooltip(new Tooltip(violation.getMessage()));
            }
        }
        message.setText("Please fix validation errors");
        message.setFill(Color.RED);
    }

    private void clearValidationErrors() {
        completionTimeField.getStyleClass().remove("error");
        completionTimeField.setTooltip(null);
        trackingIdField.getStyleClass().remove("error");
        trackingIdField.setTooltip(null);
        eventTypeField.getStyleClass().remove("error");
        eventTypeField.setTooltip(null);
        unLocodeField.getStyleClass().remove("error");
        unLocodeField.setTooltip(null);
        voyageNumberField.getStyleClass().remove("error");
        voyageNumberField.setTooltip(null);
    }

    private Control resolveField(String propertyName) {
        return switch (propertyName) {
            case "completionTime" -> completionTimeField;
            case "trackingId" -> trackingIdField;
            case "eventType" -> eventTypeField;
            case "unLocode" -> unLocodeField;
            case "voyageNumber" -> voyageNumberField;
            default -> null;
        };
    }
}
