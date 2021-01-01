package app.desktop.javafx;

import app.desktop.javafx.actions.StageInitializer;
import app.desktop.javafx.events.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {
	ConfigurableApplicationContext ctxt;
	
	@Autowired
	StageInitializer listener;
	
	@Override
	public void init() throws Exception {
		this.ctxt = new SpringApplicationBuilder(DemoApplication.class).run();
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		//ctxt.addApplicationListener(listener);
		ctxt.publishEvent(new StageReadyEvent(stage));
	}

	@Override
	public void stop() throws Exception {
		ctxt.close();
		Platform.exit();
	}

}
