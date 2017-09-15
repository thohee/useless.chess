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
}
