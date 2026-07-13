package org.eclipse.cargotrakcer.regapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eclipse.cargotrakcer.regapp.cdi.StageReadyEvent;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    private Weld weld;
    private WeldContainer container;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        weld.shutdown();
    }

    @Override
    public void init() throws Exception {
        weld = new Weld();
        container = weld.initialize();
    }

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.log(Level.INFO, "fires StageReadyEvent.");
        // start primary stage.
        container.getBeanManager().getEvent().select(StageReadyEvent.Literal).fire(stage);
    }
}
