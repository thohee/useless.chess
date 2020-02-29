package de.thohee.useless.chess.player;

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

	private boolean infinite = false;

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

	// sorted by descending value
	private TreeSet<GameState> choices = new TreeSet<>(
			(gameState1, gameState2) -> gameState2.getValue().compareTo(gameState1.getValue()));

	protected class InterruptedException extends Exception {
		private static final long serialVersionUID = 11135510449244745L;
	}

	private void writeLine(String line) {
		if (outputWriter != null) {
			outputWriter.writeLine(line);
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
			if (elapsedTime >= maxMillis - 20) {
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
		this.infinite = false;
		this.starttime = System.currentTimeMillis();
		assert (boardPosition.getColourToMove().equals(this.colour) && !boardPosition.getPossibleMoves().isEmpty());
		this.maxDepth = params.maxDepthInPlies;
		this.maxMillis = params.maxTimeInMillis;
		this.infinite = params.infinite;
		if (transpositionTable != null) {
			transpositionTable.clear();
		}
		choices.clear();
		GameState root = new GameState(boardPosition);
		boolean interrupted = false;
		try {
			maxValue(root, getMin(), getMax());
		} catch (InterruptedException e) {
			interrupted = true;
		}
		if (infinite) {
			while (!stop.get()) {
				try {
					Thread.sleep(100);
				} catch (java.lang.InterruptedException e) {
					continue;
				}
			}
		}
		if (!choices.isEmpty()) {
			if (interrupted) {
				writeLine("returning best choice");
			}
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
