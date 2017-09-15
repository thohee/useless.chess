package useless.chess.player;

import java.util.ArrayList;
import java.util.List;

import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.Move;

/**
 * Simple generic minimax-player with alpha-beta-pruning
 * 
 * @author Thomas
 *
 */
public abstract class MinimaxPlayer extends Player {

	public MinimaxPlayer(Colour colour) {
		super(colour);
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

	private List<GameState> choices = new ArrayList<>();

	@Override
	public Move makeMove(BoardPosition boardPosition) {
		assert (boardPosition.getColourToMove().equals(this.colour) && !boardPosition.getPossibleMoves().isEmpty());
		choices.clear();
		GameState root = new GameState(boardPosition);
		maxValue(root, getMin(), getMax());
		return choices.stream().max((c1, c2) -> c1.getValue().compareTo(c2.getValue())).get().getBoardPosition()
				.getLastMove();
	}

	private static Value min(Value a, Value b) {
		return a.compareTo(b) < 0 ? a : b;
	}

	private static Value max(Value a, Value b) {
		return a.compareTo(b) > 0 ? a : b;
	}

	private boolean terminalTest(GameState gameState) {
		return gameState.getDepth() >= getMaxDepth() || gameState.getBoardPosition().getPossibleMoves().isEmpty()
				|| (gameState.getDepth() >= getCutoffDepth() && isQuiescent(gameState.getBoardPosition()));
	}

	private Value maxValue(GameState gameState, Value alpha, Value beta) {
		Value result = null;
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
		return result;
	}

	private Value minValue(GameState gameState, Value alpha, Value beta) {
		Value result = null;
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
		if (gameState.getDepth() == 1) {
			gameState.setValue(result);
			choices.add(gameState);
		}
		return result;
	}

	protected abstract Value getMin();

	protected abstract Value getMax();

	protected abstract Value evaluate(BoardPosition boardPosition);

	protected abstract int getMaxDepth();

	protected abstract int getCutoffDepth();

	protected abstract boolean isQuiescent(BoardPosition boardPosition);

	protected abstract List<Move> getPossibleMoves(BoardPosition boardPosition);
}
