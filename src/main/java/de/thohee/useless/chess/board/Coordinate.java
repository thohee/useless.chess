package de.thohee.useless.chess.board;

public class Coordinate {

	private static final String[] columns = { "a", "b", "c", "d", "e", "f", "g", "h" };

	private int column;
	private int row;

	public Coordinate(int column, int row) {
		assert (0 <= column && column < 8 && 0 <= row && row < 8);
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
		return columns[column] + (row + 1);
	}

	public static Coordinate parse(String s) {
		if (s != null && s.length() == 2) {
			String s1 = s.substring(0, 1);
			for (int c = 0; c < 8; ++c) {
				if (columns[c].equals(s1)) {
					try {
						int r = Integer.parseInt(s.substring(1, 2)) - 1;
						if (0 <= r && r < 8) {
							return new Coordinate(c, r);
						}
					} finally {
					}
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (column != other.column)
			return false;
		if (row != other.row)
			return false;
		return true;
	}

}
