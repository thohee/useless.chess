package de.thohee.useless.chess.player;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Move;

/**
 * Simple generic minimax-player with alpha-beta-pruning
 * 
 * @author Thomas
 *
 */
public abstract class MinimaxPlayer extends EnginePlayer {

	private boolean debug = false;

	private OutputWriter outputWriter = null;

	private TranspositionTable transpositionTable = null;

	private AtomicBoolean stop = new AtomicBoolean(false);

	private Integer maxDepth = null;

	private Long maxMillis = null;
	private Long starttime = null;

	private long visitedNodes = 0L;

	public MinimaxPlayer(Colour colour, boolean useTranspositionTable) {
		super(colour);
		if (useTranspositionTable) {
			this.transpositionTable = new TranspositionTable();
		}
	}

	@Override
	public void setOutputWriter(OutputWriter outputWriter) {
		this.outputWriter = outputWriter;
	}

	public void setDebug() {
		debug = true;
	}

	protected class GameState {

		private BoardPosition boardPosition;

		private int depth = 0;

		private Value value = null;

		public GameState(BoardPosition boardPosition) {
			this.boardPosition = boardPosition;
		}

		protected GameState createSuccessorState(Move move) {
			GameState successor = new GameState(this.boardPosition.justPerformMove(move));
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

	protected class GameStateComparator implements Comparator<GameState> {
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
	}

	private List<GameState> previouslyEvaluatedMoves = null;

	private TreeSet<GameState> evaluatedMoves = new TreeSet<>(new GameStateComparator());

	protected class InterruptedException extends Exception {
		private static final long serialVersionUID = 11135510449244745L;
	}

	protected void writeLine(String line) {
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
		this.visitedNodes = 0L;
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
		evaluatedMoves.clear();
		GameState root = new GameState(boardPosition);
		try {
			if (this.maxDepth != null) {
				maxValue(root, getMin(), getMax());
			} else {
				this.maxDepth = 3;
				while (true) {
					writeLine("maxDepth = " + maxDepth);
					maxValue(root, getMin(), getMax());
					previouslyEvaluatedMoves = new ArrayList<>(evaluatedMoves);
					evaluatedMoves.clear();
					if (transpositionTable != null) {
						transpositionTable.clear();
					}
					maxDepth += 2;
				}
			}
		} catch (InterruptedException e) {
		}
		writeLine("#visited nodes: " + visitedNodes);
		if (transpositionTable != null) {
			writeLine("#cache hits:      " + transpositionTable.getCacheHits());
			writeLine("#cache misses:    " + transpositionTable.getCacheMisses());
		}
		if (previouslyEvaluatedMoves != null && !previouslyEvaluatedMoves.isEmpty()) {
			writeLine("returning best choice of previous max depth");
			return previouslyEvaluatedMoves.get(0).getBoardPosition().getLastMove();
		} else if (!evaluatedMoves.isEmpty()) {
			writeLine("returning best choice of current max depth");
			return evaluatedMoves.iterator().next().getBoardPosition().getLastMove();
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
				|| gameState.getBoardPosition().isDrawDisregardingStalemate()
				|| gameState.getBoardPosition().getAllPossibleMoves().isEmpty() || (getCutoffDepth() != null
						&& gameState.getDepth() >= getCutoffDepth() && isQuiescent(gameState.getBoardPosition()));
	}

	private Value maxValue(GameState gameState, Value alpha, Value beta) throws InterruptedException {
		++visitedNodes;
		checkStop();
		BoardPosition.Key key = gameState.getBoardPosition().getKey();
		Value result = transpositionTable != null ? transpositionTable.get(key) : null;
		if (result == null) {
			if (gameState.getBoardPosition().isStillCheck()) {
				// disallowed state
				result = getInvalid();
				assert (result != null);
			} else if (terminalTest(gameState)) {
				result = evaluate(gameState.getBoardPosition());
				if (debug) {
					result.setBoardPosition(gameState.getBoardPosition());
				}
			} else {
				Value v = getMin();
				boolean atLeastOneValid = false;
				for (GameState successor : getSuccessors(gameState)) {
					Value m = minValue(successor, alpha, beta);
					if (m.isInvalid()) {
						continue;
					}
					atLeastOneValid = true;
					v = max(v, m);
					alpha = max(alpha, v);
					if (alpha.compareTo(beta) >= 0) {
						// minimizer already has a better or equally bad option
						// no use to look for an even better alternative
						break;
					} else if (v.isMax()) {
						// can't get better
						break;
					}
				}
				if (atLeastOneValid) {
					result = v;
				} else {
					// we are actually in a terminal state
					result = evaluate(gameState.getBoardPosition());
				}
			}
			if (transpositionTable != null) {
				transpositionTable.put(key, result);
			}
		}
		return result;
	}

	private Value minValue(GameState gameState, Value alpha, Value beta) throws InterruptedException {
		++visitedNodes;
		checkStop();
		BoardPosition.Key key = gameState.getBoardPosition().getKey();
		Value result = transpositionTable != null ? transpositionTable.get(key) : null;
		if (result == null) {
			if (gameState.getBoardPosition().isStillCheck()) {
				// disallowed state
				result = getInvalid();
				assert (result != null);
			} else if (terminalTest(gameState)) {
				result = evaluate(gameState.getBoardPosition());
				if (debug) {
					result.setBoardPosition(gameState.getBoardPosition());
				}
			} else {
				Value v = getMax();
				boolean atLeastOneValid = false;
				for (GameState successor : getSuccessors(gameState)) {
					Value m = maxValue(successor, alpha, beta);
					if (m.isInvalid()) {
						continue;
					}
					atLeastOneValid = true;
					v = min(v, m);
					beta = min(beta, v);
					if (alpha.compareTo(beta) >= 0) {
						// maximizer already found an equally good or better move elsewhere
						// no use to look for a worse alternative
						break;
					} else if (v.isMin()) {
						// can't get worse
						break;
					}
				}
				if (atLeastOneValid) {
					result = v;
				} else {
					// we are actually in a terminal state
					result = evaluate(gameState.getBoardPosition());
				}
				result = v;
			}
			if (transpositionTable != null) {
				transpositionTable.put(key, result);
			}
		}
		if (gameState.getDepth() == 1 && !result.isInvalid() && (result.isMin() || alpha.compareTo(beta) < 0)) {
			// we must only take the value for granted, if we did not prune possibly worse
			// alternatives!
			gameState.setValue(result);
			evaluatedMoves.add(gameState);
		}
		return result;
	}

	@Override
	public void printEvaluatedChoices(PrintStream printStream) {
		Collection<GameState> col = evaluatedMoves.isEmpty() && previouslyEvaluatedMoves != null
				? previouslyEvaluatedMoves
				: evaluatedMoves;
		for (GameState gameState : col) {
			if (debug) {
				assert (gameState.getValue().getBoardPosition() != null);
				List<Move> performedMoves = gameState.getValue().getBoardPosition().getPerformedMoves();
				printStream.println(Move.toString(performedMoves.subList(
						gameState.getBoardPosition().getPerformedMoves().size() - 1, performedMoves.size()), false));
				printStream.println(gameState.getValue().getBoardPosition().toString());
			} else {
				printStream.println(
						gameState.getBoardPosition().getLastMove().asUciMove() + " " + gameState.getValue().toString());
			}
		}
		printStream.println();
	}

	@Override
	public Map<Move, Value> getEvaluatedMoves() {
		Collection<GameState> evaluatedGameStates = evaluatedMoves.isEmpty() && previouslyEvaluatedMoves != null
				? previouslyEvaluatedMoves
				: evaluatedMoves;
		return evaluatedGameStates.stream()
				.collect(Collectors.toMap(g -> g.getBoardPosition().getLastMove(), GameState::getValue));
	}

	protected abstract Value getInvalid();

	protected abstract Value getMin();

	protected abstract Value getMax();

	protected abstract Value evaluate(BoardPosition boardPosition);

	protected Integer getMaxDepth() {
		return maxDepth;
	}

	protected abstract Integer getCutoffDepth();

	protected abstract boolean isQuiescent(BoardPosition boardPosition);

	protected abstract List<GameState> getSuccessors(GameState gameState);
}
