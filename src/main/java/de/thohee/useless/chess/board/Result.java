package de.thohee.useless.chess.board;

public enum Result {
	White, Black, Draw, None;

	@Override
	public String toString() {
		switch (this) {
		case White:
			return "1-0";
		case Black:
			return "0-1";
		case Draw:
			return "1/2-1/2";
		default:
			return "???";
		}
	}

	public static Result parse(String s) {
		if (s == null || s.isEmpty()) {
			throw new IllegalArgumentException(s);
		} else if (s.endsWith("1-0")) {
			return Result.White;
		} else if (s.endsWith("0-1")) {
			return Result.Black;
		} else if (s.endsWith("1/2-1/2")) {
			return Result.Draw;
		} else {
			return Result.None;
		}
	}
}