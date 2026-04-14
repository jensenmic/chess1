package tysonline.engine;

public class Board {
    // White piece bitboards
    public long pawnW;
    public long knightW;
    public long bishopW;
    public long rookW;
    public long queenW;
    public long kingW;
    // Black piece bitboards
    public long pawnB;
    public long knightB;
    public long bishopB;
    public long rookB;
    public long queenB;
    public long kingB;
    
    public int turn;       // WHITE or BLACK
    public int castling;   // Bitmask of castling rights
    public int epSquare;   // En passant target square (or -1 if none)
    public int halfmoves;
    public int fullmoves;
    public int whiteKingSq;
    public int blackKingSq;
    
    public long occupancy;
    public long occupancyWhite;
    public long occupancyBlack;
    
    public long hash;      // Zobrist hash of the board
    public long attacks;   // Attack mask computed for the opponent
    
    public Board() {
        pawnW = knightW = bishopW = rookW = queenW = kingW = 0;
        pawnB = knightB = bishopB = rookB = queenB = kingB = 0;
        turn = Constants.WHITE;
        castling = 0;
        epSquare = -1;
        halfmoves = fullmoves = 0;
        whiteKingSq = blackKingSq = 0;
        occupancy = occupancyWhite = occupancyBlack = 0;
        hash = 0;
        attacks = 0;
    }
    
    public void computeOccupancyMasks() {
        occupancyWhite = pawnW | knightW | bishopW | rookW | queenW | kingW;
        occupancyBlack = pawnB | knightB | bishopB | rookB | queenB | kingB;
        occupancy = occupancyWhite | occupancyBlack;
    }
}
