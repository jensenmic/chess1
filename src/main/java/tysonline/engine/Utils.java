package tysonline.engine;

public class Utils {
    public static void printMoves(java.util.List<Move> moves) {
        System.out.println("Moves: " + moves.size());
        for (Move move : moves) {
            String san = San.moveToSan(move);
            System.out.println(san);
        }
    }
    
    public static void printBitboard(long bb) {
        System.out.println();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int loc = 63 - ((y * 8) + x);
                long bit = bb & (1L << loc);
                System.out.print((bit != 0 ? "x" : ".") + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public static void printBits(long bb) {
        for (int i = 0; i < 64; i++) {
            long bit = bb & (1L << (63 - i));
            System.out.print(bit != 0 ? "1" : "0");
        }
        System.out.println();
    }
}
