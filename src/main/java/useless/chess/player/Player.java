package useless.chess.player;

import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.Move;

public abstract class Player {

	protected Colour colour;

	public Player(Colour colour) {
		this.colour = colour;
	}

	public Colour getColour() {
		return colour;
	}

	public abstract Move makeMove(BoardPosition boardPosition);

}
