package useless.chess;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.GameReport;
import useless.chess.board.Move;
import useless.chess.board.Move.IllegalMoveFormatException;
import useless.chess.board.PGNParser;
import useless.chess.board.PGNWriter;
import useless.chess.board.Result;
import useless.chess.gui.Controller;
import useless.chess.gui.MainWindow;
import useless.chess.gui.Model;
import useless.chess.gui.View;
import useless.chess.player.HumanPlayer;
import useless.chess.player.LexicographicMinimaxPlayer;
import useless.chess.player.Player;

public class Game extends Application implements Model, Controller {

	private static final String gameFilename = "game.pgn";

	private enum StartMode {
		ResumeGame, StartNew;
		@Override
		public String toString() {
			switch (this) {
			case ResumeGame:
				return "resume game";
			case StartNew:
			default:
				return "start new game";
			}
		}

	}

	private MainWindow mainView;
	private Colour ownColour;
	private Player opponent;
	private BoardPosition boardPosition;
	private GameReport gameReport;
	private Thread playerThread;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		mainView = new MainWindow(stage, this, this);
		mainView.show();

		boolean resumeSavegame = false;
		File file = new File(gameFilename);
		if (file.exists()) {
			StartMode startMode = mainView.choose(Arrays.asList(StartMode.ResumeGame, StartMode.StartNew));
			resumeSavegame = startMode.equals(StartMode.ResumeGame);
		}

		boardPosition = BoardPosition.getInitialPosition();

		Player whitePlayer = null;
		Player blackPlayer = null;
		if (resumeSavegame) {
			List<GameReport> gameReports = PGNParser.parse(gameFilename);
			GameReport gameReport = gameReports.get(0);
			Class<?> whitePlayerClass = Player.class.getClassLoader().loadClass(gameReport.getWhite());
			Class<?> blackPlayerClass = Player.class.getClassLoader().loadClass(gameReport.getBlack());
			Constructor<?> whitePlayerConstructor = whitePlayerClass.getConstructor(Colour.class);
			whitePlayer = (Player) whitePlayerConstructor.newInstance(Colour.White);
			Constructor<?> blackPlayerConstructor = blackPlayerClass.getConstructor(Colour.class);
			blackPlayer = (Player) blackPlayerConstructor.newInstance(Colour.Black);
			for (Move move : gameReport.getMoves()) {
				boardPosition = boardPosition.performMove(move);
			}
			ownColour = whitePlayer instanceof HumanPlayer ? Colour.White : Colour.Black;
			opponent = ownColour.equals(Colour.White) ? blackPlayer : whitePlayer;
		} else {
			file.delete();
			ownColour = mainView.choose(Arrays.asList(Colour.White, Colour.Black));
			opponent = new LexicographicMinimaxPlayer(ownColour.opposite());
			whitePlayer = ownColour.equals(Colour.White) ? new HumanPlayer(Colour.White) : opponent;
			blackPlayer = ownColour.equals(Colour.Black) ? new HumanPlayer(Colour.Black) : opponent;
		}

		gameReport = new GameReport();
		gameReport.setEvent("useless.chess");
		gameReport.setWhite(whitePlayer.getClass().getName());
		gameReport.setBlack(blackPlayer.getClass().getName());
		updateReport();

		mainView.update();

		if (Colour.Black.equals(ownColour)) {
			makeMove();
		}
	}

	@Override
	public void stop() {
		if (playerThread != null && playerThread.isAlive()) {
			try {
				playerThread.join();
			} catch (InterruptedException e) {
			}
		}
	}

	private void writeReport() {
		gameReport.setMoves(boardPosition.getPerformedMoves());
		try {
			PGNWriter.write(gameReport, gameFilename);
		} catch (IOException e) {
			mainView.showMsg(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private void updateReport() {
		if (boardPosition.isDraw()) {
			gameReport.setResult(Result.Draw);
		} else if (boardPosition.isCheckmate()) {
			Colour winner = boardPosition.getColourToMove().opposite();
			gameReport.setResult(winner.equals(Colour.White) ? Result.White : Result.Black);
		}
		if (!Result.None.equals(gameReport.getResult())) {
			writeReport();
		}
	}

	private void makeMove() {
		playerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Move move = opponent.makeMove(boardPosition);
				boardPosition = boardPosition.performMove(move);
				updateReport();
				mainView.update();
			}
		});
		playerThread.start();
	}

	@Override
	public void executeCommand(String cmd) {
		if (cmd != null && cmd.toLowerCase().equals("quit")) {
			writeReport();
			mainView.close();
			return;
		}
		try {
			Move move = boardPosition.guessMove(cmd);
			if (move != null) {
				boardPosition = boardPosition.performMove(move);
				updateReport();
				mainView.update();
				makeMove();
			} else {
				mainView.update();
			}
		} catch (IllegalMoveFormatException e) {
			mainView.update();
		}
	}

	@Override
	public void registerListener(View view) {
		assert (view == mainView);
	}

	@Override
	public BoardPosition getBoardPosition() {
		return boardPosition;
	}

	@Override
	public Colour getOwnColour() {
		return ownColour;
	}

	@Override
	public Result getResult() {
		return gameReport.getResult() != null ? gameReport.getResult() : Result.None;
	}

}
