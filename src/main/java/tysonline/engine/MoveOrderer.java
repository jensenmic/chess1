package tysonline.engine;

import java.util.List;

public class MoveOrderer {
    public static final int[] VICTIMS = {
        100 * 16, 320 * 16, 330 * 16, 500 * 16, 900 * 16
    };
    public static final int[] ATTACKERS = {
        100, 200, 300, 400, 500, 600, 100, 200, 300, 400, 500, 600
    };
    public static final int MAX_MOVE_SCORE = 100000;
    public static final int MVV_LVA_PAWN_EXCHANGE = (100 * 16) - 100;
    
    public static void scoreMoves(Board board, TTEntry entry, List<Move> moves) {
        Move pvMove = null;
        if (board.hash == entry.zobrist && entry.nodeType < TTEntry.UPPER) {
            pvMove = entry.move;
        }
        
        for (Move move : moves) {
            if (pvMove != null && movesAreEqual(pvMove, move)) {
                move.score = MAX_MOVE_SCORE;
            } else {
                long enemyOcc = (board.turn == Constants.WHITE) ? board.occupancyBlack : board.occupancyWhite;
                boolean isCapture = (enemyOcc & Bitboards.SQUARE_BITBOARDS[move.toSquare]) != 0;
                boolean isEnPassant = board.epSquare == move.toSquare;
                if (isEnPassant) {
                    move.score = MVV_LVA_PAWN_EXCHANGE;
                } else if (isCapture) {
                    int capturedPiece = -1;
                    long toSquare = Bitboards.SQUARE_BITBOARDS[move.toSquare];
                    long[] bb;
                    if (board.turn == Constants.WHITE) {
                        bb = new long[] { board.pawnB, board.knightB, board.bishopB, board.rookB, board.queenB, board.kingB };
                    } else {
                        bb = new long[] { board.pawnW, board.knightW, board.bishopW, board.rookW, board.queenW, board.kingW };
                    }
                    
                    for (int i = 0; i < 5; i++) {
                        if ((bb[i] & toSquare) != 0) {
                            capturedPiece = i;
                            break;
                        }
                    }
                    if (capturedPiece != -1) {
                        move.score = VICTIMS[capturedPiece] - ATTACKERS[move.pieceType];
                    }
                }
            }
        }
    }
    
    public static int selectMove(List<Move> moves) {
        int bestScore = 0;
        int index = -1;
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            if (!move.exhausted && move.score >= bestScore) {
                bestScore = move.score;
                index = i;
            }
        }
        if (index != -1) {
            moves.get(index).exhausted = true;
        }
        return index;
    }
    
    public static boolean movesAreEqual(Move a, Move b) {
        return a.fromSquare == b.fromSquare && a.toSquare == b.toSquare && a.promotion == b.promotion;
    }
}
