package tysonline.engine;

public class Move {
    public int fromSquare;
    public int toSquare;
    public int promotion;  // If no promotion then NO_PROMOTION (-1)
    public int castle;     // 0 if not castling; otherwise use Constants.K, Q, k, or q.
    public int validation; // NOT_VALIDATED, LEGAL, or ILLEGAL.
    public int pieceType;  // Which piece is moving (or, for promotions, the promoted piece type)
    public int score;      // Used for move ordering.
    public boolean exhausted; // Flag for move ordering.

    public Move() {
        fromSquare = toSquare = 0;
        promotion = Constants.NO_PROMOTION;
        castle = 0;
        validation = Constants.NOT_VALIDATED;
        pieceType = 0;
        score = 0;
        exhausted = false;
    }
    
    public Move(int fromSquare, int toSquare, int promotion, int castle, int pieceType) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.promotion = promotion;
        this.castle = castle;
        this.pieceType = pieceType;
        this.validation = Constants.NOT_VALIDATED;
        this.score = 0;
        this.exhausted = false;
    }
}
