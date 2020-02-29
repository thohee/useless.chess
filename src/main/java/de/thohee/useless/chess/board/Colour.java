package de.thohee.useless.chess.board;

public enum Colour {
	White, Black;

	public Colour opposite() {
		return Colour.values()[1 - this.ordinal()];
	}
}
