package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.log.Logger;

import java.util.ArrayList;

public class SearchParameters {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("SearchParameters");

	// UCI parameters
	// List of moves to search, if null search all moves
	ArrayList<Integer> searchMoves;

	// Remaining time
	private int wtime;
	private int btime;
	// Time increment per move
	private int winc;
	private int binc;
	// Moves to the next time control
	private int movesToGo;
	// Analize x plyes only
	private int depth = Integer.MAX_VALUE;
	// Search only this number of nodes
	private int nodes = Integer.MAX_VALUE;
	// Search for mate in mate moves
	private int mate;
	// Search movetime milliseconds
	private int moveTime = Integer.MAX_VALUE;
	// Think infinite
	private boolean infinite;
	private boolean ponder;

	private boolean manageTime;

	public void clearSearchMoves() {
		if (searchMoves == null) {
			searchMoves = new ArrayList<>();
		}
		searchMoves.clear();
	}

	public void addSearchMove(int move) {
		if (searchMoves == null) {
			searchMoves = new ArrayList<>();
		}
		searchMoves.add(move);
	}

	public boolean isPonder() {
		return ponder;
	}

	public void setPonder(boolean ponder) {
		this.ponder = ponder;
	}

	public int getWtime() {
		return wtime;
	}

	public void setWtime(int wtime) {
		this.wtime = wtime;
	}

	public int getBtime() {
		return btime;
	}

	public void setBtime(int btime) {
		this.btime = btime;
	}

	public int getWinc() {
		return winc;
	}

	public void setWinc(int winc) {
		this.winc = winc;
	}

	public int getBinc() {
		return binc;
	}

	public void setBinc(int binc) {
		this.binc = binc;
	}

	public int getMovesToGo() {
		return movesToGo;
	}

	public void setMovesToGo(int movesToGo) {
		this.movesToGo = movesToGo;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getNodes() {
		return nodes;
	}

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	public int getMate() {
		return mate;
	}

	public void setMate(int mate) {
		this.mate = mate;
	}

	public int getMoveTime() {
		return moveTime;
	}

	public void setMoveTime(int moveTime) {
		this.moveTime = moveTime;
	}

	public boolean isInfinite() {
		return infinite;
	}

	public void setInfinite(boolean infinite) {
		this.infinite = infinite;
	}

	/**
	 * Used to detect if it can add more time in case of panic or apply other heuristics to reduce time
	 *
	 * @return true if the engine is responsible of managing the remaining time
	 */
	public boolean manageTime() {
		return manageTime;
	}

	/**
	 * Time management routine
	 * @param panicTime is set to true when the score fails low in the root node by 100
	 *
	 * @return the time to think, or Long.MAX_VALUE if it can think an infinite time
	 */
	public long calculateMoveTime(boolean engineIsWhite, long startTime, boolean panicTime) {
		manageTime = false;
		if (ponder || infinite || depth < Integer.MAX_VALUE || nodes < Integer.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		if (moveTime != Integer.MAX_VALUE) {
			return startTime + moveTime;
		}
		manageTime = true;

		int calcTime = 0;
		int timeAvailable = engineIsWhite ? wtime : btime;
		int timeInc = engineIsWhite ? winc : binc;
		if (timeAvailable > 0) {
			calcTime = timeAvailable / (movesToGo != 0 ? movesToGo : 25);
		}
		if (panicTime) { // x 4
			calcTime = calcTime << 2;
		}
		calcTime = Math.min(calcTime, timeAvailable >>> 3); // Never consume more than time / 8
		calcTime += timeInc;

		logger.debug("Thinking for " + calcTime + "Ms");
		return startTime + calcTime;
	}

	public static SearchParameters get(int moveTime) {
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setMoveTime(moveTime);
		return searchParameters;
	}
}
