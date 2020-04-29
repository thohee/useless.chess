package de.thohee.useless.chess;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.player.LexicographicMinimaxPlayer;
import de.thohee.useless.chess.player.Player;

public class Game implements Player.OutputWriter {

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

	private static final String _send = "send: ";
	private static final String _recv = "recv: ";
	private static final String _debg = "debg: ";

	private Player player;
	private BoardPosition boardPosition;
	private Thread playerThread;

	private InputStream inStream;
	private PrintStream outStream;
	private String logFilename;

	static String getLogFilename() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		String logFilename = tmpdir + "useless.chess.log";
		try {
			FileWriter logFile = new FileWriter(logFilename, false);
			logFile.close();
		} catch (IOException e) {
			logFilename = null;
		}
		return logFilename;
	}

	public static void main(String[] args) {
		Game game = new Game(System.in, System.out);
		game.setLogFilename(getLogFilename());
		game.playUciGame();
	}

	Game(InputStream inputStream, PrintStream outputStream) {
		this.inStream = inputStream;
		this.outStream = outputStream;
	}

	void setLogFilename(String filename) {
		this.logFilename = filename;
	}

	private void writeToLog(String line) {
		if (logFilename != null && line != null) {
			try {
				FileWriter logFile = new FileWriter(logFilename, true);
				logFile.write(line + "\n");
				logFile.close();
			} catch (IOException e) {
			}
		}
	}

	private void println(String line) {
		writeToLog(_send + line);
		outStream.println(line);
	}

	void playUciGame() {
		Scanner scanner = new Scanner(this.inStream);
		String firstLine = scanner.nextLine();
		writeToLog(_recv + firstLine);
		if (_uci.equals(firstLine)) {
			println("id name de.thohee.useless.chess");
			println("id author thohee");
			println(_uciok);
			while (processCommand(scanner.nextLine()))
				;
			scanner.close();
		} else {
			println("I only understand uci.");
		}
	}

	private boolean processCommand(String inputLine) {
		try {
			writeToLog(_recv + inputLine);
			if (_isready.equals(inputLine)) {
				println(_readyok);
				return true;
			}
			if (inputLine != null && inputLine.startsWith(_position)) {
				String startPosAndMoves = inputLine.substring(_position.length()).trim();
				if (startPosAndMoves.startsWith(_startpos)) {
					boardPosition = BoardPosition.getInitialPosition();
					String maybeMoves = startPosAndMoves.substring(_startpos.length()).trim();
					if (maybeMoves.startsWith(_moves)) {
						String[] moveTokens = maybeMoves.substring(_moves.length()).trim().split(" ");
						for (String moveToken : moveTokens) {
							boardPosition = boardPosition.performUciMove(moveToken);
						}
					}
					this.player = new LexicographicMinimaxPlayer(boardPosition.getColourToMove(), true);
					this.player.setOutputWriter(this);
					return true;
				} else {
					println(_info + "Cannot read position.");
					return false;
				}
			}
			if (inputLine.startsWith(_go)) {
				if (boardPosition == null || player == null) {
					println("unknown position");
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
			println(_info + e.getMessage());
			return false;
		}
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
				try {
					Move move = player.makeMove(boardPosition, params);
					println(_bestmove + move.asUciMove());
				} catch (Throwable e) {
					println(_info + e.getClass().getSimpleName()
							+ (e.getMessage() != null ? ": " + e.getMessage() : ""));
					println(_bestmove + boardPosition.getPossibleMoves().get(0).asUciMove());
				}
			}
		});
		playerThread.start();
	}

	@Override
	public void debug(String line) {
		writeToLog(_debg + line);

	}

	@Override
	public void info(String line) {
		println(_info + line);
	}

}
