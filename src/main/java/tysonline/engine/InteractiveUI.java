package tysonline.engine;

import java.util.Scanner;

public class InteractiveUI {
    public static void main(String[] args) {
        // Initialize engine components.
        Bitboards.initBitboards();
        Zobrist.initZobrist();
        MoveGen.initMoveGeneration();
        Evaluation.initEvaluation();

        // Set the board to the starting position.
        Board board = new Board();
        Fen.setFen(board, Constants.START_FEN);
        System.out.println("Interactive Chess Game");
        System.out.println("Starting position set. Enter your moves in coordinate notation (e.g., e2e4).");
        System.out.println("Type 'quit' to exit.");
        
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Prompt for your move.
            System.out.print("Your move: ");
            String userMove = scanner.nextLine().trim();
            if (userMove.equalsIgnoreCase("quit")) {
                break;
            }
            
            // Update the board with your move.
            try {
                San.pushSan(board, userMove);
            } catch (Exception e) {
                System.out.println("Invalid move format. Please try again.");
                continue;
            }
            
            // Let the engine calculate its move.
            System.out.println("Engine is thinking...");
            int depth = 7; // Adjust search depth as desired.
            int eval = Search.search(board, depth);
            String engineMove = San.moveToSan(Search.SEARCH_BEST_MOVE);
            System.out.printf("Engine plays: %s (eval: %d)\n", engineMove, eval);
            
            // Update the board with the engine's move.
            San.pushSan(board, engineMove);
        }
        scanner.close();
        System.out.println("Game over.");
    }
}
