package de.thohee.useless.chess.board;

public class FENParser {

	public static BoardPosition parse(String fenString) {
		assert (fenString != null);
		BoardPosition boardPosition = new BoardPosition();

		String[] fields = fenString.split(" ");
		assert (fields.length == 6);

		positionPieces(fields[0], boardPosition);

		setColourToMove(fields[1], boardPosition);

		setPossibleCastlings(fields[2], boardPosition);

		setLastPawnMove(fields[3], boardPosition);

		setNumberOfMovesWithoutPawnOrCapture(fields[4], boardPosition);

		setDepth(fields[5], boardPosition);

		return boardPosition;
	}

	private static void positionPieces(String positionString, BoardPosition boardPosition) {
		String[] rows = positionString.split("/");
		for (int r = 7; r >= 0; --r) {
			char[] row = rows[7 - r].toCharArray();
			int c = 0;
			int p = 0;
			while (c < 8 && p < row.length) {
				Piece piece = createPiece(row[p]);
				if (piece != null) {
					boardPosition.set(Coordinate.get(c, r), piece);
					++c;
				} else {
					int nEmptyCells = row[p] - 48;
					c += nEmptyCells;
				}
				++p;
			}
		}
	}

	private static Piece createPiece(char c) {
		switch (c) {
		case 'K':
			return new Piece(Colour.White, Figure.King);
		case 'Q':
			return new Piece(Colour.White, Figure.Queen);
		case 'B':
			return new Piece(Colour.White, Figure.Bishop);
		case 'N':
			return new Piece(Colour.White, Figure.Knight);
		case 'R':
			return new Piece(Colour.White, Figure.Rook);
		case 'P':
			return new Piece(Colour.White, Figure.Pawn);
		case 'k':
			return new Piece(Colour.Black, Figure.King);
		case 'q':
			return new Piece(Colour.Black, Figure.Queen);
		case 'b':
			return new Piece(Colour.Black, Figure.Bishop);
		case 'n':
			return new Piece(Colour.Black, Figure.Knight);
		case 'r':
			return new Piece(Colour.Black, Figure.Rook);
		case 'p':
			return new Piece(Colour.Black, Figure.Pawn);
		default:
			return null;
		}
	}

	private static void setColourToMove(String colourToMove, BoardPosition boardPosition) {
		switch (colourToMove) {
		case "w":
			boardPosition.setColourToMove(Colour.White);
			break;
		case "b":
			boardPosition.setColourToMove(Colour.Black);
			break;
		}
	}

	private static void setPossibleCastlings(String string, BoardPosition boardPosition) {
		// TODO Auto-generated method stub

	}

	private static void setLastPawnMove(String string, BoardPosition boardPosition) {
		// TODO Auto-generated method stub

	}

	private static void setNumberOfMovesWithoutPawnOrCapture(String string, BoardPosition boardPosition) {
		// TODO Auto-generated method stub

	}

	private static void setDepth(String field, BoardPosition boardPosition) {
		int nextFullMove = Integer.parseInt(field);
		long depth = 2 * (nextFullMove - 1);
		if (boardPosition.getColourToMove().equals(Colour.Black)) {
			++depth;
		}
		boardPosition.setDepth(depth);
	}

}
