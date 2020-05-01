package de.thohee.useless.chess;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Coordinate;
import de.thohee.useless.chess.board.Move;

public class GameTest {

	private class CommandStream extends InputStream {

		private Queue<String> commands = new ConcurrentLinkedQueue<>();
		private StringReader stringReader = null;

		public synchronized void sendCommand(String command) {
			assert (command != null && !command.contains("\n"));
			commands.add(command);
		}

		@Override
		public int read() throws IOException {
			if (stringReader == null) {
				String command = commands.poll();
				while (command == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
					command = commands.poll();
				}
				stringReader = new StringReader(command + "\n");
				System.out.println("> " + command);
			}
			int aByte = stringReader.read();
			if (aByte == -1) {
				stringReader.close();
				stringReader = null;
			}
			return aByte;
		}
	}

	private class AnswerStream extends OutputStream {

		private Queue<String> lines = new ConcurrentLinkedQueue<>();

		public String popLine(long timeout) throws Exception {
			long starttime = System.currentTimeMillis();
			String line = lines.poll();
			while (line == null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
				if (System.currentTimeMillis() > starttime + timeout) {
					throw new Exception("timeout");
				}
				line = lines.poll();
			}
			System.out.print("< " + line);
			return line;
		}

		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		@Override
		public void write(int b) throws IOException {
			buffer.write(b);
			String line = new String(buffer.toByteArray());
			if (line.endsWith("\n")) {
				lines.add(line);
				buffer.reset();
			}
		}

	}

	private String getMoveToken(String response) {
		return response.substring("bestmove ".length());
	}

	private String getResponse(AnswerStream answerStream, long timeout) throws Exception {
		long starttime = System.currentTimeMillis();
		String response = null;
		do {
			if (System.currentTimeMillis() > starttime + timeout) {
				throw new Exception("timeout");
			}
			response = answerStream.popLine(timeout).stripTrailing();
		} while (response.startsWith("info"));
		return response;
	}

	private String getResponse(AnswerStream answerStream) throws Exception {
		return getResponse(answerStream, 500);
	}

	@Test
	public void testPlayUciGame() throws Exception {

		CommandStream commandStream = new CommandStream();
		AnswerStream answerStream = new AnswerStream();

		Game game = new Game(commandStream, new PrintStream(answerStream));

		Thread gameThread = new Thread(new Runnable() {
			@Override
			public void run() {
				game.playUciGame();
			}
		});

		try {
			gameThread.start();
			commandStream.sendCommand("uci");
			assertTrue(getResponse(answerStream).startsWith("id name"));
			assertTrue(getResponse(answerStream).startsWith("id author"));
			assertTrue(getResponse(answerStream).startsWith("uciok"));
			commandStream.sendCommand("isready");
			assertTrue(getResponse(answerStream).startsWith("readyok"));
			BoardPosition boardPosition = BoardPosition.getInitialPosition();
			boardPosition = boardPosition
					.performMove(boardPosition.getMove(Coordinate.parse("e2"), Coordinate.parse("e4"), null));
			String position = "position startpos moves e2e4";
			commandStream.sendCommand(position);
			commandStream.sendCommand("go infinite");
			Thread.sleep(500);
			commandStream.sendCommand("stop");
			String response = getResponse(answerStream);
			assertTrue(response.startsWith("bestmove"));
			Move engineMove = boardPosition.parseUciMove(getMoveToken(response));
			boardPosition = boardPosition.performMove(engineMove);
			position += " " + engineMove.asUciMove();
			Move testMove = boardPosition.getPossibleMoves().iterator().next();
			boardPosition = boardPosition.performMove(testMove);
			position += " " + testMove.asUciMove();
			commandStream.sendCommand(position);
			commandStream.sendCommand("go depth 3");
			response = getResponse(answerStream, 5000);
			assertTrue(response.startsWith("bestmove"));
			engineMove = boardPosition.parseUciMove(getMoveToken(response));
			boardPosition = boardPosition.performMove(engineMove);
			position += " " + engineMove.asUciMove();
			testMove = boardPosition.getPossibleMoves().iterator().next();
			boardPosition = boardPosition.performMove(testMove);
			position += " " + testMove.asUciMove();
			commandStream.sendCommand(position);
			final long maxTime = 500;
			commandStream.sendCommand("go movetime " + maxTime);
			long starttime = System.currentTimeMillis();
			response = getResponse(answerStream, 2 * maxTime);
			assertTrue(response.startsWith("bestmove"));
			long duration = System.currentTimeMillis() - starttime;
			System.out.println("> took " + Long.toString(duration) + "ms");
			// assertTrue(duration <= maxTime);
		} finally {
			commandStream.sendCommand("quit");
			gameThread.join();
		}
	}

