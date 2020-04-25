package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Move.Castling;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;

public class BoardPositionTest {

	@Test
	public void testGetPossibleCastlings() {

		// no castlings possible in initial position
		assertTrue(BoardPosition.getInitialPosition().getPossibleCastlings(Colour.White).isEmpty());
		assertTrue(BoardPosition.getInitialPosition().getPossibleCastlings(Colour.Black).isEmpty());

		BoardPosition boardPosition = new BoardPosition();
		boardPosition.set(new Coordinate(4, 0), new Piece(Colour.White, Figure.King));
		boardPosition.addCastlingPiece(boardPosition.get(new Coordinate(4, 0)));
		// rook is missing
		assertTrue(boardPosition.getPossibleCastlings(Colour.White).isEmpty());
		boardPosition.set(new Coordinate(7, 0), new Piece(Colour.White, Figure.Rook));
		// rook is not registered as castling piece
		assertTrue(boardPosition.getPossibleCastlings(Colour.White).isEmpty());
		boardPosition.addCastlingPiece(boardPosition.get(new Coordinate(7, 0)));
		// king side castling
		assertEquals(Collections.singletonList(new Move(Colour.White, Castling.KingSide)),
				boardPosition.getPossibleCastlings(Colour.White));
		boardPosition.set(new Coordinate(5, 0), new Piece(Colour.White, Figure.Bishop));
		// blocked by bishop
		assertTrue(boardPosition.getPossibleCastlings(Colour.White).isEmpty());
		boardPosition.set(new Coordinate(0, 0), new Piece(Colour.White, Figure.Knight));
		boardPosition.addCastlingPiece(boardPosition.get(new Coordinate(0, 0)));
		// queen side castling with knight is not possible
		assertTrue(boardPosition.getPossibleCastlings(Colour.White).isEmpty());
	}

	@Test
	public void testGetPosssibleKingSideCastlingAfterQueenSideRookMoved() {

		for (Colour colour : Arrays.asList(Colour.White)) {

			int row = colour.ordinal() * 7;

			BoardPosition boardPosition = new BoardPosition();
			Piece king = new Piece(colour, Figure.King);
			boardPosition.set(new Coordinate(4, row), king);
			boardPosition.addCastlingPiece(king);
			Piece queenSideRook = new Piece(colour, Figure.Rook);
			boardPosition.set(new Coordinate(0, row), queenSideRook);
			boardPosition.addCastlingPiece(queenSideRook);
			Piece kingSideRook = new Piece(colour, Figure.Rook);
			boardPosition.set(new Coordinate(7, row), kingSideRook);
			boardPosition.addCastlingPiece(kingSideRook);

			List<Move> possibleCastlings = boardPosition.getPossibleCastlings(colour);
			assertEquals(2, possibleCastlings.size());
			assertTrue(possibleCastlings.contains(new Move(colour, Castling.KingSide)));
			assertTrue(possibleCastlings.contains(new Move(colour, Castling.QueenSide)));

			boardPosition = boardPosition.performMove(new Move(colour, Figure.Rook, new Coordinate(0, row),
					new Coordinate(0, (row + 2) % 7), Capture.None));
			possibleCastlings = boardPosition.getPossibleCastlings(colour);
			assertEquals(1, possibleCastlings.size());
			assertTrue(possibleCastlings.contains(new Move(colour, Castling.KingSide)));

		}

	}

	private class MoveMock extends Move {
		public MoveMock(Colour colour) {
			super(colour, Castling.KingSide);
		}
	}

	private List<Move> getPossibleMoves(BoardPosition boardPosition, Coordinate coordinate) {
		return boardPosition.getPossibleMoves().stream().filter(m -> coordinate.equals(m.getFrom()))
				.collect(Collectors.toList());
	}

