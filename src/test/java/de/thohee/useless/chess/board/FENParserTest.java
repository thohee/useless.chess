package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class FENParserTest {

	@Test
	public void testParse() {
		String fenString = "rrk5/8/8/8/8/8/8/6K1 w - - 18 3";
		System.out.println(fenString);
		BoardPosition boardPosition = FENParser.parse(fenString);
		System.out.println(boardPosition.toString());
		assertNotNull(boardPosition.get(Coordinate.a8));
		assertNotNull(boardPosition.get(Coordinate.b8));
		assertNotNull(boardPosition.get(Coordinate.c8));
		assertNotNull(boardPosition.get(Coordinate.g1));
		assertEquals(Colour.Black, boardPosition.get(Coordinate.a8).getColour());
		assertEquals(Colour.Black, boardPosition.get(Coordinate.b8).getColour());
		assertEquals(Colour.Black, boardPosition.get(Coordinate.c8).getColour());
		assertEquals(Colour.White, boardPosition.get(Coordinate.g1).getColour());
		assertEquals(Figure.Rook, boardPosition.get(Coordinate.a8).getFigure());
		assertEquals(Figure.Rook, boardPosition.get(Coordinate.b8).getFigure());
		assertEquals(Figure.King, boardPosition.get(Coordinate.c8).getFigure());
		assertEquals(Figure.King, boardPosition.get(Coordinate.g1).getFigure());
		assertEquals(4, boardPosition.getDepth());
	}

}
