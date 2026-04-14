package tysonline.engine;

import java.util.List;

public class Evaluation {
    public static final int[] PIECE_VALUES = {
        100, 320, 330, 500, 900, 2000,
       -100,-320,-330,-500,-900,-2000
    };
    
    public static int[] PAWN_W_PST = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10,-20,-20, 10, 10,  5,
         5, -5,-10,  0,  0,-10, -5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5,  5, 10, 25, 25, 10,  5,  5,
         10, 10, 20, 30, 30, 20, 10, 10,
         50, 50, 50, 50, 50, 50, 50, 50,
         0,  0,  0,  0,  0,  0,  0,  0
    };
    public static int[] KNIGHT_W_PST = {
       -50,-40,-30,-30,-30,-30,-40,-50,
       -40,-20,  0,  5,  5,  0,-20,-40,
       -30,  5, 10, 15, 15, 10,  5,-30,
       -30,  0, 15, 20, 20, 15,  0,-30,
       -30,  5, 15, 20, 20, 15,  5,-30,
       -30,  0, 10, 15, 15, 10,  0,-30,
       -40,-20,  0,  0,  0,  0,-20,-40,
       -50,-40,-30,-30,-30,-30,-40,-50
    };
    public static int[] BISHOP_W_PST = {
       -20,-10,-10,-10,-10,-10,-10,-20,
       -10,  5,  0,  0,  0,  0,  5,-10,
       -10, 10, 10, 10, 10, 10, 10,-10,
       -10,  0, 10, 10, 10, 10,  0,-10,
       -10,  5,  5, 10, 10,  5,  5,-10,
       -10,  0,  5, 10, 10,  5,  0,-10,
       -10,  0,  0,  0,  0,  0,  0,-10,
       -20,-10,-10,-10,-10,-10,-10,-20
    };
    public static int[] ROOK_W_PST = {
         0,  0,  5, 10, 10,  5,  0,  0,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         5, 10, 10, 10, 10, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };
    public static int[] QUEEN_W_PST = {
       -20,-10,-10, -5, -5,-10,-10,-20,
       -10,  0,  5,  0,  0,  0,  0,-10,
       -10,  5,  5,  5,  5,  5,  0,-10,
         0,  0,  5,  5,  5,  5,  0, -5,
        -5,  0,  5,  5,  5,  5,  0, -5,
       -10,  0,  5,  5,  5,  5,  0,-10,
       -10,  0,  0,  0,  0,  0,  0,-10,
       -20,-10,-10, -5, -5,-10,-10,-20,
    };
    public static int[] KING_W_PST = {
         20, 30, 10,  0,  0, 10, 30, 20,
         20, 20,  0,  0,  0,  0, 20, 20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
    };
    
    public static int[] PAWN_B_PST = new int[64];
    public static int[] KNIGHT_B_PST = new int[64];
    public static int[] BISHOP_B_PST = new int[64];
    public static int[] ROOK_B_PST = new int[64];
    public static int[] QUEEN_B_PST = new int[64];
    public static int[] KING_B_PST = new int[64];
    
    public static void initEvaluation() {
        for (int i = 0; i < 64; i++) {
            PAWN_B_PST[i] = PAWN_W_PST[63 - i];
            KNIGHT_B_PST[i] = KNIGHT_W_PST[63 - i];
            BISHOP_B_PST[i] = BISHOP_W_PST[63 - i];
            ROOK_B_PST[i] = ROOK_W_PST[63 - i];
            QUEEN_B_PST[i] = QUEEN_W_PST[63 - i];
            KING_B_PST[i] = KING_W_PST[63 - i];
        }
    }
    
    public static int evaluate(Board board, int result) {
        if (result == Constants.DRAW) return 0;
        else if (result == Constants.WHITE_WIN) return Constants.MAX_EVAL;
        else if (result == Constants.BLACK_WIN) return Constants.MIN_EVAL;
        
        int eval = 0;
        long bb;
        
        bb = board.pawnW;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.PAWN_W] + PAWN_W_PST[sq];
            bb &= bb - 1;
        }
        bb = board.knightW;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.KNIGHT_W] + KNIGHT_W_PST[sq];
            bb &= bb - 1;
        }
        bb = board.bishopW;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.BISHOP_W] + BISHOP_W_PST[sq];
            bb &= bb - 1;
        }
        bb = board.rookW;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.ROOK_W] + ROOK_W_PST[sq];
            bb &= bb - 1;
        }
        bb = board.queenW;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.QUEEN_W] + QUEEN_W_PST[sq];
            bb &= bb - 1;
        }
        
        bb = board.pawnB;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.PAWN_B] - PAWN_B_PST[sq];
            bb &= bb - 1;
        }
        bb = board.knightB;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.KNIGHT_B] - KNIGHT_B_PST[sq];
            bb &= bb - 1;
        }
        bb = board.bishopB;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.BISHOP_B] - BISHOP_B_PST[sq];
            bb &= bb - 1;
        }
        bb = board.rookB;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.ROOK_B] - ROOK_B_PST[sq];
            bb &= bb - 1;
        }
        bb = board.queenB;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            eval += PIECE_VALUES[Constants.QUEEN_B] - QUEEN_B_PST[sq];
            bb &= bb - 1;
        }
        eval += KING_W_PST[board.whiteKingSq];
        eval -= KING_B_PST[board.blackKingSq];
        
        return eval;
    }
    
    // The game result function (for now, we return UN_DETERMINED).
    public static int result(Board board, List<Move> moves) {
        // In a full engine, check for checkmate/stalemate/insufficient material.
        // Here, if no moves are legal, return a mate/stalemate value.
        if (moves.isEmpty()) {
            // Check if king is attacked.
            if (MoveGen.isSquareAttacked(board, board.turn == Constants.WHITE ? board.whiteKingSq : board.blackKingSq))
                return (board.turn == Constants.WHITE) ? Constants.BLACK_WIN : Constants.WHITE_WIN;
            return Constants.DRAW;
        }
        return Constants.UN_DETERMINED;
    }
}