	@Test
	public void testGetPossibleMoves() throws IllegalMoveFormatException {
		// left of c04 is off board
		final Coordinate c04 = new Coordinate(0, 4);
		for (Colour colour : Colour.values()) {
			BoardPosition boardPosition = new BoardPosition();
			// top of c04 is blocked by own pieces
			boardPosition.set(new Coordinate(0, 5), new Piece(colour, Figure.Pawn)); // blocks the rook, queen, king and
																						// white pawn
			boardPosition.set(new Coordinate(1, 5), new Piece(colour, Figure.Pawn)); // blocks the bishop, queen and
																						// king
			boardPosition.set(new Coordinate(1, 6), new Piece(colour, Figure.Pawn)); // blocks the knight
			// below c04 is 'blocked' by opponent's pieces
			Colour opposite = colour.opposite();
			boardPosition.set(new Coordinate(0, 3), new Piece(opposite, Figure.Pawn)); // beaten by rook, queen, king
																						// and black pawn
			boardPosition.set(new Coordinate(2, 2), new Piece(opposite, Figure.Pawn)); // beaten by bishop and queen,
																						// threatens black king
			boardPosition.set(new Coordinate(1, 2), new Piece(opposite, Figure.Pawn)); // beaten by knight
			for (Figure figure : Figure.values()) {
				Piece piece = new Piece(colour, figure);
				BoardPosition boardPositionWithPiece = new BoardPosition(boardPosition);
				boardPositionWithPiece.set(c04, piece);
				if (colour.equals(Colour.Black)) {
					// unrelated move by black
					boardPositionWithPiece.set(new Coordinate(3, 1), new Piece(Colour.White, Figure.Pawn));
					boardPositionWithPiece = boardPositionWithPiece.performMove(new Move(Colour.White, Figure.Pawn,
							new Coordinate(3, 1), new Coordinate(3, 2), Capture.None));
				}
				Set<Move> actualMoves = new HashSet<>(getPossibleMoves(boardPositionWithPiece, c04));
				Set<Move> expectedMoves = new HashSet<>();
				switch (figure) {
				case Pawn:
					// none, white is blocked by own piece, black is blocked by opponent's piece
					break;
				case Knight:
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(2, 3), Capture.None));
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(2, 5), Capture.None));
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(1, 2), Capture.Regular));
					break;
				case Bishop:
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(1, 3), Capture.None));
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(2, 2), Capture.Regular));
					break;
				case Rook:
					for (int c = 1; c < 8; ++c) {
						expectedMoves.add(new Move(colour, figure, c04, new Coordinate(c, 4), Capture.None));
					}
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(0, 3), Capture.Regular));
					break;
				case Queen:
					for (int c = 1; c < 8; ++c) {
						expectedMoves.add(new Move(colour, figure, c04, new Coordinate(c, 4), Capture.None));
					}
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(0, 3), Capture.Regular));
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(1, 3), Capture.None));
					expectedMoves.add(new Move(colour, figure, c04, new Coordinate(2, 2), Capture.Regular));
					break;
				case King:
					if (colour.equals(Colour.White)) {
						expectedMoves.add(new Move(colour, figure, c04, new Coordinate(1, 4), Capture.None));
						expectedMoves.add(new Move(colour, figure, c04, new Coordinate(0, 3), Capture.Regular));
						expectedMoves.add(new Move(colour, figure, c04, new Coordinate(1, 3), Capture.None));
					}
					break;
				}
				// traceMoves(expectedMoves, actualMoves);
				assertEquals(boardPositionWithPiece.getPositionedPieces().stream()
						.map(e -> e.getKey().toString() + " " + e.getValue().toString())
						.reduce((s1, s2) -> s1 + "," + s2).orElse(""), expectedMoves, actualMoves);
			}
		}
		final int column = 4;
		for (Colour colour : Colour.values()) {
			Piece pawn = new Piece(colour, Figure.Pawn);
			Piece otherPawn = new Piece(colour.opposite(), Figure.Pawn);
			int direction = colour.equals(Colour.White) ? 1 : -1;
			int initialRow = colour.equals(Colour.White) ? 1 : 6;
			int nextRow = initialRow + direction;
			int otherSideRow = initialRow + 3 * direction;
			int beforeLastRow = initialRow + 5 * direction;
			Piece lastPiece = new Piece(colour.opposite(), Figure.Pawn);
			Coordinate lastPosition = new Coordinate(0, 3);
			Move lastMove = new Move(lastPiece.getColour(), lastPiece.getFigure(), lastPosition,
					new Coordinate(lastPosition.getColumn(), lastPosition.getRow() - direction), Capture.None);
			// one step or capture
			{
				BoardPosition boardPosition = new BoardPosition();
				Coordinate startPosition = new Coordinate(column, nextRow);
				boardPosition.set(startPosition, pawn);
				boardPosition.set(lastPosition, lastPiece);
				boardPosition.setLastMove(new MoveMock(lastMove.getColour().opposite()));
				boardPosition = boardPosition.performMove(lastMove);
				Set<Move> expectedMoves = new HashSet<>();
				expectedMoves.add(new Move(colour, Figure.Pawn, startPosition,
						new Coordinate(column, nextRow + direction), Capture.None));
				assertEquals(expectedMoves, new HashSet<Move>(getPossibleMoves(boardPosition, startPosition)));
				// regular captures
				for (int dc : Arrays.asList(-1, 1)) {
					Coordinate captureTarget = new Coordinate(column + dc, nextRow + direction);
					boardPosition.set(captureTarget, otherPawn);
					expectedMoves.add(new Move(colour, Figure.Pawn, startPosition, captureTarget, Capture.Regular));
				}
				assertEquals(expectedMoves, new HashSet<Move>(getPossibleMoves(boardPosition, startPosition)));
			}

			// one or two steps
			{
				BoardPosition boardPosition = new BoardPosition();
				Coordinate startPosition = new Coordinate(column, initialRow);
				boardPosition.set(startPosition, pawn);
				boardPosition.set(lastPosition, lastPiece);
				boardPosition.setLastMove(new MoveMock(lastMove.getColour().opposite()));
				boardPosition = boardPosition.performMove(lastMove);
				Set<Move> expectedMoves = new HashSet<>();
				expectedMoves.add(new Move(colour, Figure.Pawn, startPosition,
						new Coordinate(column, initialRow + direction), Capture.None));
				expectedMoves.add(new Move(colour, Figure.Pawn, startPosition,
						new Coordinate(column, initialRow + 2 * direction), Capture.None));
				assertEquals(expectedMoves, new HashSet<Move>(getPossibleMoves(boardPosition, startPosition)));
			}

			// capture en passant
			{
				BoardPosition boardPosition = new BoardPosition();
				Coordinate startPosition = new Coordinate(column, otherSideRow);
				boardPosition.set(startPosition, pawn);
				int otherColumn = startPosition.getColumn() + 1;
				Coordinate otherTargetPosition = new Coordinate(otherColumn, startPosition.getRow());
				Coordinate otherStartPosition = new Coordinate(otherColumn,
						otherTargetPosition.getRow() + 2 * direction);
				Set<Move> expectedMoves = new HashSet<>();
				expectedMoves.add(new Move(colour, Figure.Pawn, startPosition,
						new Coordinate(column, otherSideRow + direction), Capture.None));
				expectedMoves.add(new Move(colour, Figure.Pawn, startPosition,
						new Coordinate(otherColumn, otherSideRow + direction), Capture.EnPassant));
				Move passingMove = new Move(otherPawn.getColour(), otherPawn.getFigure(), otherStartPosition,
						otherTargetPosition, Capture.None);
				boardPosition.set(otherStartPosition, otherPawn);
				boardPosition.setLastMove(new MoveMock(lastMove.getColour().opposite()));
				boardPosition = boardPosition.performMove(passingMove); // is at the same time the last move
				assertEquals(expectedMoves, new HashSet<Move>(getPossibleMoves(boardPosition, startPosition)));
			}

			// promotion
			{
				BoardPosition boardPosition = new BoardPosition();
				Coordinate startPosition = new Coordinate(column, beforeLastRow);
				boardPosition.set(startPosition, pawn);
				boardPosition.set(lastPosition, lastPiece);
				boardPosition.setLastMove(new MoveMock(lastMove.getColour().opposite()));
				boardPosition = boardPosition.performMove(lastMove);
				HashSet<Move> possibleMoves = new HashSet<Move>(getPossibleMoves(boardPosition, startPosition));
				assertTrue(possibleMoves.size() >= 2);
				Coordinate targetPosition = new Coordinate(column, beforeLastRow + direction);
				List<Figure> promotionFigures = Arrays.asList(Figure.Queen, Figure.Knight, Figure.Bishop, Figure.Rook);
				for (Move move : possibleMoves) {
					assertEquals(colour, move.getColour());
					assertEquals(Figure.Pawn, move.getFigure());
					assertEquals(startPosition, move.getFrom());
					assertEquals(targetPosition, move.getTo());
					assertNotNull(move.getNewPiece());
					assertEquals(colour, move.getNewPiece().getColour());
					assertTrue(promotionFigures.contains(move.getNewPiece().getFigure()));
				}
			}

			// do not set yourself in chess
			{
				BoardPosition boardPosition = BoardPosition.getInitialPosition();
				boardPosition = boardPosition.performMove(Move.parse(boardPosition.getColourToMove(), "Ng1-f3"));
				boardPosition = boardPosition.performMove(Move.parse(boardPosition.getColourToMove(), "c7-c6"));
				boardPosition = boardPosition.performMove(Move.parse(boardPosition.getColourToMove(), "a2-a4"));
				boardPosition = boardPosition.performMove(Move.parse(boardPosition.getColourToMove(), "Qd8-a5"));
				assertFalse(boardPosition.getPossibleMoves()
						.contains(Move.parse(boardPosition.getColourToMove(), "d2-d4")));
			}

			// do not remain in chess
			{
				String[] moves = new String(
						"e2-e3,g7-g6,f2-f4,Ng8-f6,g2-g3,e7-e6,Qd1-f3,Ke8-e7,b2-b3,Ke7-d6,Bc1-a3,c7-c5,d2-d4,Qd8-b6,d4xc5")
								.split(",");
				BoardPosition boardPosition = BoardPosition.getInitialPosition();
				for (String m : moves) {
					Move move = Move.parse(boardPosition.getColourToMove(), m);
					boardPosition = boardPosition.performMove(move);
				}
				assertTrue(boardPosition.getThreatsTo(Colour.Black, Coordinate.parse("d6"))
						.contains(boardPosition.get(Coordinate.parse("c5"))));
				assertTrue(boardPosition.isCheck());
				Move forbiddenMove = Move.parse(boardPosition.getColourToMove(), "Qb6-a5");
				assertFalse(boardPosition.getPossibleMoves().contains(forbiddenMove));
			}

			// 2-step initial pawn move possible
			{
				BoardPosition boardPosition = BoardPosition.getInitialPosition();
				boardPosition.getPossibleMoves().contains(Move.parse(Colour.White, "d2-d4"));
			}
		}
	}

	private BoardPosition prepareBoard(String[] moves) throws IllegalMoveFormatException {
		BoardPosition boardPosition = BoardPosition.getInitialPosition();
		for (String moveDescription : moves) {
			Move move = Move.parse(boardPosition.getColourToMove(), moveDescription);
			assertEquals(moveDescription, move.toString());
			assertTrue(boardPosition.getPossibleMoves().contains(move));
			boardPosition = boardPosition.performMove(move);
		}
		return boardPosition;
	}

	@Test
	public void testGetThreatsTo() {

	}

	@Test
	public void testGetProtections() throws IllegalMoveFormatException {

		final BoardPosition boardPosition = prepareBoard(
				new String[] { "d2-d3", "e7-e5", "a2-a3", "d7-d6", "e2-e4", "f7-f6", "f2-f3", "Nb8-c6" });
		System.out.println(boardPosition.toString());
		Set<Piece> d3 = Arrays.asList("f1", "d1", "c2").stream().map(c -> boardPosition.get(Coordinate.parse(c)))
				.collect(Collectors.toSet());
		assertEquals(d3, boardPosition.getProtections(Coordinate.parse("d3")));
		Set<Piece> c2 = Collections.singleton(boardPosition.get(Coordinate.parse("d1")));
		assertEquals(c2, boardPosition.getProtections(Coordinate.parse("c2")));
		BoardPosition boardPosition2 = boardPosition.performMove(Move.parse(boardPosition.getColourToMove(), "c2-c3"));
		d3.remove(boardPosition.get(Coordinate.parse("c2")));
		assertEquals(d3, boardPosition2.getProtections(Coordinate.parse("d3")));
		Set<Piece> c3 = Arrays.asList("b1", "b2").stream().map(c -> boardPosition.get(Coordinate.parse(c)))
				.collect(Collectors.toSet());
		assertEquals(c3, boardPosition2.getProtections(Coordinate.parse("c3")));
	}

	@Test
	public void testPerformMove() {

		for (Colour colour : Colour.values()) {
			for (Figure figure : Figure.values()) {
				Piece piece = new Piece(colour, figure);
				BoardPosition boardPosition = new BoardPosition();
				boardPosition.set(new Coordinate(4, 4), piece);
				// first move for white or unrelated last move by white before
				if (colour.equals(Colour.Black)) {
					boardPosition.set(new Coordinate(7, 5), new Piece(Colour.White, Figure.Pawn));
					boardPosition = boardPosition.performMove(new Move(Colour.White, Figure.Pawn, new Coordinate(7, 5),
							new Coordinate(7, 6), Capture.None));
				}
				for (Move move : boardPosition.getPossibleMoves()) {
					assertNull(move.getCastling());
					BoardPosition newPosition = boardPosition.performMove(move);
					assertNull(newPosition.get(move.getFrom()));
					assertEquals(piece, newPosition.get(move.getTo()));
				}
			}
		}
	}

	@Test
	public void testPerformCastling() throws IllegalMoveFormatException {
		BoardPosition preparedBoardPosition = new BoardPosition();
		Piece whiteKing = new Piece(Colour.White, Figure.King);
		Piece whiteRook1 = new Piece(Colour.White, Figure.Rook);
		Piece whiteRook2 = new Piece(Colour.White, Figure.Rook);
		Piece blackKing = new Piece(Colour.Black, Figure.King);
		Piece blackRook1 = new Piece(Colour.Black, Figure.Rook);
		Piece blackRook2 = new Piece(Colour.Black, Figure.Rook);
		preparedBoardPosition.set(new Coordinate(4, 0), whiteKing);
		preparedBoardPosition.set(new Coordinate(0, 0), whiteRook1);
		preparedBoardPosition.set(new Coordinate(7, 0), whiteRook2);
		preparedBoardPosition.set(new Coordinate(4, 7), blackKing);
		preparedBoardPosition.set(new Coordinate(0, 7), blackRook1);
		preparedBoardPosition.set(new Coordinate(7, 7), blackRook2);
		preparedBoardPosition.addCastlingPiece(whiteKing);
		preparedBoardPosition.addCastlingPiece(whiteRook1);
		preparedBoardPosition.addCastlingPiece(whiteRook2);
		preparedBoardPosition.addCastlingPiece(blackKing);
		preparedBoardPosition.addCastlingPiece(blackRook1);
		preparedBoardPosition.addCastlingPiece(blackRook2);
		List<Piece> kings = Arrays.asList(whiteKing, blackKing);
		List<Piece> rooks = Arrays.asList(whiteRook2, whiteRook1, blackRook2, blackRook1);
		for (Castling castling : Castling.values()) { // 1. king side 2. queen side
			for (Colour colour : Colour.values()) { // 1. white 2. black
				BoardPosition boardPosition = preparedBoardPosition;
				int row = colour.equals(Colour.White) ? 0 : 7;
				int direction = castling.equals(Castling.KingSide) ? 1 : -1;
				int rookColumn = castling.equals(Castling.KingSide) ? 7 : 0;
				if (colour.equals(Colour.Black)) {
					int oppositeRow = 7 - row;
					boardPosition = boardPosition.performMove(new Move(colour.opposite(), Figure.King,
							new Coordinate(4, oppositeRow), new Coordinate(4 + direction, oppositeRow), Capture.None));
				}
				Move castlingMove = new Move(colour, castling);
				boardPosition = boardPosition.performMove(castlingMove);
				assertEquals(6, boardPosition.getPieces().size());
				assertEquals(kings.get(colour.ordinal()), boardPosition.get(new Coordinate(4 + 2 * direction, row)));
				assertEquals(rooks.get(colour.ordinal() * 2 + castling.ordinal()),
						boardPosition.get(new Coordinate(4 + direction, row)));
				assertNull(boardPosition.get(new Coordinate(rookColumn, row)));
				assertNull(boardPosition.get(new Coordinate(4, row)));
			}
		}
	}

	@Test
	public void testPerfomCaptureEnPassant() throws IllegalMoveFormatException {
		BoardPosition boardPosition = new BoardPosition();
		Piece pawn = new Piece(Colour.White, Figure.Pawn);
		boardPosition.set(Coordinate.parse("a4"), pawn);
		boardPosition = boardPosition.performMove(Move.parse(Colour.White, "a4-a5"));
		boardPosition.set(Coordinate.parse("b7"), new Piece(Colour.Black, Figure.Pawn));
		boardPosition = boardPosition.performMove(Move.parse(Colour.Black, "b7-b5"));
		Move move = Move.parse(Colour.White, "a5xb6e.p.");
		assertTrue(boardPosition.getPossibleMoves().contains(move));
		boardPosition = boardPosition.performMove(move);
		assertEquals(1, boardPosition.getPieces().size());
		assertEquals(pawn, boardPosition.get(Coordinate.parse("b6")));
		assertNull(boardPosition.get(Coordinate.parse("b5")));
	}

	@Test
	public void testIsCheckmate() {
		{
			BoardPosition boardPosition = new BoardPosition();
			boardPosition.set(new Coordinate(0, 0), new Piece(Colour.White, Figure.King));
			boardPosition.set(new Coordinate(0, 2), new Piece(Colour.Black, Figure.Queen));
			assertTrue(boardPosition.isCheck());
			assertFalse(boardPosition.isCheckmate());
		}
		{
			BoardPosition boardPosition = new BoardPosition();
			boardPosition.set(new Coordinate(0, 0), new Piece(Colour.White, Figure.King));
			boardPosition.set(new Coordinate(0, 2), new Piece(Colour.Black, Figure.Queen));
			boardPosition.set(new Coordinate(2, 0), new Piece(Colour.Black, Figure.Rook));
			assertTrue(boardPosition.isCheckmate());
		}

		{
			BoardPosition boardPosition = new BoardPosition();
			boardPosition.set(new Coordinate(0, 0), new Piece(Colour.White, Figure.King));
			boardPosition.set(new Coordinate(1, 0), new Piece(Colour.White, Figure.Pawn));
			boardPosition.set(new Coordinate(0, 1), new Piece(Colour.White, Figure.Pawn));
			boardPosition.set(new Coordinate(2, 2), new Piece(Colour.Black, Figure.Queen));
			assertFalse(boardPosition.isCheckmate());
		}

	}

	private Set<Move> expectedInitiallyPossibleMoves(Colour colour) {
		Set<Move> expectedMoves = new HashSet<>();
		int direction = colour.equals(Colour.White) ? 1 : -1;
		int pawnRow = colour.equals(Colour.White) ? 1 : 6;
		for (int c = 0; c < 8; ++c) {
			expectedMoves.add(new Move(colour, Figure.Pawn, new Coordinate(c, pawnRow),
					new Coordinate(c, pawnRow + direction), Capture.None));
			expectedMoves.add(new Move(colour, Figure.Pawn, new Coordinate(c, pawnRow),
					new Coordinate(c, pawnRow + 2 * direction), Capture.None));
		}
		expectedMoves.add(new Move(colour, Figure.Knight, new Coordinate(1, pawnRow - direction),
				new Coordinate(0, pawnRow + direction), Capture.None));
		expectedMoves.add(new Move(colour, Figure.Knight, new Coordinate(1, pawnRow - direction),
				new Coordinate(2, pawnRow + direction), Capture.None));
		expectedMoves.add(new Move(colour, Figure.Knight, new Coordinate(6, pawnRow - direction),
				new Coordinate(5, pawnRow + direction), Capture.None));
		expectedMoves.add(new Move(colour, Figure.Knight, new Coordinate(6, pawnRow - direction),
				new Coordinate(7, pawnRow + direction), Capture.None));
		return expectedMoves;
	}

	@Test
	public void testGetInitialPosition() throws IllegalMoveFormatException {
		BoardPosition initialPosition = BoardPosition.getInitialPosition();
		assertTrue(initialPosition.getPossibleCastlings(Colour.White).isEmpty());
		assertEquals(expectedInitiallyPossibleMoves(Colour.White), new HashSet<>(initialPosition.getPossibleMoves()));
		BoardPosition boardPosition = initialPosition.performMove(Move.parse(Colour.White, "a2-a3"));
		assertEquals(expectedInitiallyPossibleMoves(Colour.Black), new HashSet<>(boardPosition.getPossibleMoves()));
	}

	@Test
	public void testGetDepth() {
		BoardPosition boardPosition = BoardPosition.getInitialPosition();
		int depth = 0;
		for (int column = 0; column < 8; ++column) {
			for (Colour colour : Colour.values()) {
				assertEquals(depth, boardPosition.getDepth());
				int row = colour.equals(Colour.White) ? 1 : 6;
				int direction = colour.equals(Colour.White) ? 1 : -1;
				Move move = new Move(colour, Figure.Pawn, new Coordinate(column, row),
						new Coordinate(column, row + direction), Capture.None);
				assertTrue(boardPosition.getPossibleMoves().contains(move));
				boardPosition = boardPosition.performMove(move);
				depth++;
			}
		}
	}

	@Test
	public void testIsCheck() {
		BoardPosition boardPosition = new BoardPosition();
		assertFalse(boardPosition.isCheck());
		boardPosition.set(new Coordinate(0, 0), new Piece(Colour.White, Figure.King));
		assertFalse(boardPosition.isCheck());
		boardPosition.set(new Coordinate(0, 2), new Piece(Colour.Black, Figure.Queen));
		assertTrue(boardPosition.isCheck());
	}

	@Test
	public void testIsStalemate() throws IllegalMoveFormatException {
		BoardPosition boardPosition = new BoardPosition();
		boardPosition.set(Coordinate.parse("a1"), new Piece(Colour.White, Figure.King));
		boardPosition.analyze();
		assertFalse(boardPosition.stalemate());
		boardPosition.set(Coordinate.parse("b3"), new Piece(Colour.Black, Figure.Queen));
		boardPosition.analyze();
		assertTrue(boardPosition.stalemate());

		boardPosition = new BoardPosition();
		boardPosition.set(Coordinate.parse("e8"), new Piece(Colour.Black, Figure.King));
		boardPosition.set(Coordinate.parse("d6"), new Piece(Colour.White, Figure.Queen));
		boardPosition.setLastMove(Move.parse(Colour.White, "Qd1-d6"));
		boardPosition.analyze();
		assertFalse(boardPosition.stalemate());
		boardPosition.set(Coordinate.parse("h7"), new Piece(Colour.White, Figure.Rook));
		boardPosition.analyze();
		assertTrue(boardPosition.stalemate());
	}

	@Test
	public void testIsDraw() throws IllegalMoveFormatException {

		BoardPosition boardPosition = BoardPosition.getInitialPosition();
		assertFalse(boardPosition.isDraw());

		// stalemate
		boardPosition = new BoardPosition();
		boardPosition.set(Coordinate.parse("e8"), new Piece(Colour.Black, Figure.King));
		boardPosition.set(Coordinate.parse("d6"), new Piece(Colour.White, Figure.Queen));
		boardPosition.setLastMove(Move.parse(Colour.White, "Qd1-d6"));
		assertFalse(boardPosition.isDraw());
		boardPosition.set(Coordinate.parse("h7"), new Piece(Colour.White, Figure.Rook));
		assertTrue(boardPosition.isDraw());

		// two kings
		boardPosition = new BoardPosition();
		boardPosition.set(Coordinate.parse("e8"), new Piece(Colour.Black, Figure.King));
		boardPosition.set(Coordinate.parse("e1"), new Piece(Colour.White, Figure.King));
		assertTrue(boardPosition.isDraw());

		// 3x repetition
		boardPosition = new BoardPosition();
		boardPosition.set(Coordinate.parse("e8"), new Piece(Colour.Black, Figure.King));
		boardPosition.set(Coordinate.parse("e1"), new Piece(Colour.White, Figure.King));
		boardPosition.set(Coordinate.parse("d6"), new Piece(Colour.White, Figure.Queen));
		assertFalse(boardPosition.isDraw());
		List<Move> moveCycle = Arrays.asList(Move.parse(Colour.White, "Qd6-e6"), Move.parse(Colour.Black, "Ke8-d8"),
				Move.parse(Colour.White, "Qe6-d6"), Move.parse(Colour.Black, "Kd8-e8"));
		for (int i = 1; i <= 2; ++i) {
			for (Move move : moveCycle) {
				assertFalse(boardPosition.showPerformedMoves(), boardPosition.isDraw());
				assertTrue(move.toString(), boardPosition.getPossibleMoves().contains(move));
				boardPosition = boardPosition.performMove(move);
			}
			// start position is repeated for the 3rd time after two cycles
			assertEquals(i == 2, boardPosition.isDraw());
		}

		// 50 moves without pawn and capture
		boardPosition = new BoardPosition();
		boardPosition.set(Coordinate.parse("a8"), new Piece(Colour.Black, Figure.Queen));
		boardPosition.set(Coordinate.parse("a7"), new Piece(Colour.White, Figure.Queen));
		HashMap<Colour, Coordinate> lastPositionByColour = new HashMap<>();
		lastPositionByColour.put(Colour.White, Coordinate.parse("a7"));
		lastPositionByColour.put(Colour.Black, Coordinate.parse("a8"));
		int n = 0;
		for (int m = 1; n < 50; ++m) {
			for (Colour colour : Arrays.asList(Colour.White, Colour.Black)) {
				int startRow = colour.equals(Colour.White) ? 6 : 7;
				int dr = m / 8;
				int r = startRow - dr;
				int c = dr % 2 == 0 ? m % 8 : 7 - (m % 8);
				Coordinate nextPosition = new Coordinate(c, r);
				Coordinate lastPosition = lastPositionByColour.get(colour);
				Piece piece = boardPosition.get(lastPosition);
				assertNotNull(piece);
				Move move = new Move(colour, piece.getFigure(), lastPosition, nextPosition, Capture.None);
				assertTrue(boardPosition.showPerformedMoves() + " => " + boardPosition.getPossibleMoves().toString()
						+ " <> " + move.toString(), boardPosition.getPossibleMoves().contains(move));
				boardPosition = boardPosition.performMove(move);
				++n;
				assertEquals(n == 50, boardPosition.isDraw());
				if (n == 50 || boardPosition.isDraw()) {
					assertTrue(boardPosition.getPossibleMoves().isEmpty());
					break;
				}
				lastPositionByColour.put(colour, nextPosition);
			}
		}
		assertTrue(n == 50);
	}

	@Test
	public void testScenarios() throws IllegalMoveFormatException {
		List<String[]> scenarios = new LinkedList<>();

		scenarios.add(new String[] { "d2-d4", "d7-d6", "e2-e3", "Nb8-a6", "Bf1-b5", "c7-c6", "Bb5xc6" });

		for (String[] scenario : scenarios) {
			BoardPosition boardPosition = BoardPosition.getInitialPosition();
			for (String moveDescription : scenario) {
				Move move = Move.parse(boardPosition.getColourToMove(), moveDescription);
				assertEquals(moveDescription, move.toString());
				if (!boardPosition.getPossibleMoves().contains(move)) {
					System.out.println(boardPosition.showPerformedMoves());
					System.out.println(boardPosition.toString());
					System.out.println(boardPosition.getPossibleMoves().stream().map(m -> m.toString())
							.reduce((s1, s2) -> s1 + "," + s2).orElse(""));
					assertTrue(moveDescription, boardPosition.getPossibleMoves().contains(move));
				}
				boardPosition = boardPosition.performMove(move);
			}
		}
	}

	@Test
	public void testRealMatches() throws FileNotFoundException, IllegalMoveFormatException {

		File gamesDirectory = new File("src/test/resources/games");
		assertTrue(gamesDirectory.isDirectory());

		for (String filename : gamesDirectory.list()) {
			System.out.println(filename);
			List<GameReport> gameReports = PGNParser.parse(gamesDirectory.getPath() + "/" + filename);
			assertFalse(gameReports.isEmpty());
			for (GameReport gameReport : gameReports) {
				System.out.println(gameReport.toString());
				assertTrue(gameReport.getResult() != null);
			}
		}
	}

	@Test
	public void testGetMove() {
		{
			BoardPosition bp = BoardPosition.getInitialPosition();
			bp = bp.performMove(bp.getMove(Coordinate.parse("e2"), Coordinate.parse("e4")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("d7"), Coordinate.parse("d5")));
			assertEquals(new Move(Colour.White, Figure.Pawn, Coordinate.parse("e4"), Coordinate.parse("d5"),
					Capture.Regular), bp.getMove(Coordinate.parse("e4"), Coordinate.parse("d5")));
		}
		{
			// castlings white-king-side, black-queen-side
			BoardPosition bp = BoardPosition.getInitialPosition();
			bp = bp.performMove(bp.getMove(Coordinate.parse("e2"), Coordinate.parse("e4")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("d7"), Coordinate.parse("d5")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("f1"), Coordinate.parse("d3")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("c8"), Coordinate.parse("e6")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("g1"), Coordinate.parse("f3")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("b8"), Coordinate.parse("c6")));
			Move whiteKingSideCastling = new Move(Colour.White, Castling.KingSide);
			assertEquals(whiteKingSideCastling, bp.getMove(Coordinate.parse("e1"), Coordinate.parse("g1")));
			bp = bp.performMove(whiteKingSideCastling);
			bp = bp.performMove(bp.getMove(Coordinate.parse("d8"), Coordinate.parse("d6")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("d1"), Coordinate.parse("e2")));
			Move blackQueenSideCastling = new Move(Colour.Black, Castling.QueenSide);
			assertEquals(blackQueenSideCastling, bp.getMove(Coordinate.parse("e8"), Coordinate.parse("c8")));
		}
		{
			// castlings white-queen-side, black-king-side
			BoardPosition bp = BoardPosition.getInitialPosition();
			bp = bp.performMove(bp.getMove(Coordinate.parse("d2"), Coordinate.parse("d4")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("e7"), Coordinate.parse("e5")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("c1"), Coordinate.parse("e3")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("f8"), Coordinate.parse("d6")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("b1"), Coordinate.parse("c3")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("g8"), Coordinate.parse("f6")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("d1"), Coordinate.parse("d3")));
			Move blackKingSideCastling = new Move(Colour.Black, Castling.KingSide);
			assertEquals(blackKingSideCastling, bp.getMove(Coordinate.parse("e8"), Coordinate.parse("g8")));
			bp = bp.performMove(blackKingSideCastling);
			Move whiteQueenSideCastling = new Move(Colour.White, Castling.QueenSide);
			assertEquals(whiteQueenSideCastling, bp.getMove(Coordinate.parse("e1"), Coordinate.parse("c1")));
		}
		{
			// en passant by white
			BoardPosition bp = BoardPosition.getInitialPosition();
			bp = bp.performMove(bp.getMove(Coordinate.parse("e2"), Coordinate.parse("e4")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("d7"), Coordinate.parse("d5")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("e4"), Coordinate.parse("e5")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("f7"), Coordinate.parse("f5")));
			assertEquals(new Move(Colour.White, Figure.Pawn, Coordinate.parse("e5"), Coordinate.parse("f6"),
					Capture.EnPassant), bp.getMove(Coordinate.parse("e5"), Coordinate.parse("f6")));
		}
		{
			// en passant by black
			BoardPosition bp = BoardPosition.getInitialPosition();
			bp = bp.performMove(bp.getMove(Coordinate.parse("e2"), Coordinate.parse("e4")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("d7"), Coordinate.parse("d5")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("f2"), Coordinate.parse("f3")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("d5"), Coordinate.parse("d4")));
			bp = bp.performMove(bp.getMove(Coordinate.parse("c2"), Coordinate.parse("c4")));
			assertEquals(new Move(Colour.Black, Figure.Pawn, Coordinate.parse("d4"), Coordinate.parse("c3"),
					Capture.EnPassant), bp.getMove(Coordinate.parse("d4"), Coordinate.parse("c3")));
		}
	}

}
