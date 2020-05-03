package de.thohee.useless.chess.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.thohee.useless.chess.board.BoardPosition;
import de.thohee.useless.chess.board.Colour;
import de.thohee.useless.chess.board.Coordinate;
import de.thohee.useless.chess.board.Figure;
import de.thohee.useless.chess.board.GameReport;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;
import de.thohee.useless.chess.board.PGNParser;
import de.thohee.useless.chess.board.Piece;
import de.thohee.useless.chess.board.PositionedPiece;

public class ReadyPlayer1Test implements Player.OutputWriter {

	private final static String logFilename = ReadyPlayer1Test.class.getSimpleName() + ".log";

	@Before
	public void reset() {
		try {
			FileWriter logFile = new FileWriter(logFilename, false);
			logFile.close();
		} catch (IOException e) {
		}
	}

	@Test
	public void testAvoidCheckmateIn4Plies() throws Exception {
		File pgnFile = new File("src/test/resources/games/SiegEnduringSilver.pgn");
		assertTrue(pgnFile.exists() && pgnFile.isFile());
		List<GameReport> games = PGNParser.parse(pgnFile.getPath());
		assertEquals(1, games.size());
		GameReport gameReport = games.get(0);
		BoardPosition boardPosition = BoardPosition.getInitialPosition();
		for (int m = 0; m < 12; ++m) {
			Move move = gameReport.getMoves().get(m);
			boardPosition = boardPosition.performMove(move);
		}
		ReadyPlayer1 player = new ReadyPlayer1(boardPosition.getColourToMove(), false);
		Player.Params params = new Player.Params();
		params.maxDepthInPlies = 4;
		boardPosition = boardPosition.performMove(player.makeMove(boardPosition, params));
		Move knightMove = new Move(boardPosition.getColourToMove(), Figure.Knight, Coordinate.get(5, 5),
				Coordinate.get(4, 3), Capture.None);
		Move knightMoveWithPawnCapture = new Move(boardPosition.getColourToMove(), Figure.Knight, Coordinate.get(5, 5),
				Coordinate.get(4, 3), Capture.Regular);
		if (boardPosition.getPossibleMoves().contains(knightMove)) {
			boardPosition = boardPosition.performMove(knightMove);
		} else if (boardPosition.getPossibleMoves().contains(knightMoveWithPawnCapture)) {
			boardPosition = boardPosition.performMove(knightMoveWithPawnCapture);
		}
		boardPosition = boardPosition.performMove(player.makeMove(boardPosition, params));
		Move queenMoveWithPawnCapture = new Move(boardPosition.getColourToMove(), Figure.Queen, Coordinate.get(7, 3),
				Coordinate.get(5, 1), Capture.Regular);
		Move queenMoveWithoutCapture = new Move(boardPosition.getColourToMove(), Figure.Queen, Coordinate.get(7, 3),
				Coordinate.get(5, 1), Capture.None);
		if (boardPosition.getPossibleMoves().contains(queenMoveWithPawnCapture)) {
			boardPosition = boardPosition.performMove(queenMoveWithPawnCapture);
		} else if (boardPosition.getPossibleMoves().contains(queenMoveWithoutCapture)) {
			boardPosition = boardPosition.performMove(queenMoveWithoutCapture);
		}
		System.out.println(boardPosition.toString());
		System.out.println(boardPosition.showPerformedMoves());
		assertFalse(boardPosition.isCheckmate());
	}

	@Test
	public void testDetectLoomingCheckMate() throws Exception {
		File pgnFile = new File("src/test/resources/games/SiegEnduringSilver.pgn");
		assertTrue(pgnFile.exists() && pgnFile.isFile());
		List<GameReport> games = PGNParser.parse(pgnFile.getPath());
		assertEquals(1, games.size());
		GameReport gameReport = games.get(0);
		BoardPosition boardPosition = BoardPosition.getInitialPosition();
		for (int m = 0; m < 14; ++m) {
			Move move = gameReport.getMoves().get(m);
			boardPosition = boardPosition.performMove(move);
		}
		Set<Move> avoidingMoves = new HashSet<>();
		avoidingMoves.add(boardPosition.parseUciMove("g1h3"));
		avoidingMoves.add(boardPosition.parseUciMove("d1c2"));
		avoidingMoves.add(boardPosition.parseUciMove("d1d2"));
		avoidingMoves.add(boardPosition.parseUciMove("d1d3"));
		ReadyPlayer1 player = new ReadyPlayer1(boardPosition.getColourToMove(), true);
		Player.Params params = new Player.Params();
		params.maxDepthInPlies = 2;
		Move move = player.makeMove(boardPosition, params);
		System.out.println(boardPosition.toString());
		player.printEvaluatedChoices(System.out);
		assertTrue(move.asUciMove(), avoidingMoves.contains(move));
	}

