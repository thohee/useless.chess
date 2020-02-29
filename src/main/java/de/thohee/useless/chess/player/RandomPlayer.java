package de.thohee.useless.chess.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Move;

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
	public Move makeMove(BoardPosition boardPosition, Params params) {
		List<Move> possibleMoves = new ArrayList<>(boardPosition.getPossibleMoves());
		return possibleMoves.get(randomGenerator.nextInt(possibleMoves.size()));
	}

}
