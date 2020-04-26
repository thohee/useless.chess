package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class FigureTest {

	@Test
	public void testParse() {
		for (Figure figure : Figure.values()) {
			assertEquals(figure, Figure.parse(figure.toString()));
		}
		assertEquals(Figure.Pawn, Figure.parse("P"));
		assertEquals(Figure.Pawn, Figure.parse("X"));
		assertEquals(Figure.Pawn, Figure.parse(""));
		assertThrows(NullPointerException.class, () -> Figure.parse(null));
	}

}
