package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.thohee.useless.chess.board.Figure;

public class FigureTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testParse() {
		for (Figure figure : Figure.values()) {
			assertEquals(figure, Figure.parse(figure.toString()));
		}
		assertEquals(Figure.Pawn, Figure.parse("P"));
		assertEquals(Figure.Pawn, Figure.parse("X"));
		assertEquals(Figure.Pawn, Figure.parse(""));
		expectedException.expect(NullPointerException.class);
		assertEquals(Figure.Pawn, Figure.parse(null));
	}

}
