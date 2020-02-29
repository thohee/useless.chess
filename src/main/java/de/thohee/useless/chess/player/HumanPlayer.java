package de.thohee.useless.chess.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;

public class HumanPlayer extends Player {

	public HumanPlayer(Colour colour) {
		super(colour);
	}

	@Override
	public Move makeMove(BoardPosition boardPosition, Params params) {
		Move move = null;
		while (move == null) {
			System.out.println("enter valid move or 'quit': ");
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				String input = bufferedReader.readLine();
				if (input.toLowerCase().equals("quit")) {
					throw new RuntimeException("quit");
				}
				move = boardPosition.guessMove(input);
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
