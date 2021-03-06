package de.thohee.useless.chess.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
import de.thohee.useless.chess.board.FENParser;
import de.thohee.useless.chess.board.Figure;
import de.thohee.useless.chess.board.Move;
import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;
import de.thohee.useless.chess.board.Piece;
import de.thohee.useless.chess.board.PositionLoader;
import de.thohee.useless.chess.board.PositionedPiece;
import de.thohee.useless.chess.player.Player.Params;

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
		BoardPosition boardPosition = PositionLoader.loadPosition("SiegEnduringSilver.pgn", 12);
		ReadyPlayer1 player = new ReadyPlayer1(boardPosition.getColourToMove(), true);
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
		BoardPosition boardPosition = PositionLoader.loadPosition("SiegEnduringSilver.pgn", 14);
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
		BoardPosition boardPosition = PositionLoader.loadPosition("DrawAgainstRandomPlayer2.pgn", 101);
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

	@Ignore
	@Test
	public void testEvaluateOpenings() throws Exception {
		BoardPosition boardPosition = PositionLoader.loadPosition("ThomasWinsAgainstReadyPlayer1.pgn", 13);
		ReadyPlayer1 player = new ReadyPlayer1(Colour.Black, true);
		// player.setDebug();

		BoardPosition a8a7 = boardPosition.performMove(boardPosition.parseUciMove("a8a7"));
		BoardPosition f8e7 = boardPosition.performMove(boardPosition.parseUciMove("f8e7"));
		BoardPosition d8d7 = boardPosition.performMove(boardPosition.parseUciMove("d8d7"));

		System.out.println("a8a7: " + player.evaluateOpening(a8a7));
		System.out.println("f8e7: " + player.evaluateOpening(f8e7));
		System.out.println("d8d7: " + player.evaluateOpening(d8d7));

		Params params = new Player.Params();
		params.maxDepthInPlies = 4;
		player.makeMove(boardPosition, params);
		player.printEvaluatedChoices(System.out);

	}

	private void print(BoardPosition boardPosition, Map<Coordinate, Integer> debugMap) {
		for (int r = 7; r >= 0; --r) {
			for (int c = 0; c < 8; ++c) {
				if (c > 0) {
					System.out.print(" ");
				}
				Coordinate coordinate = Coordinate.get(c, r);
				if (debugMap.containsKey(coordinate)) {
					System.out.print(debugMap.get(coordinate));
				} else if (boardPosition.get(coordinate) != null) {
					Piece p = boardPosition.get(coordinate);
					String f = p.getFigure().toString();
					System.out.print(p.getColour().equals(Colour.Black) ? f.toLowerCase() : f);
				} else {
					System.out.print(".");
				}
			}
			System.out.println();
		}
	}

	@Test
	public void testKingsReach_onEmptyBoard() {
		Map<Coordinate, Integer> debugMap = new HashMap<>();
		BoardPosition boardPosition = FENParser.parse("8/8/8/8/8/3K4/8/8 w - - 0 1");
		int reach = ReadyPlayer1.kingsReach(boardPosition,
				new PositionedPiece(Coordinate.d3, boardPosition.get(Coordinate.d3)), debugMap);
		print(boardPosition, debugMap);
		assertEquals(63, reach);
	}

	@Test
	public void testKingsReach_withOtherPieces() {
		Map<Coordinate, Integer> debugMap = new HashMap<>();
		BoardPosition boardPosition = FENParser.parse("r7/8/8/2p5/3PP3/3K4/7p/6b1 w - - 0 1");
		System.out.println(boardPosition.toString());
		int reach = ReadyPlayer1.kingsReach(boardPosition,
				new PositionedPiece(Coordinate.d3, boardPosition.get(Coordinate.d3)), debugMap);
		print(boardPosition, debugMap);
		assertEquals(42, reach);
	}

	@Test
	public void testAvoidNoSuchElementException() throws FileNotFoundException, IllegalMoveFormatException {

		BoardPosition boardPosition = PositionLoader.loadPosition("NoSuchElementException.pgn");
		ReadyPlayer1 player = new ReadyPlayer1(Colour.Black, true);
		player.setEvaluateOpenings(false);

		Params params = new Params();
		params.maxDepthInPlies = 8;
		Move move = player.makeMove(boardPosition, params);
		System.out.println(move);
	}

	@Test
	public void testDoCastle() throws Exception {

		BoardPosition boardPosition = PositionLoader.loadPosition("WhyNotCastle.pgn", 13);
		System.out.println(boardPosition.toString());
		ReadyPlayer1 player = new ReadyPlayer1(Colour.Black, false);

		BoardPosition path1 = boardPosition.performMove(boardPosition.parseUciMove("d8c8"));
		path1 = path1.performMove(path1.parseUciMove("h2h3"));
		path1 = path1.performMove(path1.parseUciMove("g4f3"));
		path1 = path1.performMove(path1.parseUciMove("e2f3"));
		Integer openingValue1 = player.evaluateOpeningMidgameTacticsAndEndgame(path1);
		System.out.print(path1);
		System.out.println(openingValue1);
		System.out.println();

		BoardPosition path2 = boardPosition.performMove(boardPosition.parseUciMove("e8g8"));
		path2 = path2.performMove(path2.parseUciMove("h2h3"));
		path2 = path2.performMove(path2.parseUciMove("g4f3"));
		path2 = path2.performMove(path2.parseUciMove("g2f3"));
		assertTrue(path2.hasCastled(player.getColour()));
		Integer openingValue2 = player.evaluateOpeningMidgameTacticsAndEndgame(path2);
		System.out.print(path2);
		System.out.println(openingValue2);
		System.out.println();

		assertTrue(openingValue1.compareTo(openingValue2) < 0);

		player.setDebug();
		Params params = new Params();
		params.maxDepthInPlies = 4;
		Move move = player.makeMove(boardPosition, params);
		System.out.println(move);
		player.printEvaluatedChoices(System.out);
		assertTrue(move.getCastling() != null);
	}

	@Test
	public void testCheckmateSingleKing() throws FileNotFoundException, IllegalMoveFormatException {
		BoardPosition boardPosition = PositionLoader
				.loadPosition("ReadyPlayer1againstHimself3foldRepetitionStillNotAvoided.pgn", 112);
		System.out.println(boardPosition);
		int nPerformedMoves = boardPosition.getPerformedMoves().size();
		List<ReadyPlayer1> players = Arrays.asList(new ReadyPlayer1(Colour.White, true),
				new ReadyPlayer1(Colour.Black, true));
		Player.Params params = new Player.Params();
		params.maxDepthInPlies = 4;
		int playerToMove = 0;
		while (!boardPosition.getPossibleMoves().isEmpty()) {
			boardPosition = boardPosition.performMove(players.get(playerToMove).makeMove(boardPosition, params));
			playerToMove = 1 - playerToMove;
		}
		System.out.println(boardPosition);
		System.out.println(Move.toString(
				boardPosition.getPerformedMoves().subList(nPerformedMoves, boardPosition.getPerformedMoves().size()),
				nPerformedMoves + 1, false));
		assert (boardPosition.isCheckmate() && boardPosition.getColourToMove() == Colour.Black);
	}

}
