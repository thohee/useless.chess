package de.thohee.useless.chess.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Move;

/**
 * Simple generic minimax-player with alpha-beta-pruning
 * 
 * @author Thomas
 *
 */
public abstract class MinimaxPlayer extends Player {

	private OutputWriter outputWriter = null;

	private TranspositionTable transpositionTable = null;

	private AtomicBoolean stop = new AtomicBoolean(false);

	private Integer maxDepth = null;

	private Long maxMillis = null;
	private Long starttime = null;

	public MinimaxPlayer(Colour colour) {
		super(colour);
	}

	public void setUseTranspositionTable() {
		this.transpositionTable = new TranspositionTable();
	}

	@Override
	public void setOutputWriter(OutputWriter outputWriter) {
		this.outputWriter = outputWriter;
	}

	private class GameState {

		private BoardPosition boardPosition;

		private int depth = 0;

		private Value value = null;

		public GameState(BoardPosition boardPosition) {
			this.boardPosition = boardPosition;
		}

		private GameState createSuccessorState(Move move) {
			GameState successor = new GameState(this.boardPosition.performMove(move));
			successor.depth = this.depth + 1;
			return successor;
		}

		public BoardPosition getBoardPosition() {
			return boardPosition;
		}

		public int getDepth() {
			return depth;
		}

		public Value getValue() {
			return value;
		}

		public void setValue(Value value) {
			this.value = value;
		}

	}

	private TreeSet<GameState> choices = new TreeSet<>(new Comparator<GameState>() {
		@Override
		public int compare(GameState gameState1, GameState gameState2) {
			// sorted by descending value
			int cmp = gameState2.getValue().compareTo(gameState1.getValue());
			if (cmp == 0) {
				cmp = gameState1.getBoardPosition().hashCode() - gameState2.getBoardPosition().hashCode();
				if (cmp == 0) {
					if (gameState1.getBoardPosition().equals(gameState2.getBoardPosition())) {
						return 0;
					} else {
						return System.identityHashCode(gameState1) - System.identityHashCode(gameState2);
					}
				}
			}
			return cmp;
		}
	});

	protected class InterruptedException extends Exception {
		private static final long serialVersionUID = 11135510449244745L;
	}

	private void writeLine(String line) {
		if (outputWriter != null) {
			outputWriter.debug(line);
		}
	}

	@Override
	public void stop() {
		stop.set(true);
	}

	protected void checkStop() throws InterruptedException {
		if (stop.get()) {
			throw new InterruptedException();
		}
		if (maxMillis != null && starttime != null) {
			long elapsedTime = System.currentTimeMillis() - starttime;
			if (elapsedTime >= maxMillis - 50) {
				writeLine("break after " + Long.toString(elapsedTime) + "ms");
				throw new InterruptedException();
			}
		}
	}

	@Override
	public Move makeMove(BoardPosition boardPosition, Params params) {
		this.stop.set(false);
		this.maxDepth = null;
		this.maxMillis = null;
		this.starttime = System.currentTimeMillis();
		assert (boardPosition.getColourToMove().equals(this.colour) && !boardPosition.getPossibleMoves().isEmpty());
		this.maxDepth = params.maxDepthInPlies;
		this.maxMillis = params.maxTimeInMillis;
		if (params.infinite) {
			assert (params.maxDepthInPlies == null);
			assert (params.maxTimeInMillis == null);
			this.maxDepth = null;
			this.maxMillis = null;
		}
		if (transpositionTable != null) {
			transpositionTable.clear();
		}
		choices.clear();
		List<GameState> evaluatedChoices = null;
		GameState root = new GameState(boardPosition);
		try {
			if (this.maxDepth != null) {
				maxValue(root, getMin(), getMax());
			} else {
				this.maxDepth = 3;
				while (true) {
					writeLine("maxDepth = " + maxDepth);
					maxValue(root, getMin(), getMax());
					evaluatedChoices = new ArrayList<>(choices);
					choices.clear();
					if (transpositionTable != null) {
						transpositionTable.clear();
					}
					maxDepth += 2;
				}
			}
		} catch (InterruptedException e) {
		}
		if (evaluatedChoices != null && !evaluatedChoices.isEmpty()) {
			writeLine("returning best choice of previous max depth");
			return evaluatedChoices.get(0).getBoardPosition().getLastMove();
		} else if (!choices.isEmpty()) {
			writeLine("returning best choice of current max depth");
			return choices.iterator().next().getBoardPosition().getLastMove();
		} else {
			writeLine("returning first possible move");
			return boardPosition.getPossibleMoves().iterator().next();
		}
	}

	private static Value min(Value a, Value b) {
		return a.compareTo(b) < 0 ? a : b;
	}

	private static Value max(Value a, Value b) {
		return a.compareTo(b) > 0 ? a : b;
	}

	private boolean terminalTest(GameState gameState) {
		return (getMaxDepth() != null && gameState.getDepth() >= getMaxDepth())
				|| gameState.getBoardPosition().getPossibleMoves().isEmpty() || (getCutoffDepth() != null
						&& gameState.getDepth() >= getCutoffDepth() && isQuiescent(gameState.getBoardPosition()));
	}

	private Value maxValue(GameState gameState, Value alpha, Value beta) throws InterruptedException {
		checkStop();
		Value result = transpositionTable != null ? transpositionTable.get(gameState.getBoardPosition()) : null;
		if (result == null) {
			if (terminalTest(gameState)) {
				result = evaluate(gameState.getBoardPosition());
			} else {
				Value v = getMin();
				for (Move move : getPossibleMoves(gameState.getBoardPosition())) {
					GameState successor = gameState.createSuccessorState(move);
					v = max(v, minValue(successor, alpha, beta));
					if (v.compareTo(beta) >= 0) {
						break;
					}
					alpha = max(alpha, v);
				}
				result = v;
			}
			if (transpositionTable != null) {
				transpositionTable.put(gameState.getBoardPosition(), result);
			}
		}
		return result;
	}

	private Value minValue(GameState gameState, Value alpha, Value beta) throws InterruptedException {
		checkStop();
		Value result = transpositionTable != null ? transpositionTable.get(gameState.getBoardPosition()) : null;
		if (result == null) {
			if (terminalTest(gameState)) {
				result = evaluate(gameState.getBoardPosition());
			} else {
				Value v = getMax();
				for (Move move : getPossibleMoves(gameState.getBoardPosition())) {
					GameState successor = gameState.createSuccessorState(move);
					v = min(v, maxValue(successor, alpha, beta));
					if (v.compareTo(alpha) <= 0) {
						break;
					}
					beta = min(beta, v);
				}
				result = v;
			}
			if (transpositionTable != null) {
				transpositionTable.put(gameState.getBoardPosition(), result);
			}
		}
		if (gameState.getDepth() == 1) {
			gameState.setValue(result);
			choices.add(gameState);
		}
		return result;
	}

	protected abstract Value getMin();

	protected abstract Value getMax();

	protected abstract Value evaluate(BoardPosition boardPosition);

	protected Integer getMaxDepth() {
		return maxDepth;
	}

	protected Integer getCutoffDepth() {
		return getMaxDepth() != null ? getMaxDepth().intValue() * 6 / 10 : null;
	}

	protected abstract boolean isQuiescent(BoardPosition boardPosition);

	protected abstract List<Move> getPossibleMoves(BoardPosition boardPosition);
}
