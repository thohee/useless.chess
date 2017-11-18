package useless.chess.board;

import java.io.FileWriter;
import java.io.IOException;

public class PGNWriter {

	public static void write(GameReport gameReport, String filename) throws IOException {
		FileWriter fileWriter = new FileWriter(filename);
		if (gameReport.getEvent() != null) {
			fileWriter.write("[Event \"" + gameReport.getEvent() + "\"]\n");
		}
		if (gameReport.getDate() != null) {
			fileWriter.write("[Date \"" + gameReport.getDate() + "\"]\n");
		}
		if (gameReport.getRound() != null) {
			fileWriter.write("[Round \"" + gameReport.getRound() + "\"]\n");
		}
		if (gameReport.getWhite() != null) {
			fileWriter.write("[White \"" + gameReport.getWhite() + "\"]\n");
		}
		if (gameReport.getBlack() != null) {
			fileWriter.write("[Black \"" + gameReport.getBlack() + "\"]\n");
		}
		if (gameReport.getResult() != null && !gameReport.getResult().equals(Result.None)) {
			fileWriter.write("[Result \"" + gameReport.getResult().toString() + "\"]\n");
		}
		if (gameReport.getMoves() != null) {
			fileWriter.write(Move.toString(gameReport.getMoves(), true));
		}
		if (gameReport.getResult() != null && !gameReport.getResult().equals(Result.None)) {
			fileWriter.write("  " + gameReport.getResult().toString());
		}
		fileWriter.write("\n");
		fileWriter.close();
	}
}