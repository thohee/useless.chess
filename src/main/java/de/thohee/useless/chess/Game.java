package de.thohee.useless.chess;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Scanner;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.player.ReadyPlayer1;
import de.thohee.useless.chess.player.Player;
import de.thohee.useless.chess.player.RandomPlayer;

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

	private PlayerConfiguration playerConfiguration;
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

	static class PlayerConfiguration {
		Class<? extends Player> playerClass;
		Class<?>[] constructorParameterTypes;
		Object[] constructorParameters;
	}

	static PlayerConfiguration createPlayerConfiguration(String classSimpleName, Object... additionalParams)
			throws Exception {
		if (ReadyPlayer1.class.getSimpleName().equals(classSimpleName)) {
			return createPlayerConfiguration(ReadyPlayer1.class, true);
		} else if (RandomPlayer.class.getSimpleName().equals(classSimpleName)) {
			return createPlayerConfiguration(RandomPlayer.class, additionalParams);
		} else {
			throw new Exception(classSimpleName);
		}
	}

	private static PlayerConfiguration createPlayerConfiguration(Class<? extends Player> playerClass,
			Object... additionalParams) {
		PlayerConfiguration playerConfiguration = new PlayerConfiguration();
		playerConfiguration.playerClass = playerClass;
		playerConfiguration.constructorParameterTypes = new Class<?>[additionalParams.length + 1];
		playerConfiguration.constructorParameterTypes[0] = Colour.class; // obligatory parameter for all players
		playerConfiguration.constructorParameters = new Object[additionalParams.length + 1];
		playerConfiguration.constructorParameters[0] = null; // will be decided later
		for (int p = 0; p < additionalParams.length; ++p) {
			playerConfiguration.constructorParameterTypes[p + 1] = additionalParams[p].getClass();
			playerConfiguration.constructorParameters[p + 1] = additionalParams[p];
		}
		return playerConfiguration;
	}

	private static String getPlayerClassName(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("--player") && i < args.length - 1) {
				return args[i + 1];
			}
		}
		return ReadyPlayer1.class.getSimpleName();
	}

	private static Object[] getPlayerOptions(String[] args) {
		ArrayList<Object> options = new ArrayList<>();
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("--player")) {
				int o = i + 2;
				while (o < args.length) {
					String arg = args[o];
					++o;
					try {
						options.add(Long.parseLong(arg));
					} catch (NumberFormatException e) {
						options.add(arg);
					}
				}
				break;
			}
		}
		return options.toArray();
	}

	public static void main(String[] args) {
		try {
			String playerClassName = getPlayerClassName(args);
			Object[] playerOptions = getPlayerOptions(args);
			Game game = new Game(System.in, System.out, createPlayerConfiguration(playerClassName, playerOptions));
			game.setLogFilename(getLogFilename());
			game.playUciGame();
		} catch (Throwable e) {
			System.err.println(e.getMessage());
		}
	}

	Game(InputStream inputStream, PrintStream outputStream, PlayerConfiguration playerConfiguration) {
		this.inStream = inputStream;
		this.outStream = outputStream;
		this.playerConfiguration = playerConfiguration;
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
					Constructor<? extends Player> constructor = playerConfiguration.playerClass
							.getConstructor(playerConfiguration.constructorParameterTypes);
					Object[] params = playerConfiguration.constructorParameters.clone();
					params[0] = boardPosition.getColourToMove();
					this.player = constructor.newInstance(params);
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
