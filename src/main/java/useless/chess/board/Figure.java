package useless.chess.board;

public enum Figure {
	King, Queen, Rook, Bishop, Knight, Pawn;

	@Override
	public String toString() {
		switch (this) {
		case King:
			return "K";
		case Queen:
			return "Q";
		case Rook:
			return "R";
		case Bishop:
			return "B";
		case Knight:
			return "N";
		case Pawn:
			return "P";
		default:
			return null;
		}
	}

	public static Figure parse(String s) {
		switch (s) {
		case "K":
			return King;
		case "Q":
			return Queen;
		case "R":
			return Rook;
		case "B":
			return Bishop;
		case "N":
			return Knight;
		default:
			return Pawn;
		}
	}
}