	private BoardPosition playGame(Map<Colour, Player> players, Player.Params params) {
		BoardPosition boardPosition = BoardPosition.getInitialPosition();
		int m = 1;
		while (!boardPosition.getPossibleMoves().isEmpty()) {
			Player player = players.get(boardPosition.getColourToMove());
			Move move = player.makeMove(boardPosition, params);
			if (boardPosition.getColourToMove().equals(Colour.White)) {
				System.out.print(Integer.toString(m) + ": " + move.toString());
				++m;
			} else {
				System.out.println(" " + move.toString());
			}
			boardPosition = boardPosition.performMove(move);
		}
		if (boardPosition.getColourToMove().equals(Colour.Black)) {
			System.out.println();
		}
		System.out.println(boardPosition.getResult());
		System.out.println(boardPosition.toString());
		return boardPosition;
	}

	@Ignore
	@Test
	public void testPlay() {
		Player.Params params = new Player.Params();
		params.maxDepthInPlies = 5;

		for (Colour ownColour : Colour.values()) {

			for (long seed : Arrays.asList(13L, 47L, 71L)) {

				Player opponent = new RandomPlayer(ownColour.opposite(), seed);
				ReadyPlayer1 self = new ReadyPlayer1(ownColour, true);

				Map<Colour, Player> players = new HashMap<>();
				players.put(opponent.getColour(), opponent);
				players.put(self.getColour(), self);

				BoardPosition boardPosition = playGame(players, params);
				assertTrue(boardPosition.isCheckmate() && boardPosition.getColourToMove().equals(ownColour.opposite()));
			}
		}
	}

	@Ignore
	@Test
	public void testAvoidDraw() {
		Player.Params params = new Player.Params();
		params.maxDepthInPlies = 5;
		Colour ownColour = Colour.Black;
		ReadyPlayer1 self = new ReadyPlayer1(Colour.Black, true);
		Player opponent = new RandomPlayer(ownColour.opposite(), 71L);
		Map<Colour, Player> players = new HashMap<>();
		players.put(opponent.getColour(), opponent);
		players.put(self.getColour(), self);
		BoardPosition boardPosition = playGame(players, params);
		assertTrue(boardPosition.isCheckmate() && boardPosition.getColourToMove().equals(ownColour.opposite()));
	}

	@Test
	public void testAvoidDraw2() throws FileNotFoundException, IllegalMoveFormatException {
		Player.Params params = new Player.Params();
		params.maxDepthInPlies = 5;
		ReadyPlayer1 player = new ReadyPlayer1(Colour.Black, true);

		File pgnFile = new File("src/test/resources/games/DrawAgainstRandomPlayer2.pgn");
		assertTrue(pgnFile.exists() && pgnFile.isFile());
		List<GameReport> games = PGNParser.parse(pgnFile.getPath());
		assertEquals(1, games.size());
		GameReport gameReport = games.get(0);
		BoardPosition boardPosition = BoardPosition.getInitialPosition();
		for (int m = 0; m < 101; ++m) {
			Move move = gameReport.getMoves().get(m);
			boardPosition = boardPosition.performMove(move);
		}
		Move unexpectedMove = boardPosition.getMove(Coordinate.a2, Coordinate.b1);
		Move move = player.makeMove(boardPosition, params);
		player.printEvaluatedChoices(System.out);
		assertNotEquals(unexpectedMove, move);
		assertFalse(boardPosition.performMove(move).isDraw());
	}

	@Override
	public void info(String line) {
		debug(line);
	}

	@Override
	public void debug(String line) {
		try {
			FileWriter logFile = new FileWriter(logFilename, true);
			logFile.write(line + "\n");
			logFile.close();
		} catch (IOException e) {
		}

	}

