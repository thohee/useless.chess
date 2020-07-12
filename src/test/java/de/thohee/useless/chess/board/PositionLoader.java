package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;

public class PositionLoader {

	public static BoardPosition loadPosition(String gameFilename, Integer plies)
			throws FileNotFoundException, IllegalMoveFormatException {
		File pgnFile = new File("src/test/resources/games/" + gameFilename);
		assertTrue(pgnFile.exists() && pgnFile.isFile());
		List<GameReport> games = PGNParser.parse(pgnFile.getPath());
		assertEquals(1, games.size());
		GameReport gameReport = games.get(0);
		BoardPosition boardPosition = gameReport.getInitialPosition();
		int max = plies != null ? plies.intValue() : gameReport.getMoves().size();
		for (int m = 0; m < max; ++m) {
			Move move = gameReport.getMoves().get(m);
			boardPosition = boardPosition.performMove(move);
		}
		return boardPosition;
	}

	public static BoardPosition loadPosition(String gameFilename)
			throws FileNotFoundException, IllegalMoveFormatException {
		return loadPosition(gameFilename, null);
	}
}
