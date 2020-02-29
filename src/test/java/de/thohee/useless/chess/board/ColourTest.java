package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.thohee.useless.chess.board.Colour;

public class ColourTest {

	@Test
	public void testOpposite() {

		assertEquals(Colour.White, Colour.Black.opposite());
		assertEquals(Colour.Black, Colour.White.opposite());
	}

}
