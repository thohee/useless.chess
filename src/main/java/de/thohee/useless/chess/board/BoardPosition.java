package de.thohee.useless.chess.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Move.Castling;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;

public class BoardPosition {

	public class RepeatablePosition {
		private final ChessBoard board;
		private final Colour colourToMove;
		private final boolean canCaptureEnPassant;
		private final Set<Piece> castlingPieces;

		public RepeatablePosition(ChessBoard board, Colour colourToMove, boolean canCaptureEnPassant,
				Set<Piece> castlingPieces) {
			this.board = board;
			this.colourToMove = colourToMove;
			this.canCaptureEnPassant = canCaptureEnPassant;
			this.castlingPieces = new HashSet<>(castlingPieces);
		}

		@Override
		public int hashCode() {
			return Objects.hash(colourToMove, canCaptureEnPassant, board, castlingPieces);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RepeatablePosition other = (RepeatablePosition) obj;
			return colourToMove == other.colourToMove && canCaptureEnPassant == other.canCaptureEnPassant
					&& Objects.equals(castlingPieces, other.castlingPieces) && Objects.equals(board, other.board);
		}

	}

	public class Key {
		private final RepeatablePosition position;
		private final int depth;

		public Key(RepeatablePosition position, int depth) {
			super();
			this.position = position;
			this.depth = depth;
		}

		@Override
		public int hashCode() {
			return depth + 1000 * position.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			return other.depth == this.depth && Objects.equals(other.position, this.position);
		}
	}

	public class Predecessor {
		private final Predecessor predecessor;
		private final RepeatablePosition position;
		private Integer repetitions = null;
		private Integer maxRepetitions = null;

		public Predecessor(Predecessor predecessor, RepeatablePosition position) {
			this.predecessor = predecessor;
			this.position = position;
		}

		public int getRepetitions() {
			if (repetitions == null) {
				repetitions = 0;
				Predecessor ancestor = predecessor;
				while (ancestor != null) {
					if (ancestor.position.equals(this.position)) {
						repetitions = ancestor.getRepetitions() + 1;
						break;
					}
					ancestor = ancestor.predecessor;
				}
			}
			return repetitions;
		}

		public int getMaxRepetitions() {
			if (maxRepetitions == null) {
				int maxRepetitionsPredecessor = predecessor != null ? predecessor.getMaxRepetitions() : 0;
				maxRepetitions = Math.max(maxRepetitionsPredecessor, getRepetitions());
			}
			return maxRepetitions;
		}
	}

	private RepeatablePosition toRepeatablePosition() {
		return new RepeatablePosition(this.board, this.colourToMove, this.canCaptureEnPassant(), this.castlingPieces);
	}

	public Key getKey() {
		return new Key(toRepeatablePosition(), this.depth);
	}

	private ChessBoard board = null;
	private Set<Piece> castlingPieces = null;
	private boolean[] hasCastled = new boolean[] { false, false };
	private List<Move> performedMoves = null;
	private Predecessor predecessor = null;
	private Predecessor self = null;
	private Integer numberOfRepetitions = null;
	private Colour colourToMove = null;
	private int movesWithoutPawnAndCapture = 0;
	private int depth = 0;

	// cached computation results
	private List<Move> allPossibleMoves = null;
	private List<Move> possibleMoves = null;
	private Map<Colour, Map<Coordinate, Set<Piece>>> threatsTo = null;
	private Map<Coordinate, Set<Piece>> protections = null;
	private Map<Colour, Coordinate> kingPosition = new HashMap<>();
	private Boolean draw = null;

	BoardPosition() {
		board = new ChessBoard();
		castlingPieces = new HashSet<>();
		performedMoves = new ArrayList<>();
	}

