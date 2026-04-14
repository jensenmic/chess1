package tysonline.engine;

import java.util.Random;

public class Zobrist {
    public static long[][] PIECES = new long[12][64];
    public static long[] EN_PASSANT = new long[64];
    public static long[] CASTLING = new long[16];
    public static long WHITE_TO_MOVE;
    
    public static void initZobrist() {
        Random rand = new Random();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 64; j++) {
                PIECES[i][j] = randomBitboard(rand);
            }
        }
        for (int i = 0; i < 64; i++) {
            EN_PASSANT[i] = randomBitboard(rand);
        }
        for (int i = 0; i < 16; i++) {
            CASTLING[i] = randomBitboard(rand);
        }
        WHITE_TO_MOVE = randomBitboard(rand);
    }
    
    private static long randomBitboard(Random rand) {
        long r = 0;
        for (int i = 0; i < 64; i++) {
            long bit = rand.nextInt(2);
            r |= bit << i;
        }
        return r;
    }
    
    public static long hash(Board board) {
        long hash = 0;
        hash ^= CASTLING[board.castling];
        // Pieces
        for (int i = 0; i < 12; i++) {
            long bb = 0;
            switch(i) {
                case 0: bb = board.pawnW; break;
                case 1: bb = board.knightW; break;
                case 2: bb = board.bishopW; break;
                case 3: bb = board.rookW; break;
                case 4: bb = board.queenW; break;
                case 5: bb = board.kingW; break;
                case 6: bb = board.pawnB; break;
                case 7: bb = board.knightB; break;
                case 8: bb = board.bishopB; break;
                case 9: bb = board.rookB; break;
                case 10: bb = board.queenB; break;
                case 11: bb = board.kingB; break;
            }
            while (bb != 0) {
                int square = Long.numberOfTrailingZeros(bb);
                hash ^= PIECES[i][square];
                bb &= bb - 1;
            }
        }
        if (board.epSquare != -1) {
            hash ^= EN_PASSANT[board.epSquare];
        }
        if (board.turn == Constants.WHITE) {
            hash ^= WHITE_TO_MOVE;
        }
        return hash;
    }
}
