package tysonline.engine;

public class TT {
    public static final int TT_SIZE = 100000;
    public static TTEntry[] TT_TABLE = new TTEntry[TT_SIZE];
    
    static {
        for (int i = 0; i < TT_SIZE; i++) {
            TT_TABLE[i] = new TTEntry();
        }
    }
    
    public static TTEntry getTTEntry(long zobrist) {
        int index = (int) Math.floorMod(zobrist, TT_SIZE);
        return TT_TABLE[index];
    }
    
    public static void addTTEntry(Board board, int eval, Move move, int depth, int beta, int alpha) {
        TTEntry entry = new TTEntry();
        if (eval <= alpha)
            entry.nodeType = TTEntry.UPPER;
        else if (eval >= beta)
            entry.nodeType = TTEntry.LOWER;
        else 
            entry.nodeType = TTEntry.EXACT;
        entry.eval = eval;
        entry.move = move;
        entry.depth = depth;
        entry.zobrist = board.hash;
        
        int index = (int) Math.floorMod(entry.zobrist, TT_SIZE);
        TT_TABLE[index] = entry;
    }
}
