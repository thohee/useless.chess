package de.thohee.useless.chess.board;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

import de.thohee.useless.chess.board.Move.Capture;
import de.thohee.useless.chess.board.Move.Castling;
import de.thohee.useless.chess.board.Move.IllegalMoveFormatException;

public class PGNParser {

	private static Integer parseColumn(String m) {
		final List<String> columns = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
		Integer column = null;
		for (int i = 0; i < m.length(); ++i) {
			String c = m.substring(i, i + 1);
			int maybeColumn = columns.indexOf(c);
			if (maybeColumn >= 0) {
				column = maybeColumn;
			}
		}
		return column;
	}

	private static Integer parseRow(String m) {
		final List<String> rows = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
		Integer row = null;
		for (int i = 0; i < m.length(); ++i) {
			String c = m.substring(i, i + 1);
			int maybeRow = rows.indexOf(c);
			if (maybeRow >= 0) {
				row = maybeRow;
			}
		}
		return row;
	}

	private static Coordinate parseTargetCoordinate(String m) throws IllegalMoveFormatException {
		Integer column = parseColumn(m);
		Integer row = parseRow(m);
		if (column == null || row == null) {
			throw new IllegalMoveFormatException(m);
		}
		return Coordinate.get(column, row);
	}

	private static Move parseMove(BoardPosition boardPosition, String moveString) throws IllegalMoveFormatException {
		if (moveString.equals("O-O")) {
			return new Move(boardPosition.getColourToMove(), Castling.KingSide);
		} else if (moveString.equals("O-O-O")) {
			return new Move(boardPosition.getColourToMove(), Castling.QueenSide);
		} else {
			Figure figure = Figure.parse(moveString.substring(0, 1));
			Coordinate target = parseTargetCoordinate(moveString);
			Capture capture = moveString.contains("x") ? Capture.Regular : Capture.None;
			if (moveString.contains("e.p.") || (capture.equals(Capture.Regular) && figure.equals(Figure.Pawn)
					&& boardPosition.get(target) == null)) {
				capture = Capture.EnPassant;
			}
			Figure newFigure = null;
			if (moveString.contains("=")) {
				int i = moveString.indexOf("=");
				newFigure = Figure.parse(moveString.substring(i + 1, i + 2));
			}
			List<Move> possibleMoves = new LinkedList<>();
			for (Move move : boardPosition.getPossibleMoves()) {
				if (move.getCastling() == null && move.getFigure().equals(figure) && move.getCapture().equals(capture)
						&& move.getTo().equals(target)
						&& ((newFigure == null && move.getNewFigure() == null) || (newFigure != null
								&& move.getNewFigure() != null && newFigure.equals(move.getNewFigure())))) {
					possibleMoves.add(move);
				}
			}
			if (possibleMoves.size() > 1) {
				String disambiguation = moveString.substring(0, moveString.indexOf(target.toString()));
				Integer column = parseColumn(disambiguation);
				Integer row = parseRow(disambiguation);
				if (column != null && row != null) {
					Coordinate src = Coordinate.get(column, row);
					possibleMoves = possibleMoves.stream().filter(m -> m.getFrom().equals(src))
							.collect(Collectors.toList());
				} else if (column != null) {
					possibleMoves = possibleMoves.stream().filter(m -> m.getFrom().getColumn() == column)
							.collect(Collectors.toList());
				} else if (row != null) {
					possibleMoves = possibleMoves.stream().filter(m -> m.getFrom().getRow() == row)
							.collect(Collectors.toList());
				}
			}
			if (possibleMoves.isEmpty() || possibleMoves.size() > 1) {
				throw new IllegalMoveFormatException(boardPosition.getPossibleMoves().toString() + " => "
						+ possibleMoves.toString() + " <-> " + moveString);
			}
			return possibleMoves.iterator().next();
		}
	}

	private static String next(Scanner moveScanner) {
		String token = moveScanner.next();
		while (token.isEmpty() || token.endsWith(".")) {
			token = next(moveScanner);
		}
		if (token.startsWith("{")) {
			while (!token.contains("}") && moveScanner.hasNext()) {
				token = moveScanner.next();
			}
			token = next(moveScanner);
		}
		try {
			Integer.parseInt(token);
			token = next(moveScanner);
		} catch (NumberFormatException e) {

		}
		return token;
	}

	public static List<GameReport> parse(String filename) throws IllegalMoveFormatException, FileNotFoundException {
		List<GameReport> reports = new LinkedList<>();
		Scanner scanner = new Scanner(new File(filename));
		GameReport current = new GameReport();
		BoardPosition boardPosition = null;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("[Event ")) {
				if (current.getEvent() != null) {
					reports.add(current);
				}
				current = new GameReport();
				current.setEvent(line.split("\"")[1]);
				boardPosition = BoardPosition.getInitialPosition();
				current.setInitialPosition(boardPosition);
			} else if (line.startsWith("[FEN ")) {
				int startIndex = line.indexOf("\"", 0) + 1;
				int endIndex = line.indexOf("\"", startIndex);
				boardPosition = FENParser.parse(line.substring(startIndex, endIndex));
				current.setInitialPosition(boardPosition);
			} else if (line.startsWith("[Round ")) {
				current.setRound(line.split("\"")[1]);
			} else if (line.startsWith("[Date ")) {
				current.setDate(line.split("\"")[1]);
			} else if (line.startsWith("[White ")) {
				current.setWhite(line.split("\"")[1]);
			} else if (line.startsWith("[Black ")) {
				current.setBlack(line.split("\"")[1]);
			} else if (line.startsWith("[Result ")) {
				current.setResult(Result.parse(line.split("\"")[1]));
			} else if (!line.isEmpty() && !line.startsWith("[")) {
				while (scanner.hasNextLine()) {
					String nextLine = scanner.nextLine();
					if (!nextLine.isEmpty()) {
						line += " " + nextLine;
					} else {
						break;
					}
				}
				line = line.trim();
				Result result = Result.parse(line);
				if (current.getResult() != null && !current.getResult().equals(result)) {
					scanner.close();
					throw new IllegalStateException(result.toString());
				}
				current.setResult(result);
				if (result != null && !result.equals(Result.None)) {
					line = line.substring(0, line.length() - result.toString().length());
				}
				Scanner moveScanner = new Scanner(line);
				moveScanner.useDelimiter("[\\.\\ ]");
				try {
					while (moveScanner.hasNext()) {
						String token = next(moveScanner);
						//						System.out.print(token + " ");
						if (token.equals("*")) {
							break;
						}
						Move moveWhite = parseMove(boardPosition, token);
						current.add(moveWhite);
						boardPosition = boardPosition.performMove(moveWhite);
						if (moveScanner.hasNext()) {
							token = next(moveScanner);
							//							System.out.print(token + " ");
							if (token.equals("*")) {
								break;
							}
							Move moveBlack = parseMove(boardPosition, token);
							current.add(moveBlack);
							boardPosition = boardPosition.performMove(moveBlack);
						}
					}
				} catch (NoSuchElementException e) {

				}
				System.out.println();
				moveScanner.close();
			}
		}
		if (current != null) {
			reports.add(current);
		}
		scanner.close();

		return reports;
	}

}
