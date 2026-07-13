package org.eclipse.cargotrakcer.regapp.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.cargotrakcer.regapp.client.HandlingReport;
import org.eclipse.cargotrakcer.regapp.client.HandlingReportService;
import org.eclipse.cargotrakcer.regapp.client.HandlingResponse;
import org.eclipse.cargotrakcer.regapp.util.DateUtil;
import tornadofx.control.DateTimePicker;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent// this is a must, set to ApplicationScoped does not work.
@FxmlView("HandlingReport.fxml")
public class HandlingReportController {
    private final static Logger LOGGER = Logger.getLogger(HandlingReportController.class.getName());

    @Inject
    private HandlingReportService handlingReportService;

    private ValidationSupport validationSupport;
    private Map<javafx.scene.control.Control, Label> errorLabelMap;

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
    private Button submitButton;

    @FXML
    private Label completionTimeError;

    @FXML
    private Label trackingIdError;

    @FXML
    private Label unLocodeError;

    @FXML
    private Label eventTypeError;

    @FXML
    private Label voyageNumberError;

    @FXML
    private Text message;

    @FXML
    private ImageView githubIcon;

    @FXML
    private ImageView linkedinIcon;

    @FXML
    private ImageView twitterIcon;

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
        githubIcon.setOnMouseClicked(e -> openUrl.accept("https://github.com/hantsy/cargotracker-regapp"));
        linkedinIcon.setOnMouseClicked(e -> openUrl.accept("https://linkedin.com/in/hantsy"));
        twitterIcon.setOnMouseClicked(e -> openUrl.accept("https://twitter.com/@hantsy"));

        // Initialize completion time with current time and format
        completionTimeField.setFormat(DateUtil.DATE_TIME_FORMAT);
        completionTimeField.setDateTimeValue(LocalDateTime.now());

        setupValidation();
    }

    private void setupValidation() {
        validationSupport = new ValidationSupport();
        validationSupport.setValidationDecorator(new StyleClassValidationDecoration());

        // 1. Completion time is required (DateTimePicker - custom validator)
        validationSupport.registerValidator(completionTimeField, true,
                (control, value) ->
                        ValidationResult.fromErrorIf(control,
                                "Completion time is required",
                                ((DateTimePicker) control).getDateTimeValue() == null
                        ));

        // 2. Tracking ID: required, min 4 chars
        validationSupport.registerValidator(trackingIdField, true,
                Validator.createPredicateValidator(
                        o -> {
                            var s = (String) o;
                            return s != null && !s.isBlank() && s.length() >= 4;
                        },
                        "Tracking ID must be at least 4 characters",
                        Severity.ERROR
                ));

        // 3. Event type: required selection
        validationSupport.registerValidator(eventTypeField, true,
                Validator.createEmptyValidator("Event type is required"));

        // 4. UN Locode: required, exactly 5 chars
        validationSupport.registerValidator(unLocodeField, true,
                Validator.createPredicateValidator(
                        o -> {
                            var s = (String) o;
                            return s != null && !s.isBlank() && s.length() == 5;
                        },
                        "Location must be 5 characters",
                        Severity.ERROR
                ));

        // 5. Voyage number: optional, but if filled min 4 chars
        validationSupport.registerValidator(voyageNumberField, false,
                Validator.createPredicateValidator(
                        o -> {
                            var s = (String) o;
                            return s == null || s.isBlank() || s.trim().length() >= 4;
                        },
                        "Voyage number must be at least 4 characters",
                        Severity.ERROR
                ));

        // Map controls to their error labels
        errorLabelMap = new HashMap<>();
        errorLabelMap.put(completionTimeField, completionTimeError);
        errorLabelMap.put(trackingIdField, trackingIdError);
        errorLabelMap.put(unLocodeField, unLocodeError);
        errorLabelMap.put(eventTypeField, eventTypeError);
        errorLabelMap.put(voyageNumberField, voyageNumberError);

        // Listen for validation changes to update error labels
        validationSupport.validationResultProperty().addListener((obs, oldVal, newVal) -> updateErrorLabels());

        // Bind submit button to validation state — disabled when form is invalid
        submitButton.disableProperty().bind(validationSupport.invalidProperty());
    }

    private void updateErrorLabels() {
        var errors = validationSupport.getValidationResult().getErrors();
        for (var entry : errorLabelMap.entrySet()) {
            var controlMessages = errors.stream()
                    .filter(m -> m.getTarget() == entry.getKey())
                    .toList();
            Label label = entry.getValue();
            if (controlMessages.isEmpty()) {
                label.setVisible(false);
                label.setManaged(false);
            } else {
                label.setText(controlMessages.get(0).getText());
                label.setVisible(true);
                label.setManaged(true);
            }
        }
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
}
