package de.thohee.useless.chess.player;

import java.io.PrintStream;
import java.util.Map;

import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Move;

public abstract class EnginePlayer extends Player {

	public EnginePlayer(Colour colour) {
		super(colour);
	}

	public abstract void printEvaluatedChoices(PrintStream printStream);

	public abstract Map<Move, Value> getEvaluatedMoves();

}
