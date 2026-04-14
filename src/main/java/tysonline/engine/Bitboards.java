package tysonline.engine;

public class Bitboards {
    public static long[] SQUARE_BITBOARDS = new long[64];
    
    public static void initBitboards() {
        // Our convention: 0 = a1, 1 = b1, …, 7 = h1, 8 = a2, …, 63 = h8.
        for (int i = 0; i < 64; i++) {
            SQUARE_BITBOARDS[i] = 1L << i;
        }
    }
}
