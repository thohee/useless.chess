package useless.chess.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class MsgDialog extends Dialog<Boolean> {

	public MsgDialog(String msg) {
		VBox layout = new VBox();
		layout.setFillWidth(true);
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setText(msg);
		layout.getChildren().add(textArea);
		Button okButton = new Button();
		okButton.setText("OK");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setResult(true);
				close();
			}
		});
		layout.getChildren().add(okButton);
		this.getDialogPane().setContent(layout);
	}
}
