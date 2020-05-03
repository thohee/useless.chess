package de.thohee.useless.chess.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Coordinate;
import de.thohee.useless.chess.board.Figure;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;
import de.thohee.useless.chess.board.Piece;
import de.thohee.useless.chess.board.PositionedPiece;

public class ReadyPlayer1 extends MinimaxPlayer {

	private String firstMoveAsWhite = null;
	private boolean openings = false;

	public ReadyPlayer1(Colour colour) {
		super(colour, true);
	}

	public ReadyPlayer1(Colour colour, Boolean useTranspositionTable) {
		super(colour, useTranspositionTable);
	}

	public ReadyPlayer1(Colour colour, Boolean useTranspositionTable, String firstMoveAsWhite) {
		super(colour, useTranspositionTable);
		this.firstMoveAsWhite = firstMoveAsWhite;
	}

	void noOpenings() {
		this.openings = false;
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
			result.add(evaluateOpeningMidgameTacticsAndEndgame(boardPosition));
//			result.add(evaluatePawnByPawnProtections(boardPosition));
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

	static int getValue(Figure figure) {
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

	int evaluateThreatsAndProtections(BoardPosition boardPosition) {
		// test if exactly the same object (==) has already been evaluated
		if (boardPosition == this.lastThreatAnalysisBoardPosition) {
			// computed during terminal test
			return this.lastThreatAnalysisValue;
		}
		int[] valueLosses = new int[2];
		valueLosses[0] = 0;
		valueLosses[1] = 0;
		Iterator<PositionedPiece> iterator = boardPosition.getPositionedPieces();
		while (iterator.hasNext()) {
			PositionedPiece coordinateAndPiece = iterator.next();
			Piece piece = coordinateAndPiece.getPiece();
			Coordinate coordinate = coordinateAndPiece.getCoordinate();
			int pieceValue = getValue(piece.getFigure());
			Set<Piece> threats = boardPosition.getThreatsTo(piece.getColour(), coordinate);
			if (!threats.isEmpty()) {
				// if there are no protections then the piece value is lost
				int valueLoss = pieceValue;
				Set<Piece> protections = boardPosition.getProtections(coordinate);
				if (!protections.isEmpty()) {
					ArrayList<Integer> threatValues = threats.stream().map(p -> getValue(p.getFigure()))
							.collect(Collectors.toCollection(ArrayList::new));
					threatValues.sort(Integer::compare);
					ArrayList<Integer> protectionValues = protections.stream().map(p -> getValue(p.getFigure()))
							.collect(Collectors.toCollection(ArrayList::new));
					protectionValues.sort(Integer::compare);
					for (int t = 0; t < threatValues.size(); ++t) {
						if (t < protectionValues.size()) {
							// attacking piece gets beaten
							valueLoss -= threatValues.get(t);
							if (t + 1 < threatValues.size()) {
								// but also protector is attacked
								valueLoss += protectionValues.get(t);
							}
						} else {
							break;
						}
					}
					// if attacked color would actually gain value, we do not count this threat
					valueLoss = Math.max(0, valueLoss);
				}
				valueLosses[piece.getColour().ordinal()] += valueLoss;
			}
		}
		int threatsValue = valueLosses[getColour().opposite().ordinal()] - valueLosses[getColour().ordinal()];
		this.lastThreatAnalysisValue = threatsValue;
		this.lastThreatAnalysisBoardPosition = boardPosition;
		return threatsValue;
	}

	private Integer evaluateOpeningMidgameTacticsAndEndgame(BoardPosition boardPosition) {
		// TODO
		return 0;
	}

	@SuppressWarnings("unused")
	private Integer evaluatePawnByPawnProtections(BoardPosition boardPosition) {
		int pawnStructureValue = 0;
		Iterator<PositionedPiece> iterator = boardPosition.getPositionedPieces();
		while (iterator.hasNext()) {
			PositionedPiece positionedPiece = iterator.next();
			Coordinate coordinate = positionedPiece.getCoordinate();
			Piece piece = positionedPiece.getPiece();
			if (piece.getFigure().equals(Figure.Pawn) && piece.getColour().equals(this.colour)) {
				int row = coordinate.getRow();
				if (piece.getColour().equals(Colour.Black)) {
					row = 7 - row;
				}
				if (row > 1) {
					int protectionsByOtherPawns = (int) boardPosition.getProtections(coordinate).stream()
							.filter(p -> p.getFigure().equals(Figure.Pawn)).count();
					pawnStructureValue += protectionsByOtherPawns;
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
		if (openings && boardPosition.getDepth() <= 1 && boardPosition.getColourToMove().equals(getColour())) {
			return playOpening(boardPosition);
		}
		// we do not exclude any moves but prioritize them
		// looking at capture moves with high figure value first seems to improve the
		// effect of alpha-beta-pruning
		ArrayList<Move> sortedMoves = new ArrayList<>(boardPosition.getAllPossibleMoves());
		Collections.sort(sortedMoves, new MoveComparator(boardPosition));
		return sortedMoves;
	}

	private List<Move> playOpening(BoardPosition boardPosition) {
		assert (boardPosition.getDepth() <= 1 && boardPosition.getColourToMove().equals(getColour()));
		List<Move> moves = new ArrayList<>();
		if (boardPosition.getDepth() == 0) {
			assert (boardPosition.getColourToMove().equals(Colour.White));
			Move firstMove = null;
			if (firstMoveAsWhite != null) {
				try {
					firstMove = boardPosition.parseUciMove(firstMoveAsWhite);
				} catch (Exception e) {
				}
				if (firstMove == null) {
					try {
						firstMove = boardPosition.guessMove(firstMoveAsWhite);
					} catch (IllegalMoveFormatException e) {
					}
				}
			}
			if (firstMove == null) {
				Random random = new Random();
				int i = random.nextInt(100);
				if (i >= 55) {
					// e2e4 with 45% chance
					firstMove = new Move(Colour.White, Figure.Pawn, Coordinate.e2, Coordinate.e4, Capture.None);
				} else if (i >= 20) {
					// d2d4 with 35% chance
					firstMove = new Move(Colour.White, Figure.Pawn, Coordinate.d2, Coordinate.d4, Capture.None);
				} else {
					// c2c4 with 20% chance
					firstMove = new Move(Colour.White, Figure.Pawn, Coordinate.c2, Coordinate.c4, Capture.None);
				}
			}
			assert (firstMove != null);
			moves.add(firstMove);
		} else {
			Move lastMove = boardPosition.getLastMove();
			if (lastMove.getFigure().equals(Figure.Pawn) && lastMove.getTo().getRow() == 3) {
				int column = lastMove.getTo().getColumn();
				// immediately block opponent's pawn
				moves.add(new Move(Colour.Black, Figure.Pawn, Coordinate.get(column, 6), Coordinate.get(column, 4),
						Capture.None));
			} else {
				// evaluate several options
				moves.add(new Move(Colour.Black, Figure.Pawn, Coordinate.e7, Coordinate.e5, Capture.None));
				moves.add(new Move(Colour.Black, Figure.Pawn, Coordinate.d7, Coordinate.d5, Capture.None));
				moves.add(new Move(Colour.Black, Figure.Pawn, Coordinate.c7, Coordinate.c5, Capture.None));
				moves.add(new Move(Colour.Black, Figure.Knight, Coordinate.b8, Coordinate.c6, Capture.None));
				moves.add(new Move(Colour.Black, Figure.Knight, Coordinate.g8, Coordinate.f6, Capture.None));
			}
		}
		assert (!moves.isEmpty());
		return moves;
	}

	@Override
	protected Integer getCutoffDepth() {
		// [4..6]
		return getMaxDepth() != null ? Math.max(Math.min(4, getMaxDepth()), Math.min(6, getMaxDepth() * 6 / 10)) : null;
	}

}
