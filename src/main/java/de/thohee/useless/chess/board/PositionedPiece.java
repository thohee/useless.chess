package de.thohee.useless.chess.board;

public class PositionedPiece {

	private Coordinate coordinate;
	private Piece piece;

	public PositionedPiece(Coordinate coordinate, Piece piece) {
		this.coordinate = coordinate;
		this.piece = piece;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public Piece getPiece() {
		return piece;
	}

}
