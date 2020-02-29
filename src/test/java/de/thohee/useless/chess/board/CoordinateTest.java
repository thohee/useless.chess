package de.thohee.useless.chess.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.thohee.useless.chess.board.Coordinate;

public class CoordinateTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testToString() {

		assertEquals("a1", new Coordinate(0, 0).toString());
		assertEquals("h8", new Coordinate(7, 7).toString());

		List<String> columns = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");

		for (int c = 0; c < 8; ++c) {
			for (int r = 0; r < 8; ++r) {
				assertEquals(columns.get(c) + (r + 1), new Coordinate(c, r).toString());
			}
		}

	}

	@Test
	public void testConstructEquals() {

		for (int c = 0; c < 8; ++c) {
			for (int r = 0; r < 8; ++r) {
				assertEquals(new Coordinate(c, r), new Coordinate(c, r));
				assertFalse(new Coordinate(c, r).equals(new Coordinate(7 - c, r)));
			}
		}
	}

	@Test
	public void testFailConstruction() {
		expectedException.expect(AssertionError.class);
		new Coordinate(1, 8);
	}

	@Test
	public void testParse() {

		assertEquals(new Coordinate(3, 7), Coordinate.parse("d8"));
		assertNull(Coordinate.parse("a9"));
		assertNull(Coordinate.parse("i3"));
		assertNull(Coordinate.parse("x"));
		assertNull(Coordinate.parse(""));
		assertNull(Coordinate.parse(null));
	}
}