	@Test
	public void testAvoidInvalidChoice() throws Exception {

		CommandStream commandStream = new CommandStream();
		AnswerStream answerStream = new AnswerStream();

		Game game = new Game(commandStream, new PrintStream(answerStream));
		game.setLogFilename("Game.log");

		Thread gameThread = new Thread(new Runnable() {
			@Override
			public void run() {
				game.playUciGame();
			}
		});

		try {
			gameThread.start();
			commandStream.sendCommand("uci");
			assertTrue(getResponse(answerStream).startsWith("id name"));
			assertTrue(getResponse(answerStream).startsWith("id author"));
			assertTrue(getResponse(answerStream).startsWith("uciok"));
			commandStream.sendCommand("isready");
			assertTrue(getResponse(answerStream).startsWith("readyok"));
			commandStream.sendCommand(
					"position startpos moves e2e3 d7d6 f1e2 e7e5 d2d4 e5e4 f2f3 d8h4 g2g3 h4e7 f3e4 e7e4 e2f3 e4f5 e3e4 f5b5 d4d5 f7f6 b1d2 b7b6 c2c4 b5b4 b2b3 g7g5 g1e2 b8d7 e1g1 d7e5 e2d4 e5g6 f3h5");
			commandStream.sendCommand("go depth 4");

			assertTrue(getResponse(answerStream, 10000).startsWith("bestmove"));

		} finally {
			commandStream.sendCommand("quit");
			gameThread.join();
		}

	}

	@Test
	public void testAcceptPromotionToKnight() throws Exception {

		CommandStream commandStream = new CommandStream();
		AnswerStream answerStream = new AnswerStream();

		Game game = new Game(commandStream, new PrintStream(answerStream));

		Thread gameThread = new Thread(new Runnable() {
			@Override
			public void run() {
				game.playUciGame();
			}
		});

		try {
			gameThread.start();
			commandStream.sendCommand("uci");
			assertTrue(getResponse(answerStream).startsWith("id name"));
			assertTrue(getResponse(answerStream).startsWith("id author"));
			assertTrue(getResponse(answerStream).startsWith("uciok"));
			commandStream.sendCommand("isready");
			assertTrue(getResponse(answerStream).startsWith("readyok"));
			commandStream.sendCommand(
					"position startpos moves d2d3 e7e5 c2c3 d7d5 e2e3 c8e6 g2g4 b8c6 h2h3 f8d6 b2b4 a7a6 b1d2 g8e7 d2f3 e8g8 c1a3 f7f5 f3g5 d8d7 g5e6 d7e6 g4g5 f5f4 d1g4 e6g4 h3g4 f4e3 f2e3 f8f7 f1h3 a8f8 e1d1 c6d8 d1c1 d8e6 h1h2 e6g5 h2b2 g5h3 b2g2 f7f1 c1b2 f1a1 b2a1 f8f1 a1b2 f1g1 g2g1 h3g1 b2b1 g1e2 b1b2 g7g6 g4g5 g8g7 b2c2 h7h6 g5h6 g7h6 c2b2 g6g5 b2b3 g5g4 b3b2 g4g3 b4b5 d6a3 b2a3 a6b5 c3c4 b5c4 e3e4 c4d3 e4d5 e7d5 a3b3 d3d2 b3c2 g3g2 c2b1 g2g1q b1b2 d2d1n");
			commandStream.sendCommand("go depth 5");

			assertTrue(getResponse(answerStream, 10000).startsWith("bestmove"));

		} finally {
			commandStream.sendCommand("quit");
			gameThread.join();
		}

	}
}
