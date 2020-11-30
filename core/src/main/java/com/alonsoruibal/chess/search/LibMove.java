package com.alonsoruibal.chess.search;

import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

public class LibMove extends Move{
    public LibMove(Square from, Square to){
        super(from, to);
    }
    public LibMove(Move move){
            super(move.getFrom(), move.getTo(), move.getPromotion());
    }
}
