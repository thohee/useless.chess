package de.thohee.useless.chess.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Coordinate;
import de.thohee.useless.chess.board.Figure;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Piece;
import de.thohee.useless.chess.board.PositionedPiece;

public class ReadyPlayer1 extends MinimaxPlayer {

	public ReadyPlayer1(Colour colour, Boolean useTranspositionTable) {
		super(colour, useTranspositionTable);
	}

	private BoardPosition lastThreatAnalysisBoardPosition = null;
	private int lastThreatAnalysisValue = 0;

	@Override
	protected Value evaluate(BoardPosition boardPosition) {
		ValueVector result = null;
		if (boardPosition.isCheckmate()) {
			result = boardPosition.getColourToMove().equals(this.colour) ? ValueVector.MINIMUM : ValueVector.MAXIMUM;
		} else {
			result = new ValueVector();
			result.add(evaluateDraw(boardPosition));
			result.add(evaluateMaterial(boardPosition));
			result.add(evaluateThreatsAndProtections(boardPosition));
			result.add(evaluateKingMobility(boardPosition));
			result.add(evaluatePawnStructure(boardPosition));
		}

		List<Move> moves = boardPosition.getPerformedMoves();
		int n = moves.size() - 1;
		if (n > 0) {
			writeLine(moves.get(n - 1).asUciMove() + " " + moves.get(n).asUciMove() + " " + result.toString());
		}

		return result;
	}

	@Override
	protected boolean isQuiescent(BoardPosition boardPosition) {
		return !boardPosition.getAllPossibleMoves().stream().anyMatch(m -> m.getNewFigure() != null)
				&& evaluateThreatsAndProtections(boardPosition) == 0;
	}

	private int getValue(Figure figure) {
		int value = 0;
		switch (figure) {
		case Pawn:
			value = 1;
			break;
		case Knight:
		case Bishop:
			value = 3;
			break;
		case Rook:
			value = 5;
			break;
		case Queen:
			value = 9;
			break;
		default:
			value = 0;
			break;
		}
		return value;
	}

	private int evaluateDraw(BoardPosition boardPosition) {
		return boardPosition.isDraw() ? -3 : (-1 * boardPosition.getRepetitions());
	}

	private int evaluateMaterial(BoardPosition boardPosition) {
		int ownValues = 0;
		int otherValues = 0;
		Iterator<Piece> iterator = boardPosition.getPieces();
		while (iterator.hasNext()) {
			Piece piece = iterator.next();
			int value = getValue(piece.getFigure());
			if (piece.getColour().equals(this.colour)) {
				ownValues += value;
			} else {
				otherValues += value;
			}
		}
		return ownValues - otherValues;
	}

	private int evaluateThreatsAndProtections(BoardPosition boardPosition) {
		if (boardPosition == this.lastThreatAnalysisBoardPosition) {
			return this.lastThreatAnalysisValue;
		}
		boolean ourTurn = boardPosition.getColourToMove().equals(this.colour);
		Colour opponentsColour = this.colour.opposite();
		int threatsValue = 0;
		Iterator<PositionedPiece> iterator = boardPosition.getPositionedPieces();
		while (iterator.hasNext()) {
			PositionedPiece coordinateAndPiece = iterator.next();
			Piece piece = coordinateAndPiece.getPiece();
			Colour pieceColour = piece.getColour();
			if ((ourTurn && pieceColour.equals(opponentsColour)) || (!ourTurn && pieceColour.equals(this.colour))) {
				Coordinate coordinate = coordinateAndPiece.getCoordinate();
				int pieceValue = getValue(piece.getFigure());
				Set<Piece> threats = boardPosition.getThreatsTo(pieceColour, coordinate);
				if (!threats.isEmpty()) {
					int valueDifference = threats.stream().map(p -> Math.max(0, pieceValue - getValue(p.getFigure())))
							.max(Integer::compareTo).orElse(0);
					if (valueDifference == 0 && boardPosition.getProtections(coordinate).isEmpty()) {
						valueDifference = pieceValue;
					}
					if (ourTurn) {
						threatsValue += valueDifference;
					} else {
						threatsValue -= valueDifference;
					}
				}
			}
		}
		this.lastThreatAnalysisValue = threatsValue;
		this.lastThreatAnalysisBoardPosition = boardPosition;
		return threatsValue;
	}

	private Integer evaluateKingMobility(BoardPosition boardPosition) {
		Set<Piece> ownCastlingPieces = boardPosition.getCastlingPieces().stream()
				.filter(p -> p.getColour().equals(this.colour)).collect(Collectors.toSet());
		if (!ownCastlingPieces.isEmpty()
				&& !ownCastlingPieces.stream().anyMatch(p -> p.getFigure().equals(Figure.King))) {
			// do not move the king as long as castling is still possible
			return -1;
		} else {
			return 0;
		}
	}

	private Integer evaluatePawnStructure(BoardPosition boardPosition) {
		int pawnStructureValue = 0;
		Iterator<PositionedPiece> iterator = boardPosition.getPositionedPieces();
		while (iterator.hasNext()) {
			PositionedPiece positionedPiece = iterator.next();
			Coordinate coordinate = positionedPiece.getCoordinate();
			Piece piece = positionedPiece.getPiece();
			if (piece.getFigure().equals(Figure.Pawn) && piece.getColour().equals(this.colour)) {
				int c = coordinate.getColumn();
				int columnFactor = c <= 3 ? c : 7 - c;
				int row = coordinate.getRow();
				if (piece.getColour().equals(Colour.Black)) {
					row = 7 - row;
				}
				pawnStructureValue += row * columnFactor;
				if (row > 1) {
					int protectionsByOtherPawns = (int) boardPosition.getProtections(coordinate).stream()
							.filter(p -> p.getFigure().equals(Figure.Pawn)).count();
					pawnStructureValue += protectionsByOtherPawns * columnFactor;
				}
			}
		}
		return pawnStructureValue;
	}

	@Override
	protected Value getInvalid() {
		return ValueVector.INVALID;
	}

	@Override
	protected Value getMin() {
		return ValueVector.MINIMUM;
	}

	@Override
	protected Value getMax() {
		return ValueVector.MAXIMUM;
	}

	private class MoveComparator implements Comparator<Move> {

		private BoardPosition boardPosition;

		public MoveComparator(BoardPosition boardPosition) {
			this.boardPosition = boardPosition;
		}

		@Override
		public int compare(Move m1, Move m2) {
			int c1 = m1.getCapture() == null || m1.getCapture().equals(Capture.None) ? 0 : 1;
			int c2 = m2.getCapture() == null || m2.getCapture().equals(Capture.None) ? 0 : 1;
			// capture move is better, i.e. smaller
			int compare = c2 - c1;
			if (compare == 0 && c1 == 1) {
				// both capture
				int v1 = m1.getCapture().equals(Capture.EnPassant) ? 1
						: getValue(boardPosition.get(m1.getTo()).getFigure());
				int v2 = m2.getCapture().equals(Capture.EnPassant) ? 1
						: getValue(boardPosition.get(m2.getTo()).getFigure());
				// higher value of captured piece is better, i.e. smaller
				compare = v2 - v1;
			}
			return compare;
		}

	}

	@Override
	protected List<Move> getPossibleMoves(BoardPosition boardPosition) {
		// we do not exclude any moves but prioritize them
		// looking at capture moves with high figure value first seems to improve the
		// effect of alpha-beta-pruning
		ArrayList<Move> sortedMoves = new ArrayList<>(boardPosition.getAllPossibleMoves());
		Collections.sort(sortedMoves, new MoveComparator(boardPosition));
		return sortedMoves;
	}

}