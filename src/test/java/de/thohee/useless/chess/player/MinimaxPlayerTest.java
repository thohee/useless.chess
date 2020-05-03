package de.thohee.useless.chess.player;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Coordinate;
import de.thohee.useless.chess.board.Figure;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Piece;
import de.thohee.useless.chess.board.PositionedPiece;
import de.thohee.useless.chess.player.Player.Params;

public class MinimaxPlayerTest {

	@Test
	public void testSupportMate() {

		List<PositionedPiece> positionedPieces = Arrays.asList(
				new PositionedPiece(Coordinate.h1, new Piece(Colour.White, Figure.King)),
				new PositionedPiece(Coordinate.e3, new Piece(Colour.Black, Figure.Knight)),
				new PositionedPiece(Coordinate.g6, new Piece(Colour.Black, Figure.Queen)));
		BoardPosition boardPosition = BoardPosition.createPosition(positionedPieces, Colour.Black);
		System.out.println(boardPosition.toString());
		ReadyPlayer1 player = new ReadyPlayer1(Colour.Black, false);
		player.noOpenings();
		Params params = new Params();
		params.maxDepthInPlies = 2;
		Move move = player.makeMove(boardPosition, params);
		player.printEvaluatedChoices(System.out);
		assertEquals(new Move(Colour.Black, Figure.Queen, Coordinate.g6, Coordinate.g2, Capture.None), move);
	}

}
