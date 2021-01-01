package de.thohee.useless.chess.board;

import java.util.List;

public class Move {

	public enum Castling {
		KingSide, QueenSide
	}

	public enum Capture {
		None, Regular, EnPassant
	}

	private Colour colour;

	// either a move of one figure
	private Figure figure = null;
	private Coordinate from = null;
	private Coordinate to = null;
	private Figure newFigure = null;
	private Capture capture = null;

	// or a castling
	private Castling castling = null;

	public Move(Colour colour, Figure figure, Coordinate from, Coordinate to, Capture capture) {
		this.colour = colour;
		this.figure = figure;
		this.from = from;
		this.to = to;
		this.capture = capture;
	}

	public Move(Colour colour, Figure figure, Coordinate from, Coordinate to, Capture capture, Figure newFigure) {
		assert (figure.equals(Figure.Pawn));
		this.colour = colour;
		this.figure = figure;
		this.from = from;
		this.to = to;
		this.capture = capture;
		this.newFigure = newFigure;
	}

	public Move(Colour colour, Castling castling) {
		this.colour = colour;
		this.castling = castling;
	}

	public Colour getColour() {
		return colour;
	}

	public Figure getFigure() {
		return figure;
	}

	public Coordinate getFrom() {
		return from;
	}

	public Coordinate getTo() {
		return to;
	}

	public Castling getCastling() {
		return castling;
	}

	public Capture getCapture() {
		return capture;
	}

	public Figure getNewFigure() {
		return newFigure;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capture == null) ? 0 : capture.hashCode());
		result = prime * result + ((castling == null) ? 0 : castling.hashCode());
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result + ((figure == null) ? 0 : figure.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((newFigure == null) ? 0 : newFigure.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		Move other = (Move) obj;
		if (capture != other.capture)
			return false;
		if (castling != other.castling)
			return false;
		if (colour != other.colour)
			return false;
		if (figure != other.figure)
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (newFigure == null) {
			if (other.newFigure != null) {
				return false;
			}
		} else if (!newFigure.equals(other.newFigure)) {
			return false;
		}
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean pgn) {
		if (castling != null) {
			if (pgn) {
				return Castling.KingSide.equals(castling) ? "O-O" : "O-O-O";
			} else {
				return Castling.KingSide.equals(castling) ? "0-0" : "0-0-0";
			}
		} else {
			return (Figure.Pawn.equals(figure) ? "" : figure.toString()) + from.toString()
					+ (capture != Capture.None ? "x" : "-") + to.toString()
					+ (newFigure != null ? "=" + newFigure.toString() : "")
					+ (capture.equals(Capture.EnPassant) ? "e.p." : "");
		}
	}

	public String asUciMove() {
		if (castling != null) {
			String row = colour.equals(Colour.White) ? "1" : "8";
			String fromColumn = "e";
			String toColumn = castling.equals(Castling.KingSide) ? "g" : "c";
			return fromColumn + row + toColumn + row;
		} else {
			return from.toString() + to.toString();
		}
	}

	public static class IllegalMoveFormatException extends Exception {

		private static final long serialVersionUID = 5113928583330763865L;

		public IllegalMoveFormatException(String s) {
			super(s);
		}
	}

	public static Move parse(Colour colour, String s) throws IllegalMoveFormatException {
		Move move = null;
		switch (s) {
		case "0-0":
			move = new Move(colour, Castling.KingSide);
			break;
		case "0-0-0":
			move = new Move(colour, Castling.QueenSide);
			break;
		default:
			if (s.length() >= 5) {
				Figure figure = Figure.parse(s.substring(0, 1));
				if (figure == null) {
					figure = Figure.Pawn;
				}
				int i = figure.equals(Figure.Pawn) ? 0 : 1;
				Coordinate from = Coordinate.parse(s.substring(i, i + 2));
				boolean isCapture = s.charAt(i + 2) == 'x';
				Coordinate to = Coordinate.parse(s.substring(i + 3, i + 5));
				boolean enPassant = s.endsWith("e.p.");
				if (from == null || to == null) {
					throw new IllegalMoveFormatException(s);
				}
				Figure newFigure = null;
				if (figure.equals(Figure.Pawn) && !enPassant
						&& ((colour.equals(Colour.White) && to.getRow() == 7)
								|| (colour.equals(Colour.Black) && to.getRow() == 0))
						&& s.length() > 6 && s.contains("=")) {
					newFigure = Figure.parse(s.substring(i + 6));
				}
				Capture capture = isCapture ? (enPassant ? Capture.EnPassant : Capture.Regular) : Capture.None;
				if (newFigure == null) {
					move = new Move(colour, figure, from, to, capture);
				} else {
					move = new Move(colour, figure, from, to, capture, newFigure);
				}
			} else {
				throw new IllegalMoveFormatException(s);
			}
		}
		return move;
	}

	public static String toString(List<Move> moves, boolean pgn) {
		return toString(moves, 1, pgn);
	}

	public static String toString(List<Move> moves, int firstMove, boolean pgn) {
		StringBuilder sb = new StringBuilder();
		int m = firstMove;
		for (Move move : moves) {
			if (m % 2 == 1) {
				sb.append(Integer.toString((m + 1) / 2)).append(". ");
			}
			sb.append(move.toString(pgn)).append(m % 12 == 0 ? "\n" : " ");
			++m;
		}
		return sb.toString();
	}

}
