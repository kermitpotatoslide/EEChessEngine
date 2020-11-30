package com.alonsoruibal.chess;

import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.pgn.PgnFile;
import com.alonsoruibal.chess.pgn.PgnImportExport;
import com.alonsoruibal.chess.search.SearchEngine;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Test tournament using the Noomen Test Suite
 */
class TournamentTest implements SearchObserver {
	private static final int GAME_TIME_PER_PLAYER = 5000; // in milliseconds
	private static final int MOVE_TIME_INC = 0; // in milliseconds
	private static final int THINK_TO_DEPTH = Integer.MAX_VALUE; // if > 0, it establishes a depth limit, used with 3 or 6 to make fast tournaments it is useful to fast test evaluator changes
	private static final int THINK_TO_NODES = Integer.MAX_VALUE; // When making changes in the search engine, is better to make tests limiting the search nodes
	private static final int SLEEP = 100;
	private static final int TEST_SIZE = 60;
	private static final int GAMES = 20 * TEST_SIZE; // Test suite is based on 30 games and they are played with whites and blacks, so we make x60 times

	private SearchEngine engine1;
	private SearchEngine engine2;
	private Board b;
	private boolean engine1Whites;
	private int endGame;
	private double[] wins;
	private int wtime;
	private int btime;
	private SearchParameters params;
	private long lastTime;

	@Test
	@Tag("slow")
	void testTournament() {
		Config config1 = new Config();
		config1.setBook(new FileBook("/book_small.bin"));
		Config config2 = new Config();
		config2.setBook(new FileBook("/book_small.bin"));

		// Change here the parameters in one of the chess engines to test the differences
		// Example: config1.setElo(2000);
		// ...
		config1.setLimitStrength(true);
		config1.setElo(2100);
		config2.setLimitStrength(true);
		config2.setElo(2000);

		engine1 = new SearchEngine(config1);
		engine2 = new SearchEngine(config2);

		PgnFile pgn = new PgnFile();
		int pgnGameNumber = 0;

		// wins[0] = draws
		// wins[1] = engine 1 wins
		// wins[2] = engine 2 wins
		wins = new double[3];
		params = new SearchParameters();

		Logger.noLog = true;

		for (int i = 0; i < GAMES; i++) {
			engine1.init();
			engine2.init();

			engine1Whites = (i & 1) == 0;

			b = new Board();

			// Each position is played two times alternating color
			String positionPgn = PgnFile.getGameNumber(this.getClass().getResourceAsStream("/NoomenTestsuite2012.pgn"), pgnGameNumber >>> 1);
			if (positionPgn == null) {
				pgnGameNumber = 0;
				positionPgn = PgnFile.getGameNumber(this.getClass().getResourceAsStream("/NoomenTestsuite2012.pgn"), pgnGameNumber >>> 1);
			} else {
				pgnGameNumber++;
			}

			PgnImportExport.setBoard(b, positionPgn);
			PgnImportExport.setBoard(engine1.getBoard(), positionPgn);
			PgnImportExport.setBoard(engine2.getBoard(), positionPgn);

			engine1.setObserver(this);
			engine2.setObserver(this);

			wtime = GAME_TIME_PER_PLAYER;
			btime = GAME_TIME_PER_PLAYER;

			endGame = 0;

			go();

			//System.out.println("move "+(j+1)+": " + Move.toStringExt(bestMove));

			while (endGame == 0) {
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			double score = wins[1] + wins[0] / 2;
			double total = wins[1] + wins[0] + wins[2];
			double percentage = (score / total);
			double eloDifference = -400 * Math.log(1 / percentage - 1) / Math.log(10);

			System.out.println((total % TEST_SIZE == 0 ? TestColors.ANSI_WHITE : (eloDifference > 0 ? TestColors.ANSI_GREEN : TestColors.ANSI_RED)) +
					"At: " + (i + 1) + " draws: " + wins[0] + " engine1: " + wins[1] + " engine2: " + wins[2] + " elodif: " + String.format("%.2f", eloDifference) + " pointspercentage=" + String.format("%.2f", (percentage * 100)) + //
					TestColors.ANSI_RESET);
		}
	}

	private void go() {
		endGame = b.isEndGame();

		// if time is up
		if (wtime < 0) {
			System.out.println("White losses by time");
			endGame = -1;
		} else if (btime < 0) {
			System.out.println("Black losses by time");
			endGame = 1;
		}

		if (endGame != 0) {
			int index = -1;
			if (endGame == 99) {
				index = 0;
			} else if (endGame == 1) {
				if (engine1Whites) {
					index = 1;
				} else {
					index = 2;
				}
			} else if (endGame == -1) {
				if (engine1Whites) {
					index = 2;
				} else {
					index = 1;
				}
			}
			wins[index]++;
		} else {
			params.setWtime(wtime);
			params.setWinc(MOVE_TIME_INC);
			params.setBtime(btime);
			params.setBinc(MOVE_TIME_INC);
			params.setDepth(THINK_TO_DEPTH);
			params.setNodes(THINK_TO_NODES);

			lastTime = System.currentTimeMillis();

			if (engine1Whites == b.getTurn()) {
				engine1.go(params);
			} else {
				engine2.go(params);
			}
		}
	}

	public void bestMove(int bestMove, int ponder) {
		long timeThinked = System.currentTimeMillis() - lastTime;
		if (b.getTurn()) {
			wtime -= timeThinked + MOVE_TIME_INC;
		} else {
			btime -= timeThinked + MOVE_TIME_INC;
		}
		b.doMove(bestMove);
		engine1.getBoard().doMove(bestMove);
		engine2.getBoard().doMove(bestMove);
		go();
	}

	public void info(SearchStatusInfo info) {
	}
}