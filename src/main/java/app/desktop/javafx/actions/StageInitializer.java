package app.desktop.javafx.actions;

import app.desktop.javafx.events.StageReadyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent>{
	@Autowired private StageActions stageActions;
	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		Stage stage = (Stage) event.getSource();
		stageActions.loadMainFxml(stage);
		stageActions.setTitleAndIcon(stage);
		stageActions.setCloseRequestHandler(stage);
		stage.show();
	}

}
