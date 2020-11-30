package com.alonsoruibal.chess.search;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.bitboard.BitboardUtils;
import com.alonsoruibal.chess.evaluation.CompleteEvaluator;
import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;
import com.alonsoruibal.chess.evaluation.SimplifiedEvaluator;
import com.alonsoruibal.chess.log.Logger;
import com.alonsoruibal.chess.tt.TranspositionTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.alonsoruibal.chess.search.ChessLibBoardWrapper;

public class WorseSearchEngine{

    private int depth = 8;//20; //maybe change if need less testing time

    Config config;
    SearchObserver observer;
    Board board;
    ChessLibBoardWrapper libBoard;

    private CompleteEvaluator completeEvaluator;
    private SimplifiedEvaluator simpleEvaluator;
    public boolean isSearching = false;

    int globalBestMove;
    boolean searching = false, initialized = false;
    int fMargin;
    int exFMargin;
    int initialPly;
    WorseNode[] nodes;
    boolean control = false;

    long evaluated = 0;
    long eFPruneCount =0;

    public long getEvaluated(){
        return evaluated;
    }

    public long getEFPruneCount(){
        return eFPruneCount;
    }

    public void reset(){
        evaluated= 0;
        eFPruneCount = 0;
    }
    
    public void setMargin(int margin){
        exFMargin = margin;
    }

    public WorseSearchEngine(Config config){
        //haha imagine listening to the config
        this.config = config;
        nodes = new WorseNode[depth];
        completeEvaluator = new CompleteEvaluator();
        simpleEvaluator = new SimplifiedEvaluator();
        board = new Board();
        libBoard = new ChessLibBoardWrapper();
        //static futility margin = 3 pawns
        fMargin = SimplifiedEvaluator.getPawn()*3;
    }

    public void clear(){
        for (int i = 0; i < depth; i++) {
			nodes[i].clear();
        }
    }

    public void setControl(boolean control){
        this.control =  control;
    }

    //used to call board and apply position to it
    public Board getBoard(){
        return board;
    }

    public void setObserver(SearchObserver observer){
        this.observer = observer;
    }

    public int go(int eFMargin, boolean control){
        libBoard.loadFromFen(board.getFen());

        for (int i=0; i<depth; i++){
            nodes[i] = new WorseNode(this, i);
        }

        //pawn value in SimplifiedEvaluator is 100 points
        this.exFMargin = eFMargin;
        this.control = control;
        initialPly = board.getMoveNumber();


        isSearching = true;
        int results = search(depth-1, Integer.MIN_VALUE+1, Integer.MAX_VALUE-1);
        System.out.println("Position Value: " + results);
        isSearching= false;
        return results;
    }

    private int search(int depth, int alpha, int beta){
        assert depth >= 0 : "Wrong depth";

        int distanceToInitialPly = board.getMoveNumber() - initialPly;
        WorseNode node = nodes[distanceToInitialPly];

        //check if board is a draw position
        if (board.isDraw()) {
            evaluated++;
			return evaluateDraw(distanceToInitialPly);
        }
        //check if checkmate
        if (board.isMate()){
            evaluated++;
            return -Evaluator.MATE;
        }

        

        
        if (depth==0){
            evaluated++;
            return evaluate(node);
            
        }

        //node.moveIterator.genMoves();
        node.generateMoves();

        for (LibMove move : node.moves){
            /*System.out.println("Do " + Move.toString(node.move) + " Piece: " 
                + Move.getPieceMoved(node.move) + " turn: " + board.getTurn() + " depth " + depth + " type " + Move.getMoveType(node.move));
            System.out.println(board.toString());*/
           
            //do move
            makeMove(move);

            //board.doMove(node.move);
            //assert board.getCheck() == Move.isCheck(node.move) : "Check flag not generated properly";
            
            if (canPrune(move)){
                //futility prune
                if (depth ==1){
                    //simple eval
                    int sEval = simpleEvaluate(node);
                    //check pass alpha
                    if ((sEval + fMargin) <= alpha){
                        undoMove();
                        return alpha;
                    }
                }

                //ext futility prune
                if (depth ==2 && !control){
                    //simpleeval
                    int sEval = simpleEvaluate(node);
                    //check to see if passes margin
                    if ((sEval + exFMargin) <= alpha){
                        undoMove();
                        eFPruneCount++;
                        return alpha;
                    }
                } 
            }
            
            
            int eval = -search(depth-1, -beta, -alpha);

            undoMove();

            //beta cutoff
            if (eval >= beta){
                //System.out.println("Snip Eval:" + eval + " beta: " + beta);
                return beta;
            }
            //set alpha to highest
            alpha = Math.max(eval, alpha);
        }
        
        return alpha;
    }
    void makeMove(LibMove move){
        libBoard.doMove(move);
        board.doMove(Move.getFromString(board, move.toString(), true));
    }

    void undoMove(){
        libBoard.undoMove();
        board.undoMove();
    }

    int evaluate(WorseNode node){
        return board.getTurn() ? completeEvaluator.evaluate(board, node.attacksInfo) : -completeEvaluator.evaluate(board, node.attacksInfo);
    }
    int simpleEvaluate(WorseNode node){
        return board.getTurn() ? simpleEvaluator.evaluate(board, node.attacksInfo) : -simpleEvaluator.evaluate(board, node.attacksInfo);
    }

    //check to see if can Extended Futility prune and Futility prune
    private boolean canPrune(LibMove move){

        int intMove = Move.getFromString(board, move.toString(), true);
        //avoid checks or promotions 
        return !Move.isCheck(intMove) && !Move.isPromotion(intMove) && !Move.isCapture(intMove);
    }

    private int evaluateDraw(int distanceToInitialPly) {
		int nonPawnMat = Long.bitCount(board.knights) * Evaluator.KNIGHT +
				Long.bitCount(board.bishops) * Evaluator.BISHOP +
				Long.bitCount(board.rooks) * Evaluator.ROOK +
				Long.bitCount(board.queens) * Evaluator.QUEEN;
		int gamePhase = nonPawnMat >= Evaluator.NON_PAWN_MATERIAL_MIDGAME_MAX ? Evaluator.GAME_PHASE_MIDGAME :
				nonPawnMat <= Evaluator.NON_PAWN_MATERIAL_ENDGAME_MIN ? Evaluator.GAME_PHASE_ENDGAME :
						((nonPawnMat - Evaluator.NON_PAWN_MATERIAL_ENDGAME_MIN) * Evaluator.GAME_PHASE_MIDGAME) / (Evaluator.NON_PAWN_MATERIAL_MIDGAME_MAX - Evaluator.NON_PAWN_MATERIAL_ENDGAME_MIN);

		return ((distanceToInitialPly & 1) == 0 ? -config.contemptFactor : config.contemptFactor) * gamePhase / Evaluator.GAME_PHASE_MIDGAME;
	}





}