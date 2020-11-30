package com.alonsoruibal.chess.search;

import java.util.LinkedList;

import com.alonsoruibal.chess.bitboard.AttacksInfo;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

/**
 * Stores the elements to be kept in each node of the search tree
 * <p>
 * Other nodes may access this elements
 */
public class WorseNode {
	public final int distanceToInitialPly;

	WorseSearchEngine searchEngine;

	// Current move
	public int move;

	// The static node eval
	public int staticEval;

	// The Move iterator
	public AttacksInfo attacksInfo;
	public WorseMoveIterator moveIterator;

	// killer moves
	public int killerMove1;
	public int killerMove2;
	public int killerMove3;
	public int killerMove4;

	public int ttMove;


	LinkedList <LibMove> moves;

	
	public WorseNode(WorseSearchEngine searchEngine, int distanceToInitialPly) {
		this.distanceToInitialPly = distanceToInitialPly;

		this.searchEngine = searchEngine;

		attacksInfo = new AttacksInfo();
		moveIterator = new WorseMoveIterator(searchEngine, attacksInfo, distanceToInitialPly);

		clear();
	}

	public void clear() {
		staticEval = Evaluator.NO_VALUE;
	}

	public void destroy() {
		moveIterator.destroy();
		moveIterator = null;
		attacksInfo = null;
	}

	public void generateMoves() {
		try {
			MoveList moveList = MoveGenerator.generateLegalMoves(searchEngine.libBoard);
			moves = new LinkedList<LibMove>();
			for (Move move : moveList){
				moves.add(new LibMove(move));
			}
		} catch (MoveGeneratorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}