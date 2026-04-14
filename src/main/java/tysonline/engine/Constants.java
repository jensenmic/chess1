package tysonline.engine;

public class Constants {
    // Side constants
    public static final int WHITE = 1;
    public static final int BLACK = 0;
    
    // Piece type constants
    public static final int PAWN_W   = 0;
    public static final int KNIGHT_W = 1;
    public static final int BISHOP_W = 2;
    public static final int ROOK_W   = 3;
    public static final int QUEEN_W  = 4;
    public static final int KING_W   = 5;
    public static final int PAWN_B   = 6;
    public static final int KNIGHT_B = 7;
    public static final int BISHOP_B = 8;
    public static final int ROOK_B   = 9;
    public static final int QUEEN_B  = 10;
    public static final int KING_B   = 11;
    
    // Evaluation limits
    public static final int MIN_EVAL = -100000;
    public static final int MAX_EVAL =  100000;
    
    // Move validation status
    public static final int NOT_VALIDATED = 0;
    public static final int LEGAL         = 1;
    public static final int ILLEGAL       = 2;
    
    // Castling flags (bit–masks)
    public static final int K = 1;  // White king–side
    public static final int Q = 2;  // White queen–side
    public static final int k = 4;  // Black king–side
    public static final int q = 8;  // Black queen–side
    
    // For updating castling rights
    public static final int ALL_CASTLE_W = 0b0011; // only white castling rights remain
    public static final int ALL_CASTLE_B = 0b1100; // only black castling rights remain
    
    // Promotion indicator
    public static final int NO_PROMOTION = -1;
    
    // Game result
    public static final int UN_DETERMINED = 0;
    public static final int WHITE_WIN       = 1;
    public static final int BLACK_WIN       = 2;
    public static final int DRAW            = 3;
    
    // Starting FEN string
    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    
    // Square numbering convention:
    // We use conventional algebraic notation with 0 = a1, 1 = b1, …, 7 = h1,
    // 8 = a2, …, 63 = h8.
    public static final int A1 = 0;
    public static final int B1 = 1;
    public static final int C1 = 2;
    public static final int D1 = 3;
    public static final int E1 = 4;
    public static final int F1 = 5;
    public static final int G1 = 6;
    public static final int H1 = 7;
    
    public static final int A2 = 8;
    public static final int B2 = 9;
    public static final int C2 = 10;
    public static final int D2 = 11;
    public static final int E2 = 12;
    public static final int F2 = 13;
    public static final int G2 = 14;
    public static final int H2 = 15;
    
    public static final int A3 = 16;
    public static final int B3 = 17;
    public static final int C3 = 18;
    public static final int D3 = 19;
    public static final int E3 = 20;
    public static final int F3 = 21;
    public static final int G3 = 22;
    public static final int H3 = 23;
    
    public static final int A4 = 24;
    public static final int B4 = 25;
    public static final int C4 = 26;
    public static final int D4 = 27;
    public static final int E4 = 28;
    public static final int F4 = 29;
    public static final int G4 = 30;
    public static final int H4 = 31;
    
    public static final int A5 = 32;
    public static final int B5 = 33;
    public static final int C5 = 34;
    public static final int D5 = 35;
    public static final int E5 = 36;
    public static final int F5 = 37;
    public static final int G5 = 38;
    public static final int H5 = 39;
    
    public static final int A6 = 40;
    public static final int B6 = 41;
    public static final int C6 = 42;
    public static final int D6 = 43;
    public static final int E6 = 44;
    public static final int F6 = 45;
    public static final int G6 = 46;
    public static final int H6 = 47;
    
    public static final int A7 = 48;
    public static final int B7 = 49;
    public static final int C7 = 50;
    public static final int D7 = 51;
    public static final int E7 = 52;
    public static final int F7 = 53;
    public static final int G7 = 54;
    public static final int H7 = 55;
    
    public static final int A8 = 56;
    public static final int B8 = 57;
    public static final int C8 = 58;
    public static final int D8 = 59;
    public static final int E8 = 60;
    public static final int F8 = 61;
    public static final int G8 = 62;
    public static final int H8 = 63;
    
    // Castling paths (bitboard masks) – these depend on our board representation.
    // Here we build masks manually (using our SQUARE_BITBOARDS from Bitboards).
    // For white king–side castling, squares between E1 and H1: f1 and g1.
    public static final long WHITE_CASTLE_K_PATH = (1L << F1) | (1L << G1);
    // For white queen–side castling, squares between A1 and E1: b1, c1, and d1.
    public static final long WHITE_CASTLE_Q_PATH = (1L << B1) | (1L << C1) | (1L << D1);
    // For black king–side castling, squares between E8 and H8: f8 and g8.
    public static final long BLACK_CASTLE_K_PATH = (1L << F8) | (1L << G8);
    // For black queen–side castling, squares between A8 and E8: b8, c8, and d8.
    public static final long BLACK_CASTLE_Q_PATH = (1L << B8) | (1L << C8) | (1L << D8);
}
