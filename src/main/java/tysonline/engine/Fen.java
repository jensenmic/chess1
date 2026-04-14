package tysonline.engine;

public class Fen {
    public static void setFen(Board board, String fen) {
        // Reset all piece bitboards
        board.pawnW = board.knightW = board.bishopW = board.rookW = board.queenW = board.kingW = 0;
        board.pawnB = board.knightB = board.bishopB = board.rookB = board.queenB = board.kingB = 0;
        board.turn = Constants.WHITE;
        board.castling = 0;
        board.epSquare = -1;
        
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 4) return;
        
        // Piece placement
        String placement = parts[0];
        String[] ranks = placement.split("/");
        int rank = 7;
        for (String rankStr : ranks) {
            int file = 0;
            for (char c : rankStr.toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    int square = rank * 8 + file;
                    switch (c) {
                        case 'P': board.pawnW |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'N': board.knightW |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'B': board.bishopW |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'R': board.rookW |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'Q': board.queenW |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'K': 
                            board.kingW |= Bitboards.SQUARE_BITBOARDS[square]; 
                            board.whiteKingSq = square;
                            break;
                        case 'p': board.pawnB |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'n': board.knightB |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'b': board.bishopB |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'r': board.rookB |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'q': board.queenB |= Bitboards.SQUARE_BITBOARDS[square]; break;
                        case 'k': 
                            board.kingB |= Bitboards.SQUARE_BITBOARDS[square]; 
                            board.blackKingSq = square;
                            break;
                    }
                    file++;
                }
            }
            rank--;
        }
        // Active color
        board.turn = parts[1].equals("w") ? Constants.WHITE : Constants.BLACK;
        // Castling rights
        String castlingStr = parts[2];
        if (!castlingStr.equals("-")) {
            for (char c : castlingStr.toCharArray()) {
                switch (c) {
                    case 'K': board.castling |= Constants.K; break;
                    case 'Q': board.castling |= Constants.Q; break;
                    case 'k': board.castling |= Constants.k; break;
                    case 'q': board.castling |= Constants.q; break;
                }
            }
        }
        // En passant
        String ep = parts[3];
        if (!ep.equals("-")) {
            int file = ep.charAt(0) - 'a';
            int rankNum = ep.charAt(1) - '1';
            board.epSquare = rankNum * 8 + file;
        }
        board.computeOccupancyMasks();
        board.hash = Zobrist.hash(board);
    }
}
