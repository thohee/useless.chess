package useless.chess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.List;

import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.GameReport;
import useless.chess.board.GameReport.Result;
import useless.chess.board.Move;
import useless.chess.board.PGNParser;
import useless.chess.board.PGNWriter;
import useless.chess.player.HumanPlayer;
import useless.chess.player.LexicographicMinimaxPlayer;
import useless.chess.player.Player;

public class Game {

	private static final String gameFilename = "game.pgn";

	public static void main(String[] args) {

		boolean resumeSavegame = false;
		File file = new File(gameFilename);
		if (file.exists()) {
			System.out.println("Resume game (y/n)? ");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			try {
				int input = bufferedReader.read();
				if (input == 'y') {
					resumeSavegame = true;
				} else {
					file.delete();
				}
			} catch (IOException e) {
				System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		Player whitePlayer = null;
		Player blackPlayer = null;
		BoardPosition boardPosition = BoardPosition.getInitialPosition();

		if (resumeSavegame) {
			try {
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
				System.out.println(boardPosition.showPerformedMoves());
			} catch (Throwable e) {
				System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
				e.printStackTrace();
				return;
			}
		} else {
			Colour humanPlayerColour = null;
			while (humanPlayerColour == null) {
				System.out.print("Choose side (white/black): ");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				try {
					String input = bufferedReader.readLine();
					switch (input.toUpperCase()) {
					case "WHITE":
						humanPlayerColour = Colour.White;
						break;
					case "BLACK":
						humanPlayerColour = Colour.Black;
						break;
					}
				} catch (IOException e) {
				}
			}
			whitePlayer = humanPlayerColour.equals(Colour.White) ? new HumanPlayer(Colour.White)
					: new LexicographicMinimaxPlayer(Colour.White);
			blackPlayer = humanPlayerColour.equals(Colour.Black) ? new HumanPlayer(Colour.Black)
					: new LexicographicMinimaxPlayer(Colour.Black);
		}

		GameReport gameReport = new GameReport();
		gameReport.setEvent("useless.chess");
		gameReport.setWhite(whitePlayer.getClass().getName());
		gameReport.setBlack(blackPlayer.getClass().getName());
		System.out.println(boardPosition.toString());
		try {
			while (!boardPosition.getPossibleMoves().isEmpty()) {
				Player player = boardPosition.getColourToMove().equals(Colour.White) ? whitePlayer : blackPlayer;
				Move move = player.makeMove(boardPosition);
				boardPosition = boardPosition.performMove(move);
				String moveString = move.toString();
				if (boardPosition.isCheck()) {
					moveString += "+";
				}
				System.out.println(Long.toString(boardPosition.getDepth()) + ": " + moveString);
				System.out.println(boardPosition.toString());
			}

			if (boardPosition.isDraw()) {
				gameReport.setResult(Result.Draw);
				System.out.println("Draw");
			} else if (boardPosition.isCheckmate()) {
				Colour winner = boardPosition.getColourToMove().opposite();
				gameReport.setResult(winner.equals(Colour.White) ? Result.White : Result.Black);
				System.out.println(winner.name() + " wins");
			}
		} catch (RuntimeException e) {
			System.out.println(e.getMessage());
		}
		gameReport.setMoves(boardPosition.getPerformedMoves());
		try {
			PGNWriter.write(gameReport, gameFilename);
		} catch (IOException e) {
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

}
