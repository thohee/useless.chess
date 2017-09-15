package useless.chess.board;

import java.util.LinkedList;
import java.util.List;

public class GameReport {

	public static enum Result {
		White, Black, Draw, None;
		@Override
		public String toString() {
			switch (this) {
			case White:
				return "1-0";
			case Black:
				return "0-1";
			case Draw:
				return "1/2-1/2";
			default:
				return "???";
			}
		}

		public static Result parse(String s) {
			if (s == null || s.isEmpty()) {
				throw new IllegalArgumentException(s);
			} else if (s.endsWith("1-0")) {
				return Result.White;
			} else if (s.endsWith("0-1")) {
				return Result.Black;
			} else if (s.endsWith("1/2-1/2")) {
				return Result.Draw;
			} else {
				return Result.None;
			}
		}
	}

	private String event;
	private String round;
	private String date;
	private String white;
	private String black;
	private Result result;
	private List<Move> moves = new LinkedList<>();

	public String getEvent() {
		return event;
	}

	public String getRound() {
		return round;
	}

	public String getDate() {
		return date;
	}

	public String getWhite() {
		return white;
	}

	public String getBlack() {
		return black;
	}

	public Result getResult() {
		return result;
	}

	public void add(Move move) {
		moves.add(move);
	}

	public void setMoves(List<Move> moves) {
		this.moves = moves;
	}

	public List<Move> getMoves() {
		return moves;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public void setRound(String round) {
		this.round = round;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setWhite(String white) {
		this.white = white;
	}

	public void setBlack(String black) {
		this.black = black;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return event + " " + date + " round " + round + " " + (result != null ? result.toString() : "???");
	}

}
