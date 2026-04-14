package tysonline.engine;

public class San {
    // Use conventional square names: 0 = a1, 1 = b1, …, 7 = h1, 8 = a2, …, 63 = h8.
    public static final String[] SQUARE_NAMES = {
        "a1","b1","c1","d1","e1","f1","g1","h1",
        "a2","b2","c2","d2","e2","f2","g2","h2",
        "a3","b3","c3","d3","e3","f3","g3","h3",
        "a4","b4","c4","d4","e4","f4","g4","h4",
        "a5","b5","c5","d5","e5","f5","g5","h5",
        "a6","b6","c6","d6","e6","f6","g6","h6",
        "a7","b7","c7","d7","e7","f7","g7","h7",
        "a8","b8","c8","d8","e8","f8","g8","h8"
    };
    
    public static String moveToSan(Move move) {
        if (move.castle != 0) {
            if (move.castle == Constants.K) return "e1g1";
            else if (move.castle == Constants.Q) return "e1c1";
            else if (move.castle == Constants.k) return "e8g8";
            else if (move.castle == Constants.q) return "e8c8";
        }
        StringBuilder san = new StringBuilder();
        san.append(SQUARE_NAMES[move.fromSquare]);
        san.append(SQUARE_NAMES[move.toSquare]);
        if (move.promotion != Constants.NO_PROMOTION) {
            if (move.promotion == Constants.QUEEN_W || move.promotion == Constants.QUEEN_B)
                san.append("q");
            else if (move.promotion == Constants.ROOK_W || move.promotion == Constants.ROOK_B)
                san.append("r");
            else if (move.promotion == Constants.BISHOP_W || move.promotion == Constants.BISHOP_B)
                san.append("b");
            else if (move.promotion == Constants.KNIGHT_W || move.promotion == Constants.KNIGHT_B)
                san.append("n");
        }
        return san.toString();
    }
    
    public static Move sanToMove(Board board, String san) {
        Move move = new Move();
        int fromFile = san.charAt(0) - 'a';
        int fromRank = Character.getNumericValue(san.charAt(1)) - 1;
        int toFile = san.charAt(2) - 'a';
        int toRank = Character.getNumericValue(san.charAt(3)) - 1;
        move.fromSquare = fromRank * 8 + fromFile;
        move.toSquare = toRank * 8 + toFile;
        move.castle = 0;
        move.promotion = Constants.NO_PROMOTION;
        
        if (move.fromSquare == Constants.E1 && move.toSquare == Constants.G1 && (board.castling & Constants.K) != 0)
            move.castle = Constants.K;
        else if (move.fromSquare == Constants.E1 && move.toSquare == Constants.C1 && (board.castling & Constants.Q) != 0)
            move.castle = Constants.Q;
        else if (move.fromSquare == Constants.E8 && move.toSquare == Constants.G8 && (board.castling & Constants.k) != 0)
            move.castle = Constants.k;
        else if (move.fromSquare == Constants.E8 && move.toSquare == Constants.C8 && (board.castling & Constants.q) != 0)
            move.castle = Constants.q;
        
        if (san.length() > 4) {
            char promo = san.charAt(4);
            if (promo == 'q') move.promotion = (board.turn == Constants.WHITE) ? Constants.QUEEN_W : Constants.QUEEN_B;
            else if (promo == 'r') move.promotion = (board.turn == Constants.WHITE) ? Constants.ROOK_W : Constants.ROOK_B;
            else if (promo == 'b') move.promotion = (board.turn == Constants.WHITE) ? Constants.BISHOP_W : Constants.BISHOP_B;
            else if (promo == 'n') move.promotion = (board.turn == Constants.WHITE) ? Constants.KNIGHT_W : Constants.KNIGHT_B;
        }
        
        // Determine piece type by checking which piece occupies the from-square.
        long mask = Bitboards.SQUARE_BITBOARDS[move.fromSquare];
        if ((board.pawnW & mask) != 0) move.pieceType = Constants.PAWN_W;
        else if ((board.knightW & mask) != 0) move.pieceType = Constants.KNIGHT_W;
        else if ((board.bishopW & mask) != 0) move.pieceType = Constants.BISHOP_W;
        else if ((board.rookW & mask) != 0) move.pieceType = Constants.ROOK_W;
        else if ((board.queenW & mask) != 0) move.pieceType = Constants.QUEEN_W;
        else if ((board.kingW & mask) != 0) move.pieceType = Constants.KING_W;
        else if ((board.pawnB & mask) != 0) move.pieceType = Constants.PAWN_B;
        else if ((board.knightB & mask) != 0) move.pieceType = Constants.KNIGHT_B;
        else if ((board.bishopB & mask) != 0) move.pieceType = Constants.BISHOP_B;
        else if ((board.rookB & mask) != 0) move.pieceType = Constants.ROOK_B;
        else if ((board.queenB & mask) != 0) move.pieceType = Constants.QUEEN_B;
        else if ((board.kingB & mask) != 0) move.pieceType = Constants.KING_B;
        
        return move;
    }
    
    public static void pushSan(Board board, String san) {
        Move move = sanToMove(board, san);
        MoveGen.pushMove(board, move);
    }
}
