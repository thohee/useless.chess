package useless.chess.player;

import java.util.List;
import java.util.Random;

import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.Move;

public class RandomPlayer extends Player {

	private Random randomGenerator = null;

	public RandomPlayer(Colour colour) {
		super(colour);
		randomGenerator = new Random(29071980);
	}

	public RandomPlayer(Colour colour, long seed) {
		super(colour);
		randomGenerator = new Random(seed);
	}

	@Override
	public Move makeMove(BoardPosition boardPosition) {
		List<Move> possibleMoves = boardPosition.getPossibleMoves();
		return possibleMoves.get(randomGenerator.nextInt(possibleMoves.size()));
	}

}
