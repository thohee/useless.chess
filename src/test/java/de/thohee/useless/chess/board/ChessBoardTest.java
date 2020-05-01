package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChessBoardTest {

	@Test
	public void testCreateEmpty() {
		ChessBoard chessBoard = new ChessBoard();
		assertEquals(0, chessBoard.size());
		assertFalse(chessBoard.pieces().hasNext());
		assertFalse(chessBoard.positionedPieces().hasNext());
	}

	@Test
	public void testPutPiece() {
		ChessBoard chessBoard = new ChessBoard();
		Piece piece = new Piece(Colour.White, Figure.Queen);
		chessBoard.put(Coordinate.e3, piece);
		assertEquals(1, chessBoard.size());
		assertTrue(chessBoard.pieces().hasNext());
		assertTrue(chessBoard.positionedPieces().hasNext());
		assertEquals(piece, chessBoard.get(Coordinate.e3));
		assertEquals(piece, chessBoard.pieces().next());
		assertEquals(piece, chessBoard.positionedPieces().next().getPiece());
	}

	@Test
	public void testReplacePiece() {
		ChessBoard chessBoard = new ChessBoard();
		Piece piece0 = new Piece(Colour.Black, Figure.King);
		chessBoard.put(Coordinate.e3, piece0);
		Piece piece = new Piece(Colour.White, Figure.Queen);
		chessBoard.put(Coordinate.e3, piece);
		assertEquals(1, chessBoard.size());
		assertTrue(chessBoard.pieces().hasNext());
		assertTrue(chessBoard.positionedPieces().hasNext());
		assertEquals(piece, chessBoard.get(Coordinate.e3));
		assertEquals(piece, chessBoard.pieces().next());
		assertEquals(piece, chessBoard.positionedPieces().next().getPiece());
	}

	@Test
	public void testRemovePiece() {
		ChessBoard chessBoard = new ChessBoard();
		Piece piece = new Piece(Colour.White, Figure.Queen);
		chessBoard.put(Coordinate.e3, piece);
		chessBoard.remove(Coordinate.e3);
		assertEquals(0, chessBoard.size());
		assertFalse(chessBoard.pieces().hasNext());
		assertFalse(chessBoard.positionedPieces().hasNext());
	}

	@Test
	public void testCopy() {
		ChessBoard chessBoard = new ChessBoard();
		chessBoard.put(Coordinate.a1, new Piece(Colour.White, Figure.Rook));
		chessBoard.put(Coordinate.e3, new Piece(Colour.White, Figure.Queen));
		chessBoard.put(Coordinate.g8, new Piece(Colour.Black, Figure.Rook));
		ChessBoard copiedChessBoard = new ChessBoard(chessBoard);
		assertEquals(chessBoard.hashCode(), copiedChessBoard.hashCode());
		assertEquals(chessBoard, copiedChessBoard);
	}

	@Test
	public void testUpdateHashCode() {
		ChessBoard chessBoard = new ChessBoard();
		int hash0 = chessBoard.hashCode();
		chessBoard.put(Coordinate.a1, new Piece(Colour.White, Figure.Rook));
		int hash1 = chessBoard.hashCode();
		assertNotEquals(hash0, hash1);
		chessBoard.put(Coordinate.e3, new Piece(Colour.White, Figure.Queen));
		int hash2 = chessBoard.hashCode();
		assertNotEquals(hash1, hash2);
		chessBoard.put(Coordinate.g8, new Piece(Colour.Black, Figure.Rook));
		int hash3 = chessBoard.hashCode();
		assertNotEquals(hash2, hash3);
		chessBoard.remove(Coordinate.e3);
		int hash4 = chessBoard.hashCode();
		assertNotEquals(hash3, hash4);
	}

	@Test
	public void testEqquals() {
		ChessBoard chessBoard = new ChessBoard();
		chessBoard.put(Coordinate.a1, new Piece(Colour.White, Figure.Rook));
		chessBoard.put(Coordinate.e3, new Piece(Colour.White, Figure.Queen));
		chessBoard.put(Coordinate.g8, new Piece(Colour.Black, Figure.Rook));
		// same position but different pieces
		ChessBoard equivalentChessBoard = new ChessBoard();
		equivalentChessBoard.put(Coordinate.a1, new Piece(Colour.White, Figure.Rook));
		equivalentChessBoard.put(Coordinate.e3, new Piece(Colour.White, Figure.Queen));
		equivalentChessBoard.put(Coordinate.g8, new Piece(Colour.Black, Figure.Rook));
		// even with other piece instances equivalent board positions count as equal
		assertEquals(chessBoard.hashCode(), equivalentChessBoard.hashCode());
		assertEquals(chessBoard, equivalentChessBoard);
	}
}
