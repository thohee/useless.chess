package de.thohee.useless.chess;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Coordinate;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.player.LexicographicMinimaxPlayer;
import de.thohee.useless.chess.player.Player;

public class Game {

	private static final String _uci = "uci";
	private static final String _uciok = "uciok";
	private static final String _info = "info string ";
	private static final String _isready = "isready";
	private static final String _readyok = "readyok";
	private static final String _position = "position";
	private static final String _startpos = "startpos";
	private static final String _moves = "moves";
	private static final String _go = "go";
	private static final String _depth = "depth";
	private static final String _movetime = "movetime";
	private static final String _infinite = "infinite";
	private static final String _stop = "stop";
	private static final String _bestmove = "bestmove ";
	private static final String _quit = "quit";

	private Player player;
	private BoardPosition boardPosition;
	private Thread playerThread;

	private InputStream inStream;
	private PrintStream outStream;

	private static class OutputToStreamWriter implements Player.OutputWriter {

		private PrintStream printStream;

		OutputToStreamWriter(PrintStream printStream) {
			assert (printStream != null);
			this.printStream = printStream;
		}

		@Override
		public void writeLine(String line) {
			printStream.println(_info + line);
		}

	}

	public static void main(String[] args) {
		Game game = new Game(System.in, System.out);
		game.playUciGame();
	}

	Game(InputStream inputStream, PrintStream outputStream) {
		this.inStream = inputStream;
		this.outStream = outputStream;
	}

	private void writeLine(String line) {
		outStream.println(line);
	}

	void playUciGame() {
		Scanner scanner = new Scanner(this.inStream);
		if (_uci.equals(scanner.nextLine())) {
			writeLine("id name de.thohee.useless.chess");
			writeLine("id author thohee");
			writeLine(_uciok);
			while (processCommand(scanner.nextLine()))
				;
			scanner.close();
		} else {
			writeLine(_info + "I only understand uci.");
		}
		writeLine(_info + "Goodbye");
	}

	private boolean processCommand(String inputLine) {
		try {
			if (_isready.equals(inputLine)) {
				writeLine(_readyok);
				return true;
			}
			if (inputLine != null && inputLine.startsWith(_position)) {
				String startPosAndMoves = inputLine.substring(_position.length()).trim();
				if (startPosAndMoves.startsWith(_startpos)) {
					this.boardPosition = BoardPosition.getInitialPosition();
					String maybeMoves = startPosAndMoves.substring(_startpos.length()).trim();
					if (maybeMoves.startsWith(_moves)) {
						String[] moveTokens = maybeMoves.substring(_moves.length()).trim().split(" ");
						for (String moveToken : moveTokens) {
							performMove(moveToken);
						}
					}
					this.player = new LexicographicMinimaxPlayer(boardPosition.getColourToMove());
					this.player.setOutputWriter(new OutputToStreamWriter(outStream));
					return true;
				} else {
					writeLine(_info + "Cannot read position.");
					return false;
				}
			}
			if (inputLine.startsWith(_go)) {
				if (boardPosition == null || player == null) {
					writeLine("unknown position");
					return false;
				}
				Player.Params params = new Player.Params();
				String[] tokens = inputLine.substring(_go.length()).trim().split(" ");
				int i = 0;
				while (i < tokens.length) {
					if (tokens[i].equals(_infinite)) {
						params.infinite = true;
					} else if (tokens[i].equals(_depth)) {
						params.maxDepthInPlies = Integer.parseInt(tokens[i + 1]);
						++i;
					} else if (tokens[i].equals(_movetime)) {
						params.maxTimeInMillis = Long.parseLong(tokens[i + 1]);
						++i;
					}
					++i;
				}
				findBestMoveConcurrently(params);
				return true;
			}
			if (_stop.equals(inputLine)) {
				if (this.player != null) {
					this.player.stop();
				}
				return true;
			}
			if (_quit.equals(inputLine)) {
				if (this.player != null) {
					this.player.stop();
				}
				if (this.playerThread != null && this.playerThread.isAlive()) {
					this.playerThread.join(100);
				}
				return false;
			}
			return true;
		} catch (Throwable e) {
			writeLine(_info + e.getMessage());
			return false;
		}
	}

	private void performMove(String moveToken) {
		Coordinate from = Coordinate.parse(moveToken.substring(0, 2));
		Coordinate to = Coordinate.parse(moveToken.substring(2, 4));
		Move move = boardPosition.getMove(from, to);
		boardPosition = boardPosition.performMove(move);
	}

	private void findBestMoveConcurrently(Player.Params params) {
		if (playerThread != null && playerThread.isAlive()) {
			player.stop();
			try {
				playerThread.join(100);
			} catch (InterruptedException e) {
			}
		}
		playerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Move move = player.makeMove(boardPosition, params);
				writeLine(_bestmove + move.asUciMove());
			}
		});
		playerThread.start();
	}

}
