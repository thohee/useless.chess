package de.thohee.useless.chess.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.thohee.useless.chess.board.BoardPosition;

public class TranspositionTable {

	private static final int MAX_SIZE = 10000;

	private long counter = 0L;

	private HashMap<BoardPosition, Value> hashMap = new HashMap<>();
	private List<BoardPosition> fifoQueue = new LinkedList<>();

	public Value get(BoardPosition boardPosition) {
		Value value = hashMap.get(boardPosition);
		if (value != null) {
			++counter;
		}
		return value;
	}

	public void put(BoardPosition boardPosition, Value value) {
		hashMap.put(boardPosition, value);
		fifoQueue.add(boardPosition);
		while (fifoQueue.size() > MAX_SIZE) {
			hashMap.remove(fifoQueue.remove(0));
		}
	}

	public void clear() {
		hashMap.clear();
		fifoQueue.clear();
		counter = 0L;
	}

	public long getCounter() {
		return counter;
	}

}
