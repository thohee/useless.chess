package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CoordinateTest {

	@Test
	public void testToString() {

		assertEquals("a1", Coordinate.get(0, 0).toString());
		assertEquals("h8", Coordinate.get(7, 7).toString());

		List<String> columns = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");

		for (int c = 0; c < 8; ++c) {
			for (int r = 0; r < 8; ++r) {
				assertEquals(columns.get(c) + (r + 1), Coordinate.get(c, r).toString());
			}
		}

	}

	@Test
	public void testConstructEquals() {

		for (int c = 0; c < 8; ++c) {
			for (int r = 0; r < 8; ++r) {
				assertEquals(Coordinate.get(c, r), Coordinate.get(c, r));
				assertFalse(Coordinate.get(c, r).equals(Coordinate.get(7 - c, r)));
			}
		}
	}

	@Test
	public void testFailConstruction() {
		assertThrows(AssertionError.class, () -> Coordinate.get(1, 8));
	}

	@Test
	public void testParse() {

		assertEquals(Coordinate.get(3, 7), Coordinate.parse("d8"));
		assertNull(Coordinate.parse("a9"));
		assertNull(Coordinate.parse("i3"));
		assertNull(Coordinate.parse("x"));
		assertNull(Coordinate.parse(""));
		assertNull(Coordinate.parse(null));
	}
}
