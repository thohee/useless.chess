package useless.chess.board;

public class Piece {

	private Colour colour;
	private Figure figure;

	public Piece(Colour colour, Figure figure) {
		this.colour = colour;
		this.figure = figure;
	}

	public Colour getColour() {
		return colour;
	}

	public Figure getFigure() {
		return figure;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result + ((figure == null) ? 0 : figure.hashCode());
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
		Piece other = (Piece) obj;
		if (colour != other.colour)
			return false;
		if (figure != other.figure)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return colour.name() + " " + figure.name();
	}

	private static String WHITE_KING = new String(Character.toChars(0x2654));
	private static String WHITE_QUEEN = new String(Character.toChars(0x2655));
	private static String WHITE_ROOK = new String(Character.toChars(0x2656));
	private static String WHITE_BISHOP = new String(Character.toChars(0x2657));
	private static String WHITE_KNIGHT = new String(Character.toChars(0x2658));
	private static String WHITE_PAWN = new String(Character.toChars(0x2659));
	private static String BLACK_KING = new String(Character.toChars(0x265A));
	private static String BLACK_QUEEN = new String(Character.toChars(0x265B));
	private static String BLACK_ROOK = new String(Character.toChars(0x265C));
	private static String BLACK_BISHOP = new String(Character.toChars(0x265D));
	private static String BLACK_KNIGHT = new String(Character.toChars(0x265E));
	private static String BLACK_PAWN = new String(Character.toChars(0x265F));

	public String toUnicode() {
		switch (this.colour) {
		case White:
			switch (this.figure) {
			case King:
				return WHITE_KING;
			case Queen:
				return WHITE_QUEEN;
			case Rook:
				return WHITE_ROOK;
			case Bishop:
				return WHITE_BISHOP;
			case Knight:
				return WHITE_KNIGHT;
			case Pawn:
				return WHITE_PAWN;
			}
			break;
		case Black:
			switch (this.figure) {
			case King:
				return BLACK_KING;
			case Queen:
				return BLACK_QUEEN;
			case Rook:
				return BLACK_ROOK;
			case Bishop:
				return BLACK_BISHOP;
			case Knight:
				return BLACK_KNIGHT;
			case Pawn:
				return BLACK_PAWN;
			}
			break;
		}
		return "?";
	}
}
