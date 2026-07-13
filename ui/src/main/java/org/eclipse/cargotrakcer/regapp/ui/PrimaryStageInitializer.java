package org.eclipse.cargotrakcer.regapp.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.eclipse.cargotrakcer.regapp.cdi.StageReadyEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class PrimaryStageInitializer {
    private final static Logger LOGGER = Logger.getLogger(PrimaryStageInitializer.class.getName());

    @Inject
    FxWeaver fxWeaver;

    public void onInitialized(@Observes @StageReadyEvent Stage stage) throws IOException {
        LOGGER.log(Level.INFO, "observed StageReadyEvent: {0}", stage);
        stage.getIcons().add(new Image("/logo32.png"));
        stage.setTitle("Incident Logging Application");
        Parent root = fxWeaver.loadView(HandlingReportController.class);
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(this.getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
