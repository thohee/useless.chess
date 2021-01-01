package de.thohee.useless.chess.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class ValueVectorTest {

	@Test
	public void testCompareTo() {

		assertEquals(0, ValueVector.createMin().compareTo(ValueVector.createMin()));
		assertEquals(0, ValueVector.createMax().compareTo(ValueVector.createMax()));
		assertEquals(-1, ValueVector.createMin().compareTo(ValueVector.createMax()));
		assertEquals(1, ValueVector.createMax().compareTo(ValueVector.createMin()));

		ValueVector vector4x4 = new ValueVector();
		vector4x4.add(4);
		vector4x4.add(4);
		vector4x4.add(4);
		vector4x4.add(4);

		ValueVector vector4x5 = new ValueVector();
		vector4x5.add(5);
		vector4x5.add(5);
		vector4x5.add(5);
		vector4x5.add(5);

		assertEquals(0, vector4x4.compareTo(vector4x4));
		assertEquals(-1, vector4x4.compareTo(vector4x5));
		assertEquals(1, vector4x5.compareTo(vector4x4));

		ValueVector vector3x5 = new ValueVector();
		vector3x5.add(5);
		vector3x5.add(5);
		vector3x5.add(5);

		assertThrows(AssertionError.class, () -> vector3x5.compareTo(vector4x5));

		ValueVector vector0 = new ValueVector();
		assertEquals(0, vector0.compareTo(vector0));

		assertThrows(AssertionError.class, () -> vector3x5.compareTo(vector0));
		assertThrows(AssertionError.class, () -> vector0.compareTo(vector4x5));

		ValueVector vector123 = new ValueVector();
		vector123.add(1);
		vector123.add(2);
		vector123.add(3);

		assertEquals(0, vector123.compareTo(vector123));
		assertEquals(-1, vector123.compareTo(vector3x5));
		assertEquals(1, vector3x5.compareTo(vector123));

		ValueVector vector223 = new ValueVector();
		vector223.add(2);
		vector223.add(2);
		vector223.add(3);

		ValueVector vector113 = new ValueVector();
		vector113.add(1);
		vector113.add(1);
		vector113.add(3);

		ValueVector vector122 = new ValueVector();
		vector122.add(1);
		vector122.add(2);
		vector122.add(2);

		assertEquals(-1, vector123.compareTo(vector223));
		assertEquals(1, vector123.compareTo(vector113));
		assertEquals(1, vector123.compareTo(vector122));

		assertEquals(-1, ValueVector.createMin().compareTo(vector4x4));
		assertEquals(-1, ValueVector.createMin().compareTo(vector4x5));
		assertEquals(-1, ValueVector.createMin().compareTo(vector3x5));
		assertEquals(-1, ValueVector.createMin().compareTo(vector123));
		assertEquals(-1, ValueVector.createMin().compareTo(vector223));
		assertEquals(-1, ValueVector.createMin().compareTo(vector113));
		assertEquals(-1, ValueVector.createMin().compareTo(vector122));
		assertEquals(-1, ValueVector.createMin().compareTo(vector0));

		assertEquals(1, ValueVector.createMax().compareTo(vector4x4));
		assertEquals(1, ValueVector.createMax().compareTo(vector4x5));
		assertEquals(1, ValueVector.createMax().compareTo(vector3x5));
		assertEquals(1, ValueVector.createMax().compareTo(vector123));
		assertEquals(1, ValueVector.createMax().compareTo(vector223));
		assertEquals(1, ValueVector.createMax().compareTo(vector113));
		assertEquals(1, ValueVector.createMax().compareTo(vector122));
		assertEquals(1, ValueVector.createMax().compareTo(vector0));

		ValueVector vector3xMax = new ValueVector();
		vector3xMax.add(Integer.MAX_VALUE);
		vector3xMax.add(Integer.MAX_VALUE);
		vector3xMax.add(Integer.MAX_VALUE);

		assertEquals(0, vector3xMax.compareTo(ValueVector.createMax()));
		assertEquals(0, ValueVector.createMax().compareTo(vector3xMax));
		assertEquals(1, vector3xMax.compareTo(ValueVector.createMin()));
		assertEquals(-1, ValueVector.createMin().compareTo(vector3xMax));
		assertEquals(1, vector3xMax.compareTo(vector123));
		assertEquals(-1, vector123.compareTo(vector3xMax));

		ValueVector vector3xMin = new ValueVector();
		vector3xMin.add(Integer.MIN_VALUE);
		vector3xMin.add(Integer.MIN_VALUE);
		vector3xMin.add(Integer.MIN_VALUE);

		assertEquals(0, vector3xMin.compareTo(ValueVector.createMin()));
		assertEquals(0, ValueVector.createMin().compareTo(vector3xMin));
		assertEquals(-1, vector3xMin.compareTo(ValueVector.createMax()));
		assertEquals(1, ValueVector.createMax().compareTo(vector3xMin));
		assertEquals(-1, vector3xMin.compareTo(vector123));
		assertEquals(1, vector123.compareTo(vector3xMin));

		ValueVector vectorMinMin3 = new ValueVector();
		vectorMinMin3.add(Integer.MIN_VALUE);
		vectorMinMin3.add(Integer.MIN_VALUE);
		vectorMinMin3.add(3);

		assertEquals(-1, ValueVector.createMin().compareTo(vectorMinMin3));
		assertEquals(1, vectorMinMin3.compareTo(ValueVector.createMin()));
		assertEquals(-1, vector3xMin.compareTo(vectorMinMin3));
		assertEquals(1, vectorMinMin3.compareTo(vector3xMin));
		assertEquals(1, ValueVector.createMax().compareTo(vectorMinMin3));
		assertEquals(-1, vectorMinMin3.compareTo(ValueVector.createMax()));

		ValueVector vectorMaxMax3 = new ValueVector();
		vectorMaxMax3.add(Integer.MAX_VALUE);
		vectorMaxMax3.add(Integer.MAX_VALUE);
		vectorMaxMax3.add(3);

		assertEquals(-1, ValueVector.createMin().compareTo(vectorMaxMax3));
		assertEquals(1, vectorMaxMax3.compareTo(ValueVector.createMin()));
		assertEquals(1, vector3xMax.compareTo(vectorMaxMax3));
		assertEquals(-1, vectorMaxMax3.compareTo(vector3xMax));
		assertEquals(1, ValueVector.createMax().compareTo(vectorMaxMax3));
		assertEquals(-1, vectorMaxMax3.compareTo(ValueVector.createMax()));
	}

}
