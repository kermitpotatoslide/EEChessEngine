package com.alonsoruibal.chess.uci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.search.WorseSearchEngine;

public class DataGenerator {

    int margin = 800;
    Config config;
    WorseSearchEngine engine;

    public DataGenerator() {
        

    }

    public void writeResults(int eval, long leafNodes, long eFPruneCount) throws IOException {
        File outPutFile = new File("D:\\Other Coding\\Extended Essay\\Results_" + margin + ".txt");
        FileWriter fWriter = new FileWriter(outPutFile, true);
        BufferedWriter writer = new BufferedWriter(fWriter);

        writer.write(Integer.toString(eval) + " " + Long.toString(leafNodes) + " " + Long.toString(eFPruneCount) + "\n");
        writer.close();
        fWriter.close();
        System.out.println("Written");
    }

    public void setUp(String fen) {
        config = new Config();
        engine = new WorseSearchEngine(config);

        Board tempBoard = new Board();
        tempBoard.setFen(fen);

        engine.getBoard().setFen(tempBoard.getInitialFen());
        engine.getBoard().doMoves(tempBoard.getMoves());

        engine.reset();
    }

    // does search and writes results
    public void doSearch() {

        int eval = engine.go(margin, margin == 0);

        try {
            writeResults(eval, engine.getEvaluated(), engine.getEFPruneCount());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        DataGenerator generator = new DataGenerator();

        //read file
        FileReader fReader = new FileReader("D:\\Other Coding\\Extended Essay\\Lichess PGN\\completed.txt");
        BufferedReader reader = new BufferedReader(fReader);
        
        int i=0;

        //loop, saving to file each time
        for (; i<61; i++){
            //get eval and leafnode count
            String fen = reader.readLine();
            generator.setUp(fen);
            generator.doSearch();
        }
        
    }
}
