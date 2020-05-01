package de.thohee.useless.chess.player;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.thohee.useless.chess.board.BoardPosition;

public class TranspositionTable {

	private static final int MAX_SIZE = 10000;

	private long cacheHits = 0L;
	private long cacheMisses = 0L;

	private Map<BoardPosition.Key, Value> hashMap = new ConcurrentHashMap<BoardPosition.Key, Value>();
	private Queue<BoardPosition.Key> fifoQueue = new ConcurrentLinkedQueue<>();

	public Value get(BoardPosition.Key boardPosition) {
		Value value = hashMap.get(boardPosition);
		if (value != null) {
			++cacheHits;
		} else {
			++cacheMisses;
		}
		return value;
	}

	public void put(BoardPosition.Key boardPosition, Value value) {
		hashMap.put(boardPosition, value);
		fifoQueue.add(boardPosition);
		while (fifoQueue.size() > MAX_SIZE) {
			hashMap.remove(fifoQueue.poll());
		}
	}

	public void clear() {
		hashMap.clear();
		fifoQueue.clear();
		cacheHits = 0L;
	}

	public long getCacheHits() {
		return cacheHits;
	}

	public long getCacheMisses() {
		return cacheMisses;
	}

}
