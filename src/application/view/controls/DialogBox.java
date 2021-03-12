package application.view.controls;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class DialogBox {

	protected static Stage getStage(String title, AnchorPane pane) {
		Stage dialogStage = new Stage();
		dialogStage.setTitle(title);
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.setMaximized(false);
		Scene scene = new Scene(pane);
		dialogStage.setScene(scene);
		return dialogStage;
	}
	
}
