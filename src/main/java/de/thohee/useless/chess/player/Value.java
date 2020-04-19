package de.thohee.useless.chess.player;

/**
 * This interfaces allows for scalar values as well as value vectors
 * 
 * @author Thomas
 *
 */
public interface Value extends Comparable<Value> {

	boolean isInvalid();
}
