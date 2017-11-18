package useless.chess.gui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.Coordinate;
import useless.chess.board.Move;
import useless.chess.board.Move.IllegalMoveFormatException;
import useless.chess.board.Piece;
import useless.chess.board.Result;

public class MainWindow implements View, ChangeListener<Number> {

	Stage stage;
	Controller controller;
	Model model;

	Pane rootPane;
	Canvas boardCanvas;
	TextField statusBar;
	List<Coordinate> selectedFields = new LinkedList<>();
	boolean enabled = false;

	public MainWindow(Stage stage, Model model, Controller controller) {
		this.stage = stage;
		this.controller = controller;
		this.model = model;
		init();
	}

	private void init() {
		stage.setTitle("useless.chess");
		stage.setResizable(true);
		boardCanvas = new Canvas(300, 300);
		boardCanvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseClicked(event.getX(), event.getY());
			}
		});
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		rootPane = vbox;
		rootPane.getChildren().add(boardCanvas);
		statusBar = new TextField();
		statusBar.setEditable(false);
		//statusBar.setDisable(true);
		statusBar.setPrefHeight(25);
		rootPane.getChildren().add(statusBar);
		double width = boardCanvas.getWidth();
		double height = boardCanvas.getHeight() + statusBar.getPrefHeight();
		Scene scene = new Scene(rootPane, width, height);
		stage.setMinWidth(width);
		stage.setMinHeight(height);
		stage.setScene(scene);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if ((model.getResult() == null || model.getResult().equals(Result.None))
						&& !model.getBoardPosition().getColourToMove().equals(model.getOwnColour())) {
					printMsg("wait for oppenent to move...");
					event.consume();
				}
			}
		});
		rootPane.heightProperty().addListener(this);
		rootPane.widthProperty().addListener(this);
	}

	public void show() {
		this.model.registerListener(this);
		this.stage.show();
	}

	private void updateBoardAndMsg() {
		updateBoard();
		printMsg();
	}

	private void updateBoard() {
		final GraphicsContext graphicsContext = this.boardCanvas.getGraphicsContext2D();
		graphicsContext.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getWidth());
		if (model == null || model.getBoardPosition() == null) {
			return;
		}
		printBoard();
		putPieces();
		printSelection();
	}

	private void printBoard() {
		final double canvasWidth = this.boardCanvas.getWidth();
		final double fieldWidth = canvasWidth / 8;
		final GraphicsContext graphicsContext = this.boardCanvas.getGraphicsContext2D();
		graphicsContext.save();
		graphicsContext.setFill(javafx.scene.paint.Color.LIGHTGRAY);
		graphicsContext.setStroke(javafx.scene.paint.Color.BLACK);
		for (int c = 0; c < 8; ++c) {
			for (int r = 0; r < 8; ++r) {
				double x = c * fieldWidth;
				double y = r * fieldWidth;
				if ((c + r) % 2 == 1) {
					graphicsContext.fillRect(x, y, fieldWidth, fieldWidth);
				}
				graphicsContext.strokeRect(x, y, fieldWidth, fieldWidth);
			}
		}
		graphicsContext.strokeRect(0, 0, canvasWidth, canvasWidth);
		graphicsContext.restore();
	}

	private void putPieces() {
		final double fieldWidth = this.boardCanvas.getWidth() / 8;
		boolean upsideDown = Colour.Black.equals(this.model.getOwnColour());
		GraphicsContext graphicsContext = this.boardCanvas.getGraphicsContext2D();
		graphicsContext.save();
		graphicsContext.setFill(javafx.scene.paint.Color.BLACK);
		graphicsContext.setFont(Font.font(0.8 * fieldWidth));
		for (Map.Entry<Coordinate, Piece> entry : this.model.getBoardPosition().getPositionedPieces()) {
			int c = entry.getKey().getColumn();
			int r = entry.getKey().getRow();
			if (!upsideDown) {
				r = 7 - r;
			} else {
				c = 7 - c;
			}
			double x = c * fieldWidth + 0.1 * fieldWidth;
			double y = r * fieldWidth + 0.8 * fieldWidth;
			graphicsContext.fillText(entry.getValue().toUnicode(), x, y);
		}
		graphicsContext.restore();
	}

	private void printSelection() {
		final double canvasWidth = this.boardCanvas.getWidth();
		final double fieldWidth = canvasWidth / 8;
		final boolean upsideDown = Colour.Black.equals(this.model.getOwnColour());
		GraphicsContext graphicsContext = this.boardCanvas.getGraphicsContext2D();
		graphicsContext.save();
		graphicsContext.setFill(javafx.scene.paint.Color.TRANSPARENT);
		graphicsContext.setStroke(javafx.scene.paint.Color.RED);
		graphicsContext.setLineWidth(Math.max(2, 0.02 * fieldWidth));
		for (Coordinate selectedField : selectedFields) {
			int c = selectedField.getColumn();
			int r = selectedField.getRow();
			if (!upsideDown) {
				r = 7 - r;
			} else {
				c = 7 - c;
			}
			graphicsContext.strokeRect(c * fieldWidth, r * fieldWidth, fieldWidth, fieldWidth);
		}
		graphicsContext.restore();
	}

	private void printMsg() {
		String msg = "";
		if (!enabled) {
			msg = "wait...";
			if (model.getResult() != null && !model.getResult().equals(Result.None)) {
				msg = model.getResult().toString();
			}
		} else if (model.getBoardPosition() != null && model.getBoardPosition().getLastMove() != null) {
			msg = model.getBoardPosition().getLastMove().toString();
		}
		printMsg(msg);
	}

	private void printMsg(String msg) {
		statusBar.clear();
		if (msg != null) {
			statusBar.setText(msg);
		}
	}

	private void mouseClicked(double x, double y) {
		if (!enabled) {
			return;
		}
		final double canvasWidth = this.boardCanvas.getWidth();
		final double fieldWidth = canvasWidth / 8;
		final boolean upsideDown = Colour.Black.equals(this.model.getOwnColour());
		if (x > 0 && x < canvasWidth && y > 0 && y < canvasWidth) {
			int c = (int) Math.floor(x / fieldWidth);
			int r = (int) Math.floor(y / fieldWidth);
			if (!upsideDown) {
				r = 7 - r;
			} else {
				c = 7 - c;
			}
			Coordinate coordinate = new Coordinate(c, r);
			if (selectedFields.contains(coordinate)) {
				selectedFields.remove(coordinate);
				updateBoardAndMsg();
			} else if (selectedFields.isEmpty()) {
				Piece piece = model.getBoardPosition().get(coordinate);
				if (piece != null && piece.getColour().equals(model.getOwnColour())) {
					selectedFields.add(coordinate);
					updateBoardAndMsg();
				}
			} else if (selectedFields.size() < 2) {
				// make move
				String intendedMove = selectedFields.get(0).toString() + "-" + coordinate.toString();
				Move move = null;
				try {
					move = model.getBoardPosition().guessMove(intendedMove);
				} catch (IllegalMoveFormatException e) {
				}
				if (move != null) {
					enabled = false;
					selectedFields.add(coordinate);
					updateBoardAndMsg();
					controller.executeCommand(move.toString());
				}
			}
		}
	}

	@Override
	public void update() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				_update();
			}
		});
	}

	private void _update() {
		final BoardPosition boardPosition = model.getBoardPosition();
		final Result result = model.getResult();
		selectedFields.clear();
		enabled = (result == null || Result.None.equals(result)) && !boardPosition.getPossibleMoves().isEmpty()
				&& boardPosition.getColourToMove().equals(model.getOwnColour());
		updateBoardAndMsg();
	}

	public <T> T choose(Collection<T> choices) {
		if (choices.isEmpty()) {
			return null;
		}
		ChoiceDialog<T> dlg = new ChoiceDialog<>(choices);
		Optional<T> maybeResult = dlg.showAndWait();
		return maybeResult.get();
	}

	public void showMsg(String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				MsgDialog dlg = new MsgDialog(msg);
				dlg.showAndWait();
				stage.close();
			}
		});
	}

	public void close() {
		stage.close();
	}

	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		double size = Math.min(rootPane.getWidth(), rootPane.getHeight() - statusBar.getPrefHeight());
		boardCanvas.setWidth(size);
		boardCanvas.setHeight(size);
		updateBoard();
	}
}
