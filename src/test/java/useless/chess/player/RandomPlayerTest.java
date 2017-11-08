package useless.chess.player;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.Move;

public class RandomPlayerTest {

	@Test
	public void testPlay() {

		for (long i = 1; i <= 10; ++i) {

			long seed = 13L * i;
			System.out.print(Long.toString(seed) + ": ");

			Player white = new RandomPlayer(Colour.White, seed);
			Player black = new RandomPlayer(Colour.Black, seed);

			BoardPosition boardPosition = BoardPosition.getInitialPosition();

			while (!boardPosition.getPossibleMoves().isEmpty()) {
				Player player = boardPosition.getColourToMove().equals(Colour.White) ? white : black;
				Move move = player.makeMove(boardPosition);
				boardPosition = boardPosition.performMove(move);
			}
			System.out
					.println(Long.toString(boardPosition.getDepth()) + " moves, result: " + boardPosition.getResult());
			assertTrue(boardPosition.isCheckmate() || boardPosition.isDraw());
		}
	}

}
