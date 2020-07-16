package de.thohee.useless.chess.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	private boolean openings = true;

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

	Integer evaluateOpeningMidgameTacticsAndEndgame(BoardPosition boardPosition) {
		return evaluateOpening(boardPosition) + evaluateEndGame(boardPosition);
	}

	int getCastlingOptions(BoardPosition boardPosition, Colour colour) {
		boolean king = false;
		int numberOfRooks = 0;
		for (Piece piece : boardPosition.getCastlingPieces()) {
			if (piece.getColour().equals(colour)) {
				if (piece.getFigure().equals(Figure.King)) {
					king = true;
				} else {
					++numberOfRooks;
				}
			}
		}
		if (!king) {
			return 0;
		} else {
			return numberOfRooks;
		}
	}

	private int whiteOrBlackRow(int whiteRow) {
		if (getColour().equals(Colour.Black)) {
			return 7 - whiteRow;
		} else {
			return whiteRow;
		}
	}

	private boolean hasOwnFigure(BoardPosition boardPosition, Coordinate coordinate, Figure figure) {
		Piece piece = boardPosition.get(coordinate);
		return piece != null && piece.getFigure().equals(figure) && piece.getColour().equals(getColour());
	}

	private boolean rooksAreConnected(BoardPosition boardPosition) {
		if (boardPosition.hasCastled(getColour())) {
			int backRank = whiteOrBlackRow(0);
			for (int c = 0; c < 8; ++c) {
				Piece piece = boardPosition.get(Coordinate.get(c, backRank));
				if (piece != null && (!piece.getColour().equals(getColour())
						|| (!piece.getFigure().equals(Figure.Rook) && !piece.getFigure().equals(Figure.King)))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private static final int openingPlies = 30;

	private boolean evaluateOpenings = true;

	void setEvaluateOpenings(boolean evaluateOpenings) {
		this.evaluateOpenings = evaluateOpenings;
	}

	Integer evaluateOpening(BoardPosition boardPosition) {
		if (this.evaluateOpenings && boardPosition.getDepth() <= openingPlies) {
			final Colour ownColour = getColour();
			int value = 0;

			boolean rooksAreConnected = rooksAreConnected(boardPosition);
			if (rooksAreConnected) {
				return value;
			} else {
				value -= openingPlies - boardPosition.getDepth();
			}

			// 1. occupy center
			int centerRow = whiteOrBlackRow(3);
			if (boardPosition.getDepth() >= 4 && !hasOwnFigure(boardPosition, Coordinate.get(3, centerRow), Figure.Pawn)
					&& !hasOwnFigure(boardPosition, Coordinate.get(4, centerRow), Figure.Pawn)) {
				value -= 30;
			}

			// 2. develop minor pieces
			int backRank = whiteOrBlackRow(0);
			int undevelopedMinorPieces = 0;
			if (boardPosition.getDepth() >= 4) {
				if (hasOwnFigure(boardPosition, Coordinate.get(1, backRank), Figure.Knight)) {
					++undevelopedMinorPieces;
				}
				if (hasOwnFigure(boardPosition, Coordinate.get(2, backRank), Figure.Bishop)) {
					++undevelopedMinorPieces;
				}
				if (hasOwnFigure(boardPosition, Coordinate.get(5, backRank), Figure.Bishop)) {
					++undevelopedMinorPieces;
				}
				if (hasOwnFigure(boardPosition, Coordinate.get(6, backRank), Figure.Knight)) {
					++undevelopedMinorPieces;
				}
				value -= 2 * undevelopedMinorPieces;
			}

			// 3. perform castling
			if (!boardPosition.hasCastled(ownColour)) {
				int castlingOptions = getCastlingOptions(boardPosition, ownColour);
				if (castlingOptions == 0) {
					// we already destroyed the option to castle
					value -= 30;
				} else if (castlingOptions == 1) {
					value -= 15;
				} else if (boardPosition.getDepth() >= 8) {
					// do not castle too late
					value -= boardPosition.getDepth() - 8;
				}
			}

			// 4. control center
			int threatsAndProtectionsInCenter = 0;
			for (int r = 3; r <= 4; ++r) {
				for (int c = 3; c <= 4; ++c) {
					Coordinate coordinate = Coordinate.get(c, r);
					threatsAndProtectionsInCenter += boardPosition.getThreatsTo(ownColour.opposite(), coordinate)
							.size();
					Piece piece = boardPosition.get(coordinate);
					if (piece != null && piece.getColour().equals(ownColour)) {
						threatsAndProtectionsInCenter += boardPosition.getProtections(coordinate).size();
					}
				}
			}
			value += threatsAndProtectionsInCenter;
			// knight on the rim is dim
			int beforePawnsRow = whiteOrBlackRow(2);
			if (hasOwnFigure(boardPosition, Coordinate.get(0, beforePawnsRow), Figure.Knight)) {
				value -= 5;
			}
			if (hasOwnFigure(boardPosition, Coordinate.get(7, beforePawnsRow), Figure.Knight)) {
				value -= 5;
			}

			// 5. do not move any piece twice
			if (boardPosition.getDepth() <= 12) {
				int repeatedMoves = 0;
				Set<Piece> movedPieces = new HashSet<>();
				BoardPosition bp = boardPosition;
				while (bp != null) {
					Move lastMove = bp.getLastMove();
					if (lastMove != null && lastMove.getColour().equals(ownColour) && lastMove.getCastling() == null) {
						Piece piece = bp.get(lastMove.getTo());
						if (movedPieces.contains(piece)) {
							++repeatedMoves;
						}
						movedPieces.add(piece);
					}
					bp = bp.getPredecessor();
				}
				value -= repeatedMoves;
			}

			// 6. do not develop the queen too early
			// implicitly achieved by keeping the castling options and forcing to develop
			// the minor pieces
			if (undevelopedMinorPieces > 0 && boardPosition.getDepth() <= 14) {
				Piece queen = boardPosition.get(Coordinate.get(3, backRank));
				if (queen == null) {
					value -= 14 - boardPosition.getDepth();
				}
			}

			// 7. react to opponent's threats (covered by evaluateThreatsAndProtections)

			return value;
		} else {
			return 0;
		}
	}

	private Integer evaluateEndGame(BoardPosition boardPosition) {
		final Colour opponentsColour = getColour().opposite();
		PositionedPiece opponentsSingleKing = null;
		Iterator<PositionedPiece> positionedPieces = boardPosition.getPositionedPieces();
		while (positionedPieces.hasNext()) {
			PositionedPiece positionedPiece = positionedPieces.next();
			if (positionedPiece.getPiece().getColour().equals(opponentsColour)) {
				if (positionedPiece.getPiece().getFigure().equals(Figure.King)) {
					opponentsSingleKing = positionedPiece;
				} else {
					opponentsSingleKing = null;
					break;
				}
			}
		}
		if (opponentsSingleKing != null) {
			return -1 * kingsReach(boardPosition, opponentsSingleKing, null);
		}
		return 0;
	}

	static int kingsReach(BoardPosition boardPosition, PositionedPiece positionedKing,
			Map<Coordinate, Integer> debugMap) {
		assert (positionedKing.getPiece().getFigure().equals(Figure.King));
		final Colour kingsColour = positionedKing.getPiece().getColour();
		final Coordinate startPosition = positionedKing.getCoordinate();
		Set<Coordinate> reachablePositions = new HashSet<>();
		Set<Coordinate> lastRing = new HashSet<>();
		for (int c : Arrays.asList(-1, 0, 1)) {
			for (int r : Arrays.asList(-1, 0, 1)) {
				if (c != 0 || r != 0) {
					int column = startPosition.getColumn() + c;
					int row = startPosition.getRow() + r;
					if (kingOfColourCanMoveTo(boardPosition, kingsColour, column, row)) {
						Coordinate coordinate = Coordinate.get(column, row);
						lastRing.add(coordinate);
						if (debugMap != null) {
							debugMap.put(coordinate, 1);
						}
					}
				}
			}
		}
		reachablePositions.addAll(lastRing);
		for (int radius = 2; radius <= 7; ++radius) {
			Set<Coordinate> nextRing = new HashSet<>();
			for (Coordinate previousPosition : lastRing) {
				int dr = previousPosition.getRow() - startPosition.getRow();
				{
					int nextRow = previousPosition.getRow() + (dr > 0 ? 1 : -1);
					for (int c : Arrays.asList(-1, 0, 1)) {
						int nextColumn = previousPosition.getColumn() + c;
						if (kingOfColourCanMoveTo(boardPosition, kingsColour, nextColumn, nextRow)) {
							nextRing.add(Coordinate.get(nextColumn, nextRow));
						}
					}
					for (int c : Arrays.asList(-1, 1)) {
						int nextColumn = previousPosition.getColumn() + c;
						if (0 <= nextColumn && nextColumn < 8) {
							Coordinate coordinate = Coordinate.get(nextColumn, previousPosition.getRow());
							if (!lastRing.contains(coordinate) && kingOfColourCanMoveTo(boardPosition, kingsColour,
									nextColumn, previousPosition.getRow())) {
								nextRing.add(coordinate);
							}
						}
					}
				}
				int dc = previousPosition.getColumn() - startPosition.getColumn();
				{
					int nextColumn = previousPosition.getColumn() + (dc > 0 ? 1 : -1);
					for (int r : Arrays.asList(-1, 0, 1)) {
						int nextRow = previousPosition.getRow() + r;
						if (kingOfColourCanMoveTo(boardPosition, kingsColour, nextColumn, nextRow)) {
							nextRing.add(Coordinate.get(nextColumn, nextRow));
						}
					}
					for (int r : Arrays.asList(-1, 1)) {
						int nextRow = previousPosition.getRow() + r;
						if (0 <= nextRow && nextRow < 8) {
							Coordinate coordinate = Coordinate.get(previousPosition.getColumn(), nextRow);
							if (!lastRing.contains(coordinate) && kingOfColourCanMoveTo(boardPosition, kingsColour,
									previousPosition.getColumn(), nextRow)) {
								nextRing.add(coordinate);
							}
						}
					}
				}
			}
			nextRing.removeAll(reachablePositions);
			if (nextRing.isEmpty()) {
				break;
			} else if (debugMap != null) {
				for (Coordinate c : nextRing) {
					assert (!debugMap.containsKey(c));
					debugMap.put(c, radius);
				}
			}
			reachablePositions.addAll(nextRing);
			lastRing = nextRing;
		}
		return reachablePositions.size();
	}

	private static boolean kingOfColourCanMoveTo(BoardPosition boardPosition, Colour colour, int column, int row) {
		if (column < 0 || column > 7 || row < 0 || row > 7) {
			return false;
		} else {
			Coordinate to = Coordinate.get(column, row);
			Piece piece = boardPosition.get(to);
			if (piece == null) {
				return boardPosition.getThreatsTo(colour, to).isEmpty();
			} else if (piece.getColour().equals(colour)) {
				return false;
			} else {
				return boardPosition.getProtections(to).isEmpty();
			}
		}
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
	protected List<GameState> getSuccessors(GameState gameState) {
		BoardPosition boardPosition = gameState.getBoardPosition();
		List<Move> moves = null;
		if (openings && boardPosition.getDepth() <= 1 && boardPosition.getColourToMove().equals(getColour())) {
			moves = playOpening(boardPosition);
		} else {
			moves = new ArrayList<>(boardPosition.getAllPossibleMoves());
			if (gameState.getDepth() == 0 && boardPosition.getDepth() <= openingPlies) {
				// We evaluate all possible first moves directly to prioritize them.
				// This way we may avoid useless intermediate moves which would eventually lead
				// to the same costs, because alpha-beta-pruning will prune them instead of the
				// direct move.
				ArrayList<GameState> evaluatedSuccessors = new ArrayList<>(moves.size());
				for (Move move : moves) {
					GameState successor = gameState.createSuccessorState(move);
					successor.setValue(evaluate(successor.getBoardPosition()));
					evaluatedSuccessors.add(successor);
				}
				Collections.sort(evaluatedSuccessors, new GameStateComparator());
				return evaluatedSuccessors;
			} else {
				// we prioritize the moves with a cheaper heuristic:
				// looking at capture moves with high figure value first seems to improve the
				// effect of alpha-beta-pruning
				Collections.sort(moves, new MoveComparator(boardPosition));
			}
		}
		assert (moves != null);
		ArrayList<GameState> successors = new ArrayList<>(moves.size());
		for (Move move : moves) {
			successors.add(gameState.createSuccessorState(move));
		}
		return successors;
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
					writeLine(e.getMessage());
				}
				if (firstMove == null) {
					try {
						firstMove = boardPosition.guessMove(firstMoveAsWhite);
					} catch (IllegalMoveFormatException e) {
						writeLine(e.getMessage());
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
