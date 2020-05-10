package de.thohee.useless.chess.player;

import de.thohee.useless.chess.board.BoardPosition;

/**
 * This interfaces allows for scalar values as well as value vectors
 * 
 * @author Thomas
 *
 */
public interface Value extends Comparable<Value> {

	boolean isInvalid();

	boolean isMin();

	boolean isMax();

	BoardPosition getBoardPosition();

	void setBoardPosition(BoardPosition boardPosition);
}
