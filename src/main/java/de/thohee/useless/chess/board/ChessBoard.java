package de.thohee.useless.chess.board;

import java.util.Iterator;

public class ChessBoard {

	private Piece[] chessboard = new Piece[64];
	private int pieceCount = 0;
	private Integer cachedHash = null;

	public ChessBoard() {
	}

	public ChessBoard(ChessBoard other) {
		this.chessboard = other.chessboard.clone();
		this.pieceCount = other.pieceCount;
	}

	public Piece get(Coordinate coordinate) {
		return chessboard[coordinate.ordinal()];
	}

	public void put(Coordinate coordinate, Piece piece) {
		if (chessboard[coordinate.ordinal()] == null) {
			++pieceCount;
		}
		chessboard[coordinate.ordinal()] = piece;
		cachedHash = null;
	}

	public void remove(Coordinate coordinate) {
		if (chessboard[coordinate.ordinal()] != null) {
			--pieceCount;
		}
		chessboard[coordinate.ordinal()] = null;
		cachedHash = null;
	}

	public int size() {
		return pieceCount;
	}

	private static class AbstractIterator {

		private Piece[] chessboard = null;
		private int indexOfNextPiece = -1;

		protected AbstractIterator(Piece[] chessboard) {
			this.chessboard = chessboard;
			moveToNext();
		}

		protected void moveToNext() {
			do {
				++indexOfNextPiece;
			} while (indexOfNextPiece < chessboard.length && chessboard[indexOfNextPiece] == null);
		}

		public boolean hasNext() {
			return indexOfNextPiece < chessboard.length;
		}

		protected Piece nextPiece() {
			return chessboard[indexOfNextPiece];
		}

		protected Coordinate nextCoordinate() {
			return Coordinate.values()[indexOfNextPiece];
		}

	}

	private static class PieceIterator extends AbstractIterator implements Iterator<Piece> {

		protected PieceIterator(Piece[] chessBoard) {
			super(chessBoard);
		}

		@Override
		public Piece next() {
			assert (hasNext());
			Piece piece = nextPiece();
			moveToNext();
			return piece;
		}

	}

	private static class PositionedPieceIterator extends AbstractIterator implements Iterator<PositionedPiece> {

		protected PositionedPieceIterator(Piece[] chessBoard) {
			super(chessBoard);
		}

		@Override
		public PositionedPiece next() {
			assert (hasNext());
			PositionedPiece positionedPiece = new PositionedPiece(nextCoordinate(), nextPiece());
			moveToNext();
			return positionedPiece;
		}

	}

	public Iterator<Piece> pieces() {
		return new PieceIterator(this.chessboard);
	}

	public Iterator<PositionedPiece> positionedPieces() {
		return new PositionedPieceIterator(this.chessboard);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof ChessBoard) {
			ChessBoard other = (ChessBoard) obj;
			if (this.pieceCount != other.pieceCount) {
				return false;
			}
			for (int i = 0; i < chessboard.length; ++i) {
				Piece piece = chessboard[i];
				Piece otherPiece = other.chessboard[i];
				if (piece != null && otherPiece != null) {
					if (piece.getFigure() != otherPiece.getFigure() || piece.getColour() != otherPiece.getColour()) {
						return false;
					}
				} else if ((piece == null) != (otherPiece == null)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (cachedHash == null) {
			final int prime = 31;
			int hash = 1;
			for (int i = 0; i < chessboard.length; ++i) {
				Piece piece = chessboard[i];
				if (piece != null) {
					hash = hash * prime + i;
					hash = hash * prime + piece.getFigure().ordinal() + 1;
					hash = hash * prime + piece.getColour().ordinal() + 1;
				}
			}
			hash += 100 * hash + pieceCount;
			cachedHash = hash;
		}
		return cachedHash;
	}

}
