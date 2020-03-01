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

		public String popLine() {
			String line = lines.poll();
			while (line == null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
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

	private Move parseMove(BoardPosition boardPosition, String response) {
		String moveToken = response.substring("bestmove ".length());
		Coordinate from = Coordinate.parse(moveToken.substring(0, 2));
		Coordinate to = Coordinate.parse(moveToken.substring(2, 4));
		return boardPosition.getMove(from, to);
	}

	private String getResponse(AnswerStream answerStream) {
		String response = null;
		do {
			response = answerStream.popLine();
		} while (response.startsWith("info"));
		return response;
	}

	@Test
	public void testPlayUciGame() throws InterruptedException {

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
					.performMove(boardPosition.getMove(Coordinate.parse("e2"), Coordinate.parse("e4")));
			String position = "position startpos moves e2e4";
			commandStream.sendCommand(position);
			commandStream.sendCommand("go infinite");
			Thread.sleep(500);
			commandStream.sendCommand("stop");
			String response = getResponse(answerStream);
			assertTrue(response.startsWith("bestmove"));
			Move engineMove = parseMove(boardPosition, response);
			boardPosition = boardPosition.performMove(engineMove);
			position += " " + engineMove.asUciMove();
			Move testMove = boardPosition.getPossibleMoves().iterator().next();
			boardPosition = boardPosition.performMove(testMove);
			position += " " + testMove.asUciMove();
			commandStream.sendCommand(position);
			commandStream.sendCommand("go depth 3");
			response = getResponse(answerStream);
			assertTrue(response.startsWith("bestmove"));
			engineMove = parseMove(boardPosition, response);
			boardPosition = boardPosition.performMove(engineMove);
			position += " " + engineMove.asUciMove();
			testMove = boardPosition.getPossibleMoves().iterator().next();
			boardPosition = boardPosition.performMove(testMove);
			position += " " + testMove.asUciMove();
			commandStream.sendCommand(position);
			final long maxTime = 5000;
			commandStream.sendCommand("go movetime " + maxTime);
			long starttime = System.currentTimeMillis();
			response = getResponse(answerStream);
			assertTrue(response.startsWith("bestmove"));
			long duration = System.currentTimeMillis() - starttime;
			System.out.println("> took " + Long.toString(duration) + "ms");
			//assertTrue(duration <= maxTime);
		} finally {
			commandStream.sendCommand("quit");
			gameThread.join();
		}
	}

}
