package de.thohee.useless.chess.player;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Move;

public class MinimaxPlayerTest {

	@Ignore
	@Test
	public void testPlay() {

		Player.Params params = new Player.Params();
		params.maxDepthInPlies = 5;

		long totalDuration = 0L;
		long numberOfMoves = 0L;

		for (Colour ownColour : Colour.values()) {

			for (int seed : Arrays.asList(13, 47, 71)) {

				Player opponent = new RandomPlayer(ownColour.opposite(), seed);
				LexicographicMinimaxPlayer self = new LexicographicMinimaxPlayer(ownColour, true);

				Map<Colour, Player> players = new HashMap<>();
				players.put(opponent.getColour(), opponent);
				players.put(self.getColour(), self);

				BoardPosition boardPosition = BoardPosition.getInitialPosition();

				int m = 1;
				while (!boardPosition.getPossibleMoves().isEmpty()) {
					Player player = players.get(boardPosition.getColourToMove());
					long starttime = System.currentTimeMillis();
					Move move = player.makeMove(boardPosition, params);
					long duration = System.currentTimeMillis() - starttime;
					if (player.getColour().equals(ownColour)) {
						totalDuration += duration;
						++numberOfMoves;
					}
					if (boardPosition.getColourToMove().equals(Colour.White)) {
						System.out.print(Integer.toString(m) + ": " + move.toString());
						++m;
					} else {
						System.out.println(" " + move.toString());
					}
					boardPosition = boardPosition.performMove(move);
				}
				if (boardPosition.getColourToMove().equals(Colour.Black)) {
					System.out.println();
				}
				System.out.println(boardPosition.getResult());
				System.out.println(boardPosition.toString());
				assertTrue(boardPosition.isCheckmate() && boardPosition.getColourToMove().equals(ownColour.opposite()));
			}
		}

		double avgDuration = (double) totalDuration / (double) numberOfMoves;
		System.out.println("average thinking duration: " + avgDuration);
	}
}
