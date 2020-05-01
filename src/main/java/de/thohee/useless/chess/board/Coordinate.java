package de.thohee.useless.chess.board;

public enum Coordinate {

	// @formatter:off

	a1("a1", 0, 0),
	a2("a2", 0, 1),
	a3("a3", 0, 2),
	a4("a4", 0, 3),
	a5("a5", 0, 4),
	a6("a6", 0, 5),
	a7("a7", 0, 6),
	a8("a8", 0, 7),

	b1("b1", 1, 0),
	b2("b2", 1, 1),
	b3("b3", 1, 2),
	b4("b4", 1, 3),
	b5("b5", 1, 4),
	b6("b6", 1, 5),
	b7("b7", 1, 6),
	b8("b8", 1, 7),

	c1("c1", 2, 0),
	c2("c2", 2, 1),
	c3("c3", 2, 2),
	c4("c4", 2, 3),
	c5("c5", 2, 4),
	c6("c6", 2, 5),
	c7("c7", 2, 6),
	c8("c8", 2, 7),

	d1("d1", 3, 0),
	d2("d2", 3, 1),
	d3("d3", 3, 2),
	d4("d4", 3, 3),
	d5("d5", 3, 4),
	d6("d6", 3, 5),
	d7("d7", 3, 6),
	d8("d8", 3, 7),

	e1("e1", 4, 0),
	e2("e2", 4, 1),
	e3("e3", 4, 2),
	e4("e4", 4, 3),
	e5("e5", 4, 4),
	e6("e6", 4, 5),
	e7("e7", 4, 6),
	e8("e8", 4, 7),

	f1("f1", 5, 0),
	f2("f2", 5, 1),
	f3("f3", 5, 2),
	f4("f4", 5, 3),
	f5("f5", 5, 4),
	f6("f6", 5, 5),
	f7("f7", 5, 6),
	f8("f8", 5, 7),

	g1("g1", 6, 0),
	g2("g2", 6, 1),
	g3("g3", 6, 2),
	g4("g4", 6, 3),
	g5("g5", 6, 4),
	g6("g6", 6, 5),
	g7("g7", 6, 6),
	g8("g8", 6, 7),

	h1("h1", 7, 0),
	h2("h2", 7, 1),
	h3("h3", 7, 2),
	h4("h4", 7, 3),
	h5("h5", 7, 4),
	h6("h6", 7, 5),
	h7("h7", 7, 6),
	h8("h8", 7, 7);

	// @formatter:on

	private String name;
	private int column;
	private int row;

	public static Coordinate get(int column, int row) {
		assert (0 <= column && column < 8 && 0 <= row && row < 8);
		return values()[column * 8 + row];
	}

	private Coordinate(String name, int column, int row) {
		this.name = name;
		this.column = column;
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	@Override
	public String toString() {
		return name;
	}

	public static Coordinate parse(String s) {
		if (s == null) {
			return null;
		}
		switch (s) {
		case "a1":
			return a1;
		case "a2":
			return a2;
		case "a3":
			return a3;
		case "a4":
			return a4;
		case "a5":
			return a5;
		case "a6":
			return a6;
		case "a7":
			return a7;
		case "a8":
			return a8;
		case "b1":
			return b1;
		case "b2":
			return b2;
		case "b3":
			return b3;
		case "b4":
			return b4;
		case "b5":
			return b5;
		case "b6":
			return b6;
		case "b7":
			return b7;
		case "b8":
			return b8;
		case "c1":
			return c1;
		case "c2":
			return c2;
		case "c3":
			return c3;
		case "c4":
			return c4;
		case "c5":
			return c5;
		case "c6":
			return c6;
		case "c7":
			return c7;
		case "c8":
			return c8;
		case "d1":
			return d1;
		case "d2":
			return d2;
		case "d3":
			return d3;
		case "d4":
			return d4;
		case "d5":
			return d5;
		case "d6":
			return d6;
		case "d7":
			return d7;
		case "d8":
			return d8;
		case "e1":
			return e1;
		case "e2":
			return e2;
		case "e3":
			return e3;
		case "e4":
			return e4;
		case "e5":
			return e5;
		case "e6":
			return e6;
		case "e7":
			return e7;
		case "e8":
			return e8;
		case "f1":
			return f1;
		case "f2":
			return f2;
		case "f3":
			return f3;
		case "f4":
			return f4;
		case "f5":
			return f5;
		case "f6":
			return f6;
		case "f7":
			return f7;
		case "f8":
			return f8;
		case "g1":
			return g1;
		case "g2":
			return g2;
		case "g3":
			return g3;
		case "g4":
			return g4;
		case "g5":
			return g5;
		case "g6":
			return g6;
		case "g7":
			return g7;
		case "g8":
			return g8;
		case "h1":
			return h1;
		case "h2":
			return h2;
		case "h3":
			return h3;
		case "h4":
			return h4;
		case "h5":
			return h5;
		case "h6":
			return h6;
		case "h7":
			return h7;
		case "h8":
			return h8;
		default:
			return null;
		}
	}

}
