package useless.chess.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.Move;
import useless.chess.board.Move.IllegalMoveFormatException;
import useless.chess.board.Piece;

public class HumanPlayer extends Player {

	public HumanPlayer(Colour colour) {
		super(colour);
	}

	@Override
	public Move makeMove(BoardPosition boardPosition) {
		Move move = null;
		while (move == null) {
			System.out.println("enter valid move or 'quit': ");
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				String input = bufferedReader.readLine();
				if (input.toLowerCase().equals("quit")) {
					throw new RuntimeException("quit");
				}
				move = Move.parse(boardPosition.getColourToMove(), input);
				if (!boardPosition.getPossibleMoves().contains(move)) {
					Move inputMove = move;
					move = null;
					if (inputMove.getCastling() == null) {
						Piece piece = boardPosition.get(inputMove.getFrom());
						if (piece != null) {
							// TODO: en passant
							Move.Capture capture = boardPosition.get(inputMove.getTo()) != null ? Move.Capture.Regular
									: Move.Capture.None;
							Move intendedMove = new Move(boardPosition.getColourToMove(), piece.getFigure(),
									inputMove.getFrom(), inputMove.getTo(), capture);
							if (boardPosition.getPossibleMoves().contains(intendedMove)) {
								move = intendedMove;
							}
						}
					}
				}
			} catch (IllegalMoveFormatException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
		return move;
	}

}
