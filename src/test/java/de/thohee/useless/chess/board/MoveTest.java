package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Move.Castling;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;

public class MoveTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testParse() throws IllegalMoveFormatException {

		for (Colour c : Colour.values()) {

			assertEquals(new Move(c, Castling.KingSide), Move.parse(c, "0-0"));
			assertEquals(new Move(c, Castling.QueenSide), Move.parse(c, "0-0-0"));

			assertEquals(new Move(c, Figure.Pawn, Coordinate.parse("a2"), Coordinate.parse("a3"), Capture.None),
					Move.parse(c, "a2-a3"));

			assertEquals(new Move(c, Figure.Rook, Coordinate.parse("h8"), Coordinate.parse("d8"), Capture.None),
					Move.parse(c, "Rh8-d8"));

			assertEquals(new Move(c, Figure.Rook, Coordinate.parse("h8"), Coordinate.parse("d8"), Capture.Regular),
					Move.parse(c, "Rh8xd8"));

			assertEquals(new Move(c, Figure.Pawn, Coordinate.parse("a5"), Coordinate.parse("b6"), Capture.EnPassant),
					Move.parse(c, "a5xb6e.p."));

		}

		{
			Move move = Move.parse(Colour.White, "a7-a8=Q");
			assertEquals(Colour.White, move.getColour());
			assertEquals(Figure.Pawn, move.getFigure());
			assertEquals(Coordinate.parse("a7"), move.getFrom());
			assertEquals(Coordinate.parse("a8"), move.getTo());
			assertEquals(Capture.None, move.getCapture());
			assertNotNull(move.getNewFigure());
			assertEquals(Figure.Queen, move.getNewFigure());
		}

		{
			Move move = Move.parse(Colour.Black, "a2-a1=Q");
			assertEquals(Colour.Black, move.getColour());
			assertEquals(Figure.Pawn, move.getFigure());
			assertEquals(Coordinate.parse("a2"), move.getFrom());
			assertEquals(Coordinate.parse("a1"), move.getTo());
			assertEquals(Capture.None, move.getCapture());
			assertNotNull(move.getNewFigure());
			assertEquals(Figure.Queen, move.getNewFigure());
		}

		expectException("Pa2-a3");
		expectException("a2a3");
		expectException("");

		expectedException.expect(NullPointerException.class);
		Move.parse(Colour.White, null);
	}

	private void expectException(String s) {
		try {
			Move.parse(Colour.White, s);
			assertTrue(s, false);
		} catch (Move.IllegalMoveFormatException e) {
		}
	}

	@Test
	public void testAsUciResponse() {
		assertEquals("g1f3",
				new Move(Colour.White, Figure.Knight, Coordinate.parse("g1"), Coordinate.parse("f3"), Capture.None)
						.asUciMove());
		assertEquals("e1g1", new Move(Colour.White, Castling.KingSide).asUciMove());
		assertEquals("e8g8", new Move(Colour.Black, Castling.KingSide).asUciMove());
		assertEquals("e1c1", new Move(Colour.White, Castling.QueenSide).asUciMove());
		assertEquals("e8c8", new Move(Colour.Black, Castling.QueenSide).asUciMove());
	}
}