	@Override
	public int hashCode() {
		return movesWithoutPawnAndCapture + 100 * board.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof BoardPosition) {
			BoardPosition other = (BoardPosition) obj;
			return this.depth == other.depth && this.movesWithoutPawnAndCapture == other.movesWithoutPawnAndCapture
					&& this.board.equals(other.board) && this.castlingPieces.equals(other.castlingPieces);
		} else {
			return false;
		}
	}

	public static BoardPosition getInitialPosition() {
		BoardPosition boardPosition = new BoardPosition();
		ChessBoard map = boardPosition.board;
		map.put(Coordinate.get(0, 0), new Piece(Colour.White, Figure.Rook));
		map.put(Coordinate.get(1, 0), new Piece(Colour.White, Figure.Knight));
		map.put(Coordinate.get(2, 0), new Piece(Colour.White, Figure.Bishop));
		map.put(Coordinate.get(3, 0), new Piece(Colour.White, Figure.Queen));
		map.put(Coordinate.get(4, 0), new Piece(Colour.White, Figure.King));
		map.put(Coordinate.get(5, 0), new Piece(Colour.White, Figure.Bishop));
		map.put(Coordinate.get(6, 0), new Piece(Colour.White, Figure.Knight));
		map.put(Coordinate.get(7, 0), new Piece(Colour.White, Figure.Rook));
		for (int c = 0; c <= 7; ++c) {
			map.put(Coordinate.get(c, 1), new Piece(Colour.White, Figure.Pawn));
		}
		map.put(Coordinate.get(0, 7), new Piece(Colour.Black, Figure.Rook));
		map.put(Coordinate.get(1, 7), new Piece(Colour.Black, Figure.Knight));
		map.put(Coordinate.get(2, 7), new Piece(Colour.Black, Figure.Bishop));
		map.put(Coordinate.get(3, 7), new Piece(Colour.Black, Figure.Queen));
		map.put(Coordinate.get(4, 7), new Piece(Colour.Black, Figure.King));
		map.put(Coordinate.get(5, 7), new Piece(Colour.Black, Figure.Bishop));
		map.put(Coordinate.get(6, 7), new Piece(Colour.Black, Figure.Knight));
		map.put(Coordinate.get(7, 7), new Piece(Colour.Black, Figure.Rook));
		for (int c = 0; c <= 7; ++c) {
			map.put(Coordinate.get(c, 6), new Piece(Colour.Black, Figure.Pawn));
		}
		boardPosition.castlingPieces.add(map.get(Coordinate.get(0, 0)));
		boardPosition.castlingPieces.add(map.get(Coordinate.get(4, 0)));
		boardPosition.castlingPieces.add(map.get(Coordinate.get(7, 0)));
		boardPosition.castlingPieces.add(map.get(Coordinate.get(0, 7)));
		boardPosition.castlingPieces.add(map.get(Coordinate.get(4, 7)));
		boardPosition.castlingPieces.add(map.get(Coordinate.get(7, 7)));
		return boardPosition;
	}

	BoardPosition(BoardPosition other) {
		board = new ChessBoard(other.board);
		castlingPieces = new HashSet<>(other.castlingPieces);
		hasCastled[0] = other.hasCastled[0];
		hasCastled[1] = other.hasCastled[1];
		performedMoves = new ArrayList<>(other.performedMoves);
	}

	public Predecessor getPredecessor() {
		return this.predecessor;
	}

	public int getDepth() {
		return depth;
	}

	void setDepth(int depth) {
		this.depth = depth;
	}

	public Piece get(Coordinate coordinate) {
		return board.get(coordinate);
	}

	public Piece get(int column, int row) {
		return get(Coordinate.get(column, row));
	}

	public Iterator<Piece> getPieces() {
		return board.pieces();
	}

	public int getNumberOfPieces() {
		return board.size();
	}

	public Iterator<PositionedPiece> getPositionedPieces() {
		return board.positionedPieces();
	}

	public Set<Piece> getCastlingPieces() {
		return castlingPieces;
	}

	public BoardPosition performMove(Move move) {
		assert (getPossibleMoves().contains(move)) : move;
		return justPerformMove(move);
	}

	public BoardPosition justPerformMove(Move move) {
		assert (move != null && move.getColour().equals(getColourToMove()));
		BoardPosition newBoardPosition = new BoardPosition(this);
		boolean moveWithoutPawnAndCapture = true;
		if (move.getCastling() == null) {
			movePiece(newBoardPosition, move.getFrom(), move.getTo(), move.getNewFigure());
			newBoardPosition.castlingPieces.remove(newBoardPosition.get(move.getTo()));
			moveWithoutPawnAndCapture = !move.getFigure().equals(Figure.Pawn) && move.getCapture().equals(Capture.None);
		} else {
			performCastling(newBoardPosition, move.getColour(), move.getCastling());
			newBoardPosition.castlingPieces.removeIf(p -> p.getColour().equals(move.getColour()));
		}
		newBoardPosition.performedMoves.add(move);
		newBoardPosition.depth = this.depth + 1;

		if (moveWithoutPawnAndCapture) {
			newBoardPosition.movesWithoutPawnAndCapture = this.movesWithoutPawnAndCapture + 1;
		} // else reset to default 0

		if (self == null) {
			self = new Predecessor(this.predecessor, toRepeatablePosition());
		}
		newBoardPosition.predecessor = self;

		return newBoardPosition;
	}

	private static void movePiece(BoardPosition boardPosition, Coordinate from, Coordinate to, Figure newFigure) {
		Piece piece = boardPosition.board.get(from);
		assert (piece != null);
		if (newFigure != null) {
			piece = new Piece(piece.getColour(), newFigure);
		}
		if (Figure.Pawn.equals(piece.getFigure()) && to.getColumn() != from.getColumn()
				&& boardPosition.board.get(to) == null) {
			// en passant
			int direction = piece.getColour().equals(Colour.White) ? 1 : -1;
			Coordinate captureTarget = makeCoordinate(to.getColumn(), to.getRow() - direction);
			assert (captureTarget != null && boardPosition.get(captureTarget) != null);
			boardPosition.board.remove(captureTarget);
		}
		boardPosition.board.remove(from);
		boardPosition.board.put(to, piece);
	}

	private static void performCastling(BoardPosition boardPosition, Colour colour, Castling castling) {
		switch (castling) {
		case QueenSide:
			switch (colour) {
			case White:
				// rook (0,0) -> (3,0); king (4,0) -> (2,0)
				movePiece(boardPosition, Coordinate.get(0, 0), Coordinate.get(3, 0), null);
				movePiece(boardPosition, Coordinate.get(4, 0), Coordinate.get(2, 0), null);
				break;
			case Black:
				// rook (0,7) -> (3,7); king (4,7) -> (2,7)
				movePiece(boardPosition, Coordinate.get(0, 7), Coordinate.get(3, 7), null);
				movePiece(boardPosition, Coordinate.get(4, 7), Coordinate.get(2, 7), null);
				break;
			}
			break;
		case KingSide:
			switch (colour) {
			case White:
				// rook (7,0) -> (5,0); king (4,0) -> (6,0)
				movePiece(boardPosition, Coordinate.get(7, 0), Coordinate.get(5, 0), null);
				movePiece(boardPosition, Coordinate.get(4, 0), Coordinate.get(6, 0), null);
				break;
			case Black:
				// rook (7,7) -> (5,7); king (4,7) -> (6,7)
				movePiece(boardPosition, Coordinate.get(7, 7), Coordinate.get(5, 7), null);
				movePiece(boardPosition, Coordinate.get(4, 7), Coordinate.get(6, 7), null);
				break;
			}
			break;
		}
		boardPosition.hasCastled[colour.ordinal()] = true;
	}

	public boolean hasCastled(Colour colour) {
		return hasCastled[colour.ordinal()];
	}

	private boolean allEmpty(int row, List<Integer> columns) {
		for (int c : columns) {
			if (get(c, row) != null) {
				return false;
			}
		}
		return true;
	}

	private boolean threatsToAny(Colour colour, int row, List<Integer> columns) {
		for (int c : columns) {
			if (!threatsTo(colour, Coordinate.get(c, row)).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	List<Move> getPossibleCastlings(Colour colour) {
		if (castlingPieces.isEmpty()) {
			return Collections.emptyList();
		}
		int row = colour.equals(Colour.White) ? 0 : 7;
		Piece king = get(4, row);
		Piece kingSideRook = get(7, row);
		Piece queenSideRook = get(0, row);
		List<Move> possibleCastlings = new LinkedList<>();
		if (king != null && king.getFigure().equals(Figure.King) && castlingPieces.contains(king)) {
			if (kingSideRook != null && kingSideRook.getFigure().equals(Figure.Rook)
					&& castlingPieces.contains(kingSideRook) && allEmpty(row, Arrays.asList(5, 6))
					&& !threatsToAny(colour, row, Arrays.asList(4, 5, 6))) {
				possibleCastlings.add(new Move(colour, Castling.KingSide));
			}
			if (queenSideRook != null && queenSideRook.getFigure().equals(Figure.Rook)
					&& castlingPieces.contains(queenSideRook) && allEmpty(row, Arrays.asList(1, 2, 3))
					&& !threatsToAny(colour, row, Arrays.asList(2, 3, 4))) {
				possibleCastlings.add(new Move(colour, Castling.QueenSide));
			}
		}
		return possibleCastlings;
	}

	private static Coordinate makeCoordinate(int column, int row) {
		if (0 <= column && column < 8 && 0 <= row && row < 8) {
			return Coordinate.get(column, row);
		} else {
			return null;
		}
	}

	private static class Direction {
		public final int dc;
		public final int dr;

		public Direction(int dc, int dr) {
			this.dc = dc;
			this.dr = dr;
		}
	}

	private static final int[] turns = { -1, 1 };

	private static final Direction[] rookDirections = { new Direction(-1, 0), new Direction(1, 0), new Direction(0, -1),
			new Direction(0, 1) };

	private static final Direction[] bishopDirections = { new Direction(-1, -1), new Direction(1, -1),
			new Direction(-1, 1), new Direction(1, 1) };

	private static final Figure[] promotionFigures = { Figure.Queen, Figure.Knight, Figure.Bishop, Figure.Rook };

	private void goAsFarAsPossible(final Piece piece, final Coordinate startPosition, final Direction[] directions,
			final int maxOffset, List<Move> moves) {
		final Colour colourToMove = piece.getColour();
		for (Direction d : directions) {
			for (int offset = 1; offset <= maxOffset; ++offset) {
				Coordinate target = makeCoordinate(startPosition.getColumn() + offset * d.dc,
						startPosition.getRow() + offset * d.dr);
				if (target == null) {
					break;
				}
				Piece p = get(target);
				if (p == null) {
					moves.add(new Move(colourToMove, piece.getFigure(), startPosition, target, Capture.None));
				} else if (!p.getColour().equals(colourToMove)) {
					moves.add(new Move(colourToMove, piece.getFigure(), startPosition, target, Capture.Regular));
				} else {
					protections.computeIfAbsent(target, c -> new HashSet<>()).add(piece);
				}
				if (p != null) {
					// either we are blocked by one of our own or we beat one
					break;
				}
			}
		}
	}

	private static void addPawnMoves(final Colour colour, final Coordinate position, final Coordinate target,
			final Capture capture, List<Move> moves) {
		if (target.getRow() == (colour.equals(Colour.White) ? 7 : 0)) {
			// promotion
			for (Figure figure : promotionFigures) {
				moves.add(new Move(colour, Figure.Pawn, position, target, capture, figure));
			}
		} else {
			moves.add(new Move(colour, Figure.Pawn, position, target, capture));
		}
	}

	private boolean pawnOfColour(int column, int row, Colour colour) {
		if (column < 0 || column > 7 || row < 0 || row > 7) {
			return false;
		}
		Piece piece = get(Coordinate.get(column, row));
		return piece != null && piece.getFigure() == Figure.Pawn && piece.getColour() == colour;
	}

	boolean canCaptureEnPassant() {
		Move lastMove = getLastMove();
		if (lastMove != null && lastMove.getFigure() == Figure.Pawn) {
			final boolean whiteMoved = lastMove.getColour() == Colour.White;
			final int sourceRow = whiteMoved ? 1 : 6;
			final int targetRow = whiteMoved ? 3 : 4;
			final int column = lastMove.getTo().getColumn();
			if (lastMove.getFrom().getRow() == sourceRow && lastMove.getTo().getRow() == targetRow) {
				final Colour colourToMove = getColourToMove();
				assert (whiteMoved == (colourToMove == Colour.Black));
				return pawnOfColour(column - 1, targetRow, colourToMove)
						|| pawnOfColour(column + 1, targetRow, colourToMove);
			}
		}
		return false;
	}

	private void computePossibleMoves(PositionedPiece positionedPiece, List<Move> moves) {
		Piece piece = positionedPiece.getPiece();
		Coordinate position = positionedPiece.getCoordinate();
		Colour colour = piece.getColour();
		switch (piece.getFigure()) {
		case Pawn: {
			int step = colour.equals(Colour.White) ? 1 : -1;
			Coordinate target = makeCoordinate(position.getColumn(), position.getRow() + step);
			if (target != null && get(target) == null) {
				addPawnMoves(colour, position, target, Capture.None, moves);
				if (position.getRow() == (colour.equals(Colour.White) ? 1 : 6)) {
					// initial 2-step move
					Coordinate twoStepTarget = makeCoordinate(position.getColumn(), position.getRow() + 2 * step);
					if (twoStepTarget != null && get(twoStepTarget) == null) {
						// no promotion possible
						moves.add(new Move(colour, Figure.Pawn, position, twoStepTarget, Capture.None));
					}
				}
			}
			for (int dc : turns) {
				Coordinate captureTarget = makeCoordinate(position.getColumn() + dc, position.getRow() + step);
				if (captureTarget != null) {
					final Move lastMove = getLastMove();
					Piece otherPiece = get(captureTarget);
					if (otherPiece != null) {
						if (otherPiece.getColour().equals(colour.opposite())) {
							addPawnMoves(colour, position, captureTarget, Capture.Regular, moves);
						} else {
							protections.computeIfAbsent(captureTarget, c -> new HashSet<>()).add(piece);
						}
					} else if (lastMove != null && !lastMove.getColour().equals(colour)
							&& Figure.Pawn.equals(lastMove.getFigure())
							&& lastMove.getFrom().getColumn() == captureTarget.getColumn()
							&& lastMove.getFrom().getRow() == captureTarget.getRow() + step
							&& lastMove.getTo().getColumn() == captureTarget.getColumn()
							&& lastMove.getTo().getRow() == captureTarget.getRow() - step) {
						// en passant
						// no promotion possible
						moves.add(new Move(colour, Figure.Pawn, position, captureTarget, Capture.EnPassant));
					} else {
						threatsTo.get(colour.opposite()).computeIfAbsent(captureTarget, c -> new HashSet<>())
								.add(piece);
					}
				}
			}
			break;
		}
		case Knight:
			for (Direction d : rookDirections) {
				for (int turn : turns) {
					Coordinate target = d.dc == 0
							? makeCoordinate(position.getColumn() + turn, position.getRow() + 2 * d.dr)
							: makeCoordinate(position.getColumn() + 2 * d.dc, position.getRow() + turn);
					if (target != null) {
						Piece p = get(target);
						if (p == null) {
							moves.add(new Move(colour, piece.getFigure(), position, target, Capture.None));
						} else if (!p.getColour().equals(colour)) {
							moves.add(new Move(colour, piece.getFigure(), position, target, Capture.Regular));
						} else {
							protections.computeIfAbsent(target, c -> new HashSet<>()).add(piece);
						}
					}
				}
			}
			break;
		case Bishop:
			goAsFarAsPossible(piece, position, bishopDirections, 7, moves);
			break;
		case Rook:
			goAsFarAsPossible(piece, position, rookDirections, 7, moves);
			break;
		case Queen:
			goAsFarAsPossible(piece, position, bishopDirections, 7, moves);
			goAsFarAsPossible(piece, position, rookDirections, 7, moves);
			break;
		case King: {
			List<Move> tmp = new LinkedList<>();
			goAsFarAsPossible(piece, position, bishopDirections, 1, tmp);
			goAsFarAsPossible(piece, position, rookDirections, 1, tmp);
			tmp = tmp.stream().filter(m -> threatsTo(colour, m.getTo()).isEmpty() && protections(m.getTo()).isEmpty())
					.collect(Collectors.toList());
			moves.addAll(tmp);
			kingPosition.put(colour, position);
			break;
		}
		}
	}

	public Set<Piece> getThreatsTo(Colour colour, Coordinate coordinate) {
		analyze0();
		return threatsTo(colour, coordinate);
	}

	private Set<Piece> threatsTo(Colour colour, Coordinate coordinate) {
		Set<Piece> threats = threatsTo.get(colour).get(coordinate);
		return threats != null ? threats : Collections.emptySet();
	}

	public Set<Piece> getProtections(Coordinate coordinate) {
		analyze0();
		return protections(coordinate);
	}

	private Set<Piece> protections(Coordinate coordinate) {
		Set<Piece> result = protections.get(coordinate);
		return result != null ? result : Collections.emptySet();
	}

	public boolean isCheck() {
		analyze0();
		return checkTo(getColourToMove());
	}

	private boolean checkTo(Colour colour) {
		return !threatsTo(colour, kingPosition.get(colour)).isEmpty();
	}

	public boolean isStillCheck() {
		analyze0();
		return checkTo(getColourToMove().opposite());
	}

	public boolean isCheckmate() {
		analyze();
		return isCheck() && possibleMoves.isEmpty();
	}

	// Patt
	boolean stalemate() {
		return possibleMoves.isEmpty() && !checkTo(getColourToMove());
	}

	// Remis
	public boolean isDraw() {
		analyze();
		return draw;
	}

	public boolean isThreeFoldRepetition() {
		return getNumberOfRepetitions() >= 2;
	}

	public int getNumberOfRepetitions() {
		if (numberOfRepetitions == null) {
			if (self == null) {
				self = new Predecessor(this.predecessor, toRepeatablePosition());
			}
			numberOfRepetitions = self.getRepetitions();
		}
		return numberOfRepetitions;
	}

	public int getMaxNumberOfRepetitions() {
		if (self == null) {
			self = new Predecessor(this.predecessor, toRepeatablePosition());
		}
		return self.getMaxRepetitions();
	}

	private boolean onlyKingsLeft(Iterator<Piece> iterator) {
		while (iterator.hasNext()) {
			if (!iterator.next().getFigure().equals(Figure.King)) {
				return false;
			}
		}
		return true;
	}

	public boolean isDrawDisregardingStalemateAndThreeFoldRepetition() {

		// 100 plies = 50 moves
		if (this.movesWithoutPawnAndCapture >= 100) {
			return true;
		}

		// only the two kings are left
		if (board.size() == 2 && onlyKingsLeft(board.pieces())) {
			return true;
		}

		return false;
	}

	private boolean draw() {

		if (isDrawDisregardingStalemateAndThreeFoldRepetition()) {
			return true;
		}

		// stalemate
		if (stalemate()) {
			return true;
		}

		return false;
	}

	public Colour getColourToMove() {
		final Move lastMove = getLastMove();
		return lastMove != null ? lastMove.getColour().opposite()
				: (colourToMove != null ? colourToMove : Colour.White);
	}

	void setColourToMove(Colour colour) {
		colourToMove = colour;
	}

	private void analyze0() {
		if (allPossibleMoves != null) {
			return;
		}
		threatsTo = new HashMap<>();
		threatsTo.put(Colour.White, new HashMap<>());
		threatsTo.put(Colour.Black, new HashMap<>());
		protections = new HashMap<>();
		Colour colourToMove = getColourToMove();
		for (Colour colour : Arrays.asList(colourToMove.opposite(), colourToMove)) {
			List<Move> moves = new LinkedList<>();
			Iterator<PositionedPiece> iterator = board.positionedPieces();
			while (iterator.hasNext()) {
				PositionedPiece entry = iterator.next();
				if (entry.getPiece().getColour().equals(colour)) {
					computePossibleMoves(entry, moves);
				}
			}
			Colour opponent = colour.opposite();
			for (Move move : moves) {
				if (!move.getFigure().equals(Figure.Pawn) || Capture.Regular.equals(move.getCapture())) {
					threatsTo.get(opponent).computeIfAbsent(move.getTo(), c -> new HashSet<>())
							.add(get(move.getFrom()));
				}
			}
			if (colour.equals(colourToMove)) {
				if (!checkTo(colour)) {
					moves.addAll(getPossibleCastlings(colourToMove));
				}
				allPossibleMoves = new ArrayList<>(moves);
			}
		}
	}

	void analyze() {
		analyze0();
		if (possibleMoves != null) {
			return;
		}
		// only those moves are allowed which do not leave the king in check
		possibleMoves = allPossibleMoves.stream().filter(m -> !justPerformMove(m).isStillCheck())
				.collect(Collectors.toList());
		this.draw = draw();
	}

	/**
	 * @return all allowed moves, which particularly excludes moves, after which
	 *         the king of the moving color is (still) in check. This requires
	 *         analyzing the resulting positions as well.
	 */
	public List<Move> getPossibleMoves() {
		analyze();
		if (isCheckmate() || isDraw()) {
			return Collections.emptyList();
		}
		return possibleMoves;
	}

	/**
	 * @return all possible moves including those after which the king of the
	 *         moving color is (still) in check, which is actually not allowed.
	 */
	public List<Move> getAllPossibleMoves() {
		analyze0();
		return allPossibleMoves;
	}

	public List<Move> getPerformedMoves() {
		return performedMoves;
	}

	public String showPerformedMoves() {
		return Move.toString(getPerformedMoves(), false);
	}

	public String getResult() {
		if (isDraw()) {
			return "1/2-1/2";
		} else if (isCheckmate()) {
			if (getColourToMove().equals(Colour.White)) {
				return "0-1";
			} else {
				return "1-0";
			}
		} else {
			return "?";
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int r = 7; r >= 0; --r) {
			for (int c = 0; c < 8; ++c) {
				if (c > 0) {
					sb.append(" ");
				}
				Piece p = get(c, r);
				if (p == null) {
					sb.append(".");
				} else {
					String f = p.getFigure().toString();
					sb.append(p.getColour().equals(Colour.Black) ? f.toLowerCase() : f);
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public Move getLastMove() {
		return performedMoves.isEmpty() ? null : performedMoves.get(performedMoves.size() - 1);
	}

	public Move getMove(Coordinate from, Coordinate to) {
		return getMove(from, to, null);
	}

	public Move getMove(Coordinate from, Coordinate to, Figure newFigure) {
		Piece piece = board.get(from);
		assert (piece != null);
		assert (piece.getColour().equals(getColourToMove()));
		final Move lastMove = getLastMove();
		Capture capture = board.get(to) != null ? Capture.Regular : Capture.None;
		if (piece.getFigure().equals(Figure.Pawn) && to.getRow() == (piece.getColour().equals(Colour.White) ? 5 : 2)
				&& board.get(to) == null && lastMove.getFigure().equals(Figure.Pawn)
				&& lastMove.getTo().getColumn() == to.getColumn()
				&& lastMove.getTo().getRow() == (to.getRow() + (piece.getColour().equals(Colour.White) ? -1 : 1))) {
			capture = Capture.EnPassant;
		}
		Move move = null;
		if (piece.getFigure().equals(Figure.King) && Arrays.asList(0, 7).contains(from.getRow())
				&& Math.abs(from.getColumn() - to.getColumn()) == 2) {
			assert (Arrays.asList(0, 7).contains(to.getRow()));
			assert (Arrays.asList(2, 6).contains(to.getColumn()));
			Castling castling = to.getColumn() == 6 ? Castling.KingSide : Castling.QueenSide;
			move = new Move(getColourToMove(), castling);
		} else {
			if (piece.getFigure().equals(Figure.Pawn) && Arrays.asList(0, 7).contains(to.getRow())) {
				assert (newFigure != null);
				move = new Move(getColourToMove(), piece.getFigure(), from, to, capture, newFigure);
			} else {
				move = new Move(getColourToMove(), piece.getFigure(), from, to, capture);
			}
		}
		assert (getPossibleMoves().contains(move));
		return move;
	}

	public Move guessMove(String input) throws IllegalMoveFormatException {
		Move move = Move.parse(getColourToMove(), input);
		if (!getPossibleMoves().contains(move)) {
			Move intendedMove = move;
			move = null;
			if (intendedMove.getCastling() == null) {
				Piece piece = get(intendedMove.getFrom());
				if (piece != null) {
					intendedMove = new Move(intendedMove.getColour(), piece.getFigure(), intendedMove.getFrom(),
							intendedMove.getTo(), intendedMove.getCapture());
					Piece targetPiece = get(intendedMove.getTo());
					if (targetPiece != null) {
						if (targetPiece.getColour().equals(piece.getColour().opposite())) {
							// capture?
							intendedMove = new Move(getColourToMove(), piece.getFigure(), intendedMove.getFrom(),
									intendedMove.getTo(), Move.Capture.Regular);
						} else {
							// castling indicated by selecting king and rook?
							if (piece.getFigure().equals(Figure.King) && targetPiece.getFigure().equals(Figure.Rook)) {
								Castling castling = intendedMove.getTo().getColumn() == 7 ? Castling.KingSide
										: Castling.QueenSide;
								intendedMove = new Move(intendedMove.getColour(), castling);
							} else if (piece.getFigure().equals(Figure.Rook)
									&& targetPiece.getFigure().equals(Figure.King)) {
								Castling castling = intendedMove.getFrom().getColumn() == 7 ? Castling.KingSide
										: Castling.QueenSide;
								intendedMove = new Move(intendedMove.getColour(), castling);
							}
						}
					} else if (piece.getFigure().equals(Figure.Pawn) && ((piece.getColour().equals(Colour.White)
							&& intendedMove.getTo().getRow() == 2)
							|| (piece.getColour().equals(Colour.Black) && intendedMove.getTo().getRow() == 5))) {
						// en passant?
						intendedMove = new Move(intendedMove.getColour(), intendedMove.getFigure(),
								intendedMove.getFrom(), intendedMove.getTo(), Capture.EnPassant);
					} else if (piece.getFigure().equals(Figure.King)) {
						// castling indicated by 2-step move of king?
						int dc = Math.abs(intendedMove.getTo().getColumn() - intendedMove.getFrom().getColumn());
						if (dc == 2) {
							int c = intendedMove.getTo().getColumn();
							if (c == 6) {
								intendedMove = new Move(intendedMove.getColour(), Castling.KingSide);
							} else if (c == 2) {
								intendedMove = new Move(intendedMove.getColour(), Castling.QueenSide);
							}
						}
					}
					if (piece.getFigure().equals(Figure.Pawn)) {
						if ((piece.getColour().equals(Colour.White) && intendedMove.getTo().getRow() == 7)
								|| (piece.getColour().equals(Colour.Black) && intendedMove.getTo().getRow() == 0)) {
							// promotion to knight would require player decision
							intendedMove = new Move(intendedMove.getColour(), intendedMove.getFigure(),
									intendedMove.getFrom(), intendedMove.getTo(), intendedMove.getCapture(),
									Figure.Queen);
						}
					}
				}
			}
			if (getPossibleMoves().contains(intendedMove)) {
				move = intendedMove;
			}
		}
		return move;
	}

	// unit test methods

	public static BoardPosition createPosition(Collection<PositionedPiece> positionedPieces, Colour colourToMove) {
		BoardPosition boardPosition = new BoardPosition();
		for (PositionedPiece pp : positionedPieces) {
			boardPosition.set(pp.getCoordinate(), pp.getPiece());
		}
		if (colourToMove.equals(Colour.Black)) {
			boardPosition.performedMoves.add(new Move(Colour.White, Castling.KingSide));
		}
		return boardPosition;
	}

	private void resetCachedValues() {
		this.allPossibleMoves = null;
		this.possibleMoves = null;
	}

	void set(Coordinate coordinate, Piece piece) {
		board.put(coordinate, piece);
		resetCachedValues();
	}

	void addCastlingPiece(Piece piece) {
		castlingPieces.add(piece);
		resetCachedValues();
	}

	void setLastMove(Move move) {
		this.performedMoves.add(move);
		resetCachedValues();
	}

	public Move parseUciMove(String uciMove) throws Exception {
		Coordinate from = Coordinate.parse(uciMove.substring(0, 2));
		Coordinate to = Coordinate.parse(uciMove.substring(2, 4));
		Figure newFigure = null;
		if (uciMove.length() > 4) {
			String newFigureLetter = uciMove.substring(4, 5);
			switch (newFigureLetter) {
			case "q":
				newFigure = Figure.Queen;
				break;
			case "r":
				newFigure = Figure.Rook;
				break;
			case "b":
				newFigure = Figure.Bishop;
				break;
			case "n":
				newFigure = Figure.Knight;
				break;
			default:
				throw new Exception(newFigureLetter);
			}
		}
		return getMove(from, to, newFigure);
	}

	public BoardPosition performUciMove(String uciMove) throws Exception {
		return performMove(parseUciMove(uciMove));
	}

}
