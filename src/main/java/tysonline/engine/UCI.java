package tysonline.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UCI {
    public static String AUTHOR = "Tyson Line";
    public static String ENGINE_NAME = "TysonChess";
    
    public static void main(String[] args) throws Exception {
        // Initialize engine components.
        Bitboards.initBitboards();
        Zobrist.initZobrist();
        MoveGen.initMoveGeneration();
        Evaluation.initEvaluation();
        
        // Set I/O buffering.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Board board = new Board();
        Fen.setFen(board, Constants.START_FEN);
        
        String input;
        while ((input = br.readLine()) != null) {
            input = input.trim();
            if (input.isEmpty()) continue;
            if (input.startsWith("isready")) {
                System.out.println("readyok");
                continue;
            } else if (input.startsWith("position")) {
                parsePosition(input, board);
            } else if (input.startsWith("ucinewgame")) {
                Fen.setFen(board, Constants.START_FEN);
            } else if (input.startsWith("go")) {
                getBestMove(board);
            } else if (input.startsWith("quit")) {
                break;
            } else if (input.startsWith("uci")) {
                printEngineInfo();
            }
        }
    }
    
    public static void parsePosition(String command, Board board) {
        if (command.contains("fen")) {
            int fenIndex = command.indexOf("fen") + 4;
            String fenPart = command.substring(fenIndex).trim();
            int movesIndex = fenPart.indexOf("moves");
            String fen;
            if (movesIndex != -1) {
                fen = fenPart.substring(0, movesIndex).trim();
            } else {
                fen = fenPart;
            }
            Fen.setFen(board, fen);
            if (movesIndex != -1) {
                String movesStr = fenPart.substring(movesIndex + 6).trim();
                for (String san : movesStr.split("\\s+")) {
                    San.pushSan(board, san);
                }
            }
        } else if (command.contains("startpos")) {
            Fen.setFen(board, Constants.START_FEN);
            int movesIndex = command.indexOf("moves");
            if (movesIndex != -1) {
                String movesStr = command.substring(movesIndex + 6).trim();
                for (String san : movesStr.split("\\s+")) {
                    San.pushSan(board, san);
                }
            }
        }
    }
    
    public static void getBestMove(Board board) {
        int depth = 7;
        long start = System.currentTimeMillis();
        int eval = Search.search(board, depth);
        long end = System.currentTimeMillis();
        long timeSpent = end - start;
        System.out.printf("info depth %d time %d nodes %d score cp %d\n", depth, timeSpent, Search.SEARCH_NODES_SEARCHED, eval);
        String bestSan = San.moveToSan(Search.SEARCH_BEST_MOVE);
        System.out.println("bestmove " + bestSan);
    }
    
    public static void printEngineInfo() {
        System.out.println("id name " + ENGINE_NAME);
        System.out.println("id author " + AUTHOR);
        System.out.println("uciok");
    }
}
