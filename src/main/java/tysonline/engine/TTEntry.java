package tysonline.engine;

public class TTEntry {
    public long zobrist;
    public int eval;
    public int nodeType;
    public int depth;
    public Move move;
    
    public static final int EXACT = 0;
    public static final int LOWER = 1;
    public static final int UPPER = 2;
}
