package useless.chess.gui;

import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;

public class ChoiceDialog<T> extends Dialog<T> {

	public ChoiceDialog(Collection<T> choices) {
		VBox layout = new VBox();
		layout.setFillWidth(true);
		for (T choice : choices) {
			Button button = new Button();
			button.prefWidthProperty().bind(layout.widthProperty());
			button.setText(choice.toString());
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					setResult(choice);
					close();
				}
			});
			layout.getChildren().add(button);
		}
		this.getDialogPane().setContent(layout);
	}

}