	@Test
	public void testEvaluateThreatsAndProtections() {

		ReadyPlayer1 whitePlayer1 = new ReadyPlayer1(Colour.White, false);
		ReadyPlayer1 blackPlayer1 = new ReadyPlayer1(Colour.Black, false);

		{
			// white queen threatens unprotected black pawn
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.White, Figure.Queen)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.Black, Figure.Pawn))),
							Colour.White);
			assertEquals(ReadyPlayer1.getValue(Figure.Pawn), whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(-1 * ReadyPlayer1.getValue(Figure.Pawn),
					blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{
			// white queen threatens unprotected black knight
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.White, Figure.Queen)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.Black, Figure.Knight))),
							Colour.White);
			assertEquals(ReadyPlayer1.getValue(Figure.Knight),
					whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(-1 * ReadyPlayer1.getValue(Figure.Knight),
					blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{
			// white queen is no threat to black queen, which is protected by pawn, but is
			// itself threatened by black queen
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.White, Figure.Queen)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.Black, Figure.Queen)),
									new PositionedPiece(Coordinate.f4, new Piece(Colour.Black, Figure.Pawn))),
							Colour.White);
			assertEquals(-1 * ReadyPlayer1.getValue(Figure.Queen),
					whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(ReadyPlayer1.getValue(Figure.Queen),
					blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{
			// queen exchange possible with equal value loss for both sides
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.White, Figure.Queen)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.Black, Figure.Queen)),
									new PositionedPiece(Coordinate.f4, new Piece(Colour.Black, Figure.Pawn)),
									new PositionedPiece(Coordinate.a1, new Piece(Colour.White, Figure.Rook))),
							Colour.White);
			assertEquals(0, whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(0, blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{
			// the black pawn is protected by two pieces and threatened by only two pieces
			// => no real threat
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.White, Figure.Bishop)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.Black, Figure.Pawn)),
									new PositionedPiece(Coordinate.e4, new Piece(Colour.Black, Figure.Knight)),
									new PositionedPiece(Coordinate.h1, new Piece(Colour.White, Figure.Knight)),
									new PositionedPiece(Coordinate.g8, new Piece(Colour.Black, Figure.Rook))),
							Colour.White);
			assertEquals(0, whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(0, blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{
			// the black pawn is protected by two pieces and threatened by three pieces
			// which will attack in ascending order of value
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.White, Figure.Bishop)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.Black, Figure.Pawn)),
									new PositionedPiece(Coordinate.e4, new Piece(Colour.Black, Figure.Knight)),
									new PositionedPiece(Coordinate.h1, new Piece(Colour.White, Figure.Knight)),
									new PositionedPiece(Coordinate.g8, new Piece(Colour.Black, Figure.Rook)),
									new PositionedPiece(Coordinate.a3, new Piece(Colour.White, Figure.Rook))),
							Colour.White);
			assertEquals(
					ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop),
					whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(
					-1 * (ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop)),
					blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{

			// the white pawn is protected by two pieces and threatened by three pieces
			// which will attack in ascending order of value
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.Black, Figure.Bishop)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.White, Figure.Pawn)),
									new PositionedPiece(Coordinate.e4, new Piece(Colour.White, Figure.Knight)),
									new PositionedPiece(Coordinate.h1, new Piece(Colour.Black, Figure.Knight)),
									new PositionedPiece(Coordinate.g8, new Piece(Colour.White, Figure.Rook)),
									new PositionedPiece(Coordinate.a3, new Piece(Colour.Black, Figure.Rook))),
							Colour.White);
			assertEquals(
					-1 * (ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop)),
					whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(
					ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop),
					blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{

			// the white pawn is protected by two pieces and threatened by three pieces
			// in addition, the second white pawn is threatened by the second black knight
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.Black, Figure.Bishop)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.White, Figure.Pawn)),
									new PositionedPiece(Coordinate.e4, new Piece(Colour.White, Figure.Knight)),
									new PositionedPiece(Coordinate.h1, new Piece(Colour.Black, Figure.Knight)),
									new PositionedPiece(Coordinate.g8, new Piece(Colour.White, Figure.Rook)),
									new PositionedPiece(Coordinate.a3, new Piece(Colour.Black, Figure.Rook)),
									new PositionedPiece(Coordinate.d7, new Piece(Colour.White, Figure.Pawn)),
									new PositionedPiece(Coordinate.b6, new Piece(Colour.Black, Figure.Knight))),
							Colour.White);
			assertEquals(
					-1 * (ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop) + ReadyPlayer1.getValue(Figure.Pawn)),
					whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(
					ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop) + ReadyPlayer1.getValue(Figure.Pawn),
					blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}

		{

			// the white pawn is protected by two pieces and threatened by three pieces
			// in addition, the black pawn is threatened by the second white knight
			BoardPosition boardPosition = BoardPosition
					.createPosition(
							Arrays.asList(new PositionedPiece(Coordinate.e1, new Piece(Colour.Black, Figure.Bishop)),
									new PositionedPiece(Coordinate.g3, new Piece(Colour.White, Figure.Pawn)),
									new PositionedPiece(Coordinate.e4, new Piece(Colour.White, Figure.Knight)),
									new PositionedPiece(Coordinate.h1, new Piece(Colour.Black, Figure.Knight)),
									new PositionedPiece(Coordinate.g8, new Piece(Colour.White, Figure.Rook)),
									new PositionedPiece(Coordinate.a3, new Piece(Colour.Black, Figure.Rook)),
									new PositionedPiece(Coordinate.d7, new Piece(Colour.Black, Figure.Pawn)),
									new PositionedPiece(Coordinate.b6, new Piece(Colour.White, Figure.Knight))),
							Colour.White);
			assertEquals(
					-1 * (ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop) - ReadyPlayer1.getValue(Figure.Pawn)),
					whitePlayer1.evaluateThreatsAndProtections(boardPosition));
			assertEquals(
					ReadyPlayer1.getValue(Figure.Pawn) + ReadyPlayer1.getValue(Figure.Rook)
							- ReadyPlayer1.getValue(Figure.Bishop) - ReadyPlayer1.getValue(Figure.Pawn),
					blackPlayer1.evaluateThreatsAndProtections(boardPosition));
		}
	}
}
