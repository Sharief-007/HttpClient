package app.desktop.javafx.actions;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TabActions {

    private final ApplicationContext context;
    private final Resource mainResource;
    private final Logger log;

    public TabActions(ApplicationContext context,
                                 @Value("${app.fxml.second-file}") Resource appResource) {
        this.context = context;
        this.mainResource = appResource;
        log = LoggerFactory.getLogger(getClass());
    }

    public Tab createNewTab() throws IOException {
        var loader = new FXMLLoader(mainResource.getURL());
        loader.setControllerFactory(context::getBean);
        Tab tab = loader.load();
        tab.setText("  HttpClient  ");
        tab.setClosable(true);

        //log.info(String.format("Created a new tab with content of %s file ",mainResource.getFilename()));
        return tab;
    }
}
