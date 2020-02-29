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

	public static class Params {
		public Long maxTimeInMillis = null;
		public Integer maxDepthInPlies = null;
		public boolean infinite = false;
	}

	public abstract Move makeMove(BoardPosition boardPosition, Params params);

	public void stop() {
		// can be implemented by engines to accept a concurrent interrupt signal for makeMove 
	}

	public static interface OutputWriter {
		void writeLine(String line);
	}

	public void setOutputWriter(OutputWriter stream) {
		// can be implemented by engines to output debug information
	}

}
