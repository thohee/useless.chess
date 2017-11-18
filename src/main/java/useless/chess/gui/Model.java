package useless.chess.gui;

import useless.chess.board.BoardPosition;
import useless.chess.board.Colour;
import useless.chess.board.Result;

public interface Model {

	void registerListener(View view);

	BoardPosition getBoardPosition();

	Colour getOwnColour();

	Result getResult();

}
