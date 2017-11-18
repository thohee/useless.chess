package useless.chess.gui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseEvent;
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

public class MainWindow implements View {

	Stage stage;
	Controller controller;
	Model model;

	Canvas boardCanvas;
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
		stage.setResizable(false);
		VBox rootLayoutPane = new VBox();
		Scene scene = new Scene(rootLayoutPane, 300, 300);
		boardCanvas = new Canvas(300, 300);
		boardCanvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseClicked(event.getX(), event.getY());
			}
		});
		rootLayoutPane.getChildren().add(boardCanvas);
		stage.setScene(scene);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (!enabled) {
					event.consume();
				}
			}
		});
	}

	public void show() {
		this.model.registerListener(this);
		this.stage.show();
	}

	private void updateBoard() {
		final GraphicsContext graphicsContext = this.boardCanvas.getGraphicsContext2D();
		graphicsContext.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getWidth());
		printBoard();
		putPieces();
		printSelection();
	}

	private void printBoard() {
		final double fieldWidth = this.boardCanvas.getWidth() / 8;
		final GraphicsContext graphicsContext = this.boardCanvas.getGraphicsContext2D();
		graphicsContext.save();
		graphicsContext.setFill(javafx.scene.paint.Color.LIGHTGRAY);
		for (int c = 0; c < 8; ++c) {
			for (int r = 0; r < 8; ++r) {
				double x = c * fieldWidth;
				double y = r * fieldWidth;
				if ((c + r) % 2 == 1) {
					graphicsContext.fillRect(x, y, fieldWidth, fieldWidth);
				}
			}
		}
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
				updateBoard();
			} else if (selectedFields.isEmpty()) {
				Piece piece = model.getBoardPosition().get(coordinate);
				if (piece != null && piece.getColour().equals(model.getOwnColour())) {
					selectedFields.add(coordinate);
					updateBoard();
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
					updateBoard();
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
		updateBoard();
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
				Dialog<Boolean> dlg = new Dialog<Boolean>();
				dlg.setContentText(msg);
				dlg.show();
			}
		});
	}

	public void close() {
		stage.close();
	}
}
