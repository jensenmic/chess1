package tysonline.engine;

import java.util.ArrayList;
import java.util.List;

public class MoveGen {
    // Constants for pawn starting ranks and castling paths
    public static long PAWN_START_WHITE = 0xFF00L;          // White pawns on rank 2 (a2-h2)
    public static long PAWN_START_BLACK = 0x00FF000000000000L; // Black pawns on rank 7 (a7-h7)
    
    public static final long WHITE_CASTLE_K_PATH = Constants.WHITE_CASTLE_K_PATH;
    public static final long WHITE_CASTLE_Q_PATH = Constants.WHITE_CASTLE_Q_PATH;
    public static final long BLACK_CASTLE_K_PATH = Constants.BLACK_CASTLE_K_PATH;
    public static final long BLACK_CASTLE_Q_PATH = Constants.BLACK_CASTLE_Q_PATH;
    
    public static int[] WHITE_PROMOTIONS = {Constants.QUEEN_W, Constants.BISHOP_W, Constants.KNIGHT_W, Constants.ROOK_W};
    public static int[] BLACK_PROMOTIONS = {Constants.QUEEN_B, Constants.BISHOP_B, Constants.KNIGHT_B, Constants.ROOK_B};
    
    public static final int NO_PROMOTION = Constants.NO_PROMOTION;
    public static final int NOT_CASTLE = 0;
    
    // Attack tables (length 64)
    public static long[] PAWN_W_ATTACKS_EAST = new long[64];
    public static long[] PAWN_W_ATTACKS_WEST = new long[64];
    public static long[] PAWN_B_ATTACKS_EAST = new long[64];
    public static long[] PAWN_B_ATTACKS_WEST = new long[64];
    public static long[] KNIGHT_MOVEMENT = new long[64];
    public static long[] BISHOP_MOVEMENT = new long[64];
    public static long[] ROOK_MOVEMENT = new long[64];
    public static long[] KING_MOVEMENT = new long[64];
    
    // Magic–bitboard attack tables for sliding pieces
    public static long[][] BISHOP_ATTACKS = new long[64][];
    public static long[][] ROOK_ATTACKS = new long[64][];
    
    public static int[] ROOK_RELEVANT_BITS = {
        12, 11, 11, 11, 11, 11, 11, 12,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        12, 11, 11, 11, 11, 11, 11, 12
    };
    public static int[] BISHOP_RELEVANT_BITS = {
        6, 5, 5, 5, 5, 5, 5, 6,
        5, 5, 5, 5, 5, 5, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5,
        6, 5, 5, 5, 5, 5, 5, 6
    };
    
    // Initialize all move–generation tables.
    public static void initMoveGeneration() {
        initKnightMovementTable();
        initKingMovementTable();
        initPawnAttackTables();
        initBishopRookAttackTables();
    }
    public static void initBishopRookAttackTables() {
        for (int square = 0; square < 64; square++) {
            long bishopMask = bishopMovement(square);
            long rookMask = rookMovement(square);
            int bishopRelevantBits = BISHOP_RELEVANT_BITS[square];
            int rookRelevantBits = ROOK_RELEVANT_BITS[square];
            int bishopVariations = 1 << bishopRelevantBits;
            int rookVariations = 1 << rookRelevantBits;

            BISHOP_ATTACKS[square] = new long[bishopVariations];
            ROOK_ATTACKS[square] = new long[rookVariations];

            for (int i = 0; i < bishopVariations; i++) {
                long occupancy = occupancyMask(i, bishopRelevantBits, bishopMask);
                int magicIndex = (int) ((occupancy * Magics.BISHOP_MAGICS[square]) >>> (64 - bishopRelevantBits));
                BISHOP_ATTACKS[square][magicIndex] = bishopAttacksOnTheFly(square, occupancy);
            }
            for (int i = 0; i < rookVariations; i++) {
                long occupancy = occupancyMask(i, rookRelevantBits, rookMask);
                int magicIndex = (int) ((occupancy * Magics.ROOK_MAGICS[square]) >>> (64 - rookRelevantBits));
                ROOK_ATTACKS[square][magicIndex] = rookAttacksOnTheFly(square, occupancy);
            }

            // Save the movement masks for later use.
            BISHOP_MOVEMENT[square] = bishopMask;
            ROOK_MOVEMENT[square] = rookMask;
        }
    }

    
    private static void initKingMovementTable() {
        for (int sq = 0; sq < 64; sq++) {
            long moves = 0;
            int rank = sq / 8;
            int file = sq % 8;
            if (rank < 7) moves |= (1L << (sq + 8));
            if (rank > 0) moves |= (1L << (sq - 8));
            if (file > 0) moves |= (1L << (sq - 1));
            if (file < 7) moves |= (1L << (sq + 1));
            if (rank < 7 && file > 0) moves |= (1L << (sq + 8 - 1));
            if (rank < 7 && file < 7) moves |= (1L << (sq + 8 + 1));
            if (rank > 0 && file > 0) moves |= (1L << (sq - 8 - 1));
            if (rank > 0 && file < 7) moves |= (1L << (sq - 8 + 1));
            KING_MOVEMENT[sq] = moves;
        }
    }
    
    private static void initKnightMovementTable() {
        for (int sq = 0; sq < 64; sq++) {
            long moves = 0;
            int rank = sq / 8;
            int file = sq % 8;
            if (rank + 2 < 8 && file + 1 < 8) moves |= (1L << (sq + 16 + 1));
            if (rank + 2 < 8 && file - 1 >= 0) moves |= (1L << (sq + 16 - 1));
            if (rank - 2 >= 0 && file + 1 < 8) moves |= (1L << (sq - 16 + 1));
            if (rank - 2 >= 0 && file - 1 >= 0) moves |= (1L << (sq - 16 - 1));
            if (rank + 1 < 8 && file + 2 < 8) moves |= (1L << (sq + 8 + 2));
            if (rank + 1 < 8 && file - 2 >= 0) moves |= (1L << (sq + 8 - 2));
            if (rank - 1 >= 0 && file + 2 < 8) moves |= (1L << (sq - 8 + 2));
            if (rank - 1 >= 0 && file - 2 >= 0) moves |= (1L << (sq - 8 - 2));
            KNIGHT_MOVEMENT[sq] = moves;
        }
    }
    
    private static void initPawnAttackTables() {
        for (int sq = 0; sq < 64; sq++) {
            int rank = sq / 8;
            int file = sq % 8;
            long attack = 0;
            // For white, pawn attacks one rank up.
            if (rank < 7) {
                if (file > 0) attack |= (1L << (sq + 7));
                PAWN_W_ATTACKS_WEST[sq] = attack;
                attack = 0;
                if (file < 7) attack |= (1L << (sq + 9));
                PAWN_W_ATTACKS_EAST[sq] = attack;
            }
            // For black, pawn attacks one rank down.
            attack = 0;
            if (rank > 0) {
                if (file < 7) attack |= (1L << (sq - 7));
                PAWN_B_ATTACKS_WEST[sq] = attack;
                attack = 0;
                if (file > 0) attack |= (1L << (sq - 9));
                PAWN_B_ATTACKS_EAST[sq] = attack;
            }
        }
    }
    
    public static boolean isSquareAttacked(Board board, int square) {
        long sqBb = Bitboards.SQUARE_BITBOARDS[square];
        // Use opponent's pieces: if it's white's turn, check black pieces, and vice-versa.
        long pawn = (board.turn == Constants.WHITE) ? board.pawnB : board.pawnW;
        long king = (board.turn == Constants.WHITE) ? board.kingB : board.kingW;
        long knight = (board.turn == Constants.WHITE) ? board.knightB : board.knightW;
        long bishop = (board.turn == Constants.WHITE) ? board.bishopB : board.bishopW;
        long rook = (board.turn == Constants.WHITE) ? board.rookB : board.rookW;
        long queen = (board.turn == Constants.WHITE) ? board.queenB : board.queenW;
        
        // Check queen (using both bishop and rook moves)
        while (queen != 0) {
            int sq = Long.numberOfTrailingZeros(queen);
            long attacks = getBishopAttacks(sq, board.occupancy);
            attacks |= getRookAttacks(sq, board.occupancy);
            if ((attacks & sqBb) != 0) return true;
            queen &= queen - 1;
        }
        // Check bishops
        while (bishop != 0) {
            int sq = Long.numberOfTrailingZeros(bishop);
            long attacks = getBishopAttacks(sq, board.occupancy);
            if ((attacks & sqBb) != 0) return true;
            bishop &= bishop - 1;
        }
        // Check rooks
        while (rook != 0) {
            int sq = Long.numberOfTrailingZeros(rook);
            long attacks = getRookAttacks(sq, board.occupancy);
            if ((attacks & sqBb) != 0) return true;
            rook &= rook - 1;
        }
        // Check knights
        while (knight != 0) {
            int sq = Long.numberOfTrailingZeros(knight);
            if ((KNIGHT_MOVEMENT[sq] & sqBb) != 0) return true;
            knight &= knight - 1;
        }
        // Check pawn attacks (note the attack tables are from the opponent's perspective)
        while (pawn != 0) {
            int sq = Long.numberOfTrailingZeros(pawn);
            if (board.turn == Constants.WHITE) {
                if (((PAWN_B_ATTACKS_EAST[sq] | PAWN_B_ATTACKS_WEST[sq]) & sqBb) != 0) return true;
            } else {
                if (((PAWN_W_ATTACKS_EAST[sq] | PAWN_W_ATTACKS_WEST[sq]) & sqBb) != 0) return true;
            }
            pawn &= pawn - 1;
        }
        // Check king moves
        while (king != 0) {
            int sq = Long.numberOfTrailingZeros(king);
            if ((KING_MOVEMENT[sq] & sqBb) != 0) return true;
            king &= king - 1;
        }
        return false;
    }
    
    private static long bishopMovement(int square) {
        int tr = square / 8;
        int tf = square % 8;
        long movement = 0L;
        // Loop diagonally up-right (only inner squares, from rank+1 to 6 and file+1 to 6)
        for (int r = tr + 1, f = tf + 1; r <= 6 && f <= 6; r++, f++) {
            movement |= (1L << (r * 8 + f));
        }
        // Diagonally up-left
        for (int r = tr + 1, f = tf - 1; r <= 6 && f >= 1; r++, f--) {
            movement |= (1L << (r * 8 + f));
        }
        // Diagonally down-right
        for (int r = tr - 1, f = tf + 1; r >= 1 && f <= 6; r--, f++) {
            movement |= (1L << (r * 8 + f));
        }
        // Diagonally down-left
        for (int r = tr - 1, f = tf - 1; r >= 1 && f >= 1; r--, f--) {
            movement |= (1L << (r * 8 + f));
        }
        return movement;
    }

    
    private static long rookMovement(int square) {
        int tr = square / 8;
        int tf = square % 8;
        long movement = 0L;
        // Vertical moves: up (ranks greater than current, but only up to rank 6)
        for (int r = tr + 1; r <= 6; r++) {
            movement |= (1L << (r * 8 + tf));
        }
        // Vertical moves: down (ranks less than current, but not below rank 1)
        for (int r = tr - 1; r >= 1; r--) {
            movement |= (1L << (r * 8 + tf));
        }
        // Horizontal moves: right (files greater than current, but only up to file 6)
        for (int f = tf + 1; f <= 6; f++) {
            movement |= (1L << (tr * 8 + f));
        }
        // Horizontal moves: left (files less than current, but not less than file 1)
        for (int f = tf - 1; f >= 1; f--) {
            movement |= (1L << (tr * 8 + f));
        }
        return movement;
    }


    
    private static long occupancyMask(int index, int bits, long attackMask) {
        long occupancy = 0;
        for (int i = 0; i < bits; i++) {
            int square = Long.numberOfTrailingZeros(attackMask);
            attackMask ^= (1L << square);
            if ((index & (1 << i)) != 0) {
                occupancy |= (1L << square);
            }
        }
        return occupancy;
    }
    
    private static long bishopAttacksOnTheFly(int square, long block) {
        long attacks = 0;
        int rank = square / 8;
        int file = square % 8;
        for (int r = rank + 1, f = file + 1; r < 8 && f < 8; r++, f++) {
            int sq = r * 8 + f;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        for (int r = rank + 1, f = file - 1; r < 8 && f >= 0; r++, f--) {
            int sq = r * 8 + f;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        for (int r = rank - 1, f = file + 1; r >= 0 && f < 8; r--, f++) {
            int sq = r * 8 + f;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        for (int r = rank - 1, f = file - 1; r >= 0 && f >= 0; r--, f--) {
            int sq = r * 8 + f;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        return attacks;
    }
    
    private static long rookAttacksOnTheFly(int square, long block) {
        long attacks = 0;
        int rank = square / 8;
        int file = square % 8;
        for (int r = rank + 1; r < 8; r++) {
            int sq = r * 8 + file;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        for (int r = rank - 1; r >= 0; r--) {
            int sq = r * 8 + file;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        for (int f = file + 1; f < 8; f++) {
            int sq = rank * 8 + f;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        for (int f = file - 1; f >= 0; f--) {
            int sq = rank * 8 + f;
            attacks |= (1L << sq);
            if ((block & (1L << sq)) != 0) break;
        }
        return attacks;
    }
    
    public static long getBishopAttacks(int square, long occupancy) {
        occupancy &= BISHOP_MOVEMENT[square];
        occupancy *= Magics.BISHOP_MAGICS[square];
        int shift = 64 - BISHOP_RELEVANT_BITS[square];
        int index = (int)(occupancy >>> shift);
        return BISHOP_ATTACKS[square][index];
    }
    
    public static long getRookAttacks(int square, long occupancy) {
        occupancy &= ROOK_MOVEMENT[square];
        occupancy *= Magics.ROOK_MAGICS[square];
        int shift = 64 - ROOK_RELEVANT_BITS[square];
        int index = (int)(occupancy >>> shift);
        return ROOK_ATTACKS[square][index];
    }
    
    // Returns the king moves that are not occupied by friendly pieces.
    public static long getKingMask(Board board) {
        int kingSquare = (board.turn == Constants.WHITE) ? board.whiteKingSq : board.blackKingSq;
        long opponentOcc = (board.turn == Constants.WHITE) ? board.occupancyWhite : board.occupancyBlack;
        return KING_MOVEMENT[kingSquare] & ~opponentOcc;
    }
    
    // Helper bitboard functions.
    private static long setBit(long bb, int square) {
        return bb | (1L << square);
    }
    private static long toggleBit(long bb, int square) {
        return bb ^ (1L << square);
    }
    
    // Returns an array: [singlePush, doublePush]
    private static long[] pawnSingleAndDoublePushes(Board board) {
        long single, dbl;
        if (board.turn == Constants.WHITE) {
            single = board.pawnW << 8;
            single &= ~board.occupancy;
            dbl = (board.pawnW & PAWN_START_WHITE) << 16;
            dbl &= ~board.occupancy;
            // Double pushes only valid if intermediate square is also empty:
            dbl = (dbl >> 8) & single;
            dbl = dbl << 8;
        } else {
            single = board.pawnB >>> 8;
            single &= ~board.occupancy;
            dbl = (board.pawnB & PAWN_START_BLACK) >>> 16;
            dbl &= ~board.occupancy;
            dbl = (dbl << 8) & single;
            dbl = dbl >>> 8;
        }
        return new long[]{single, dbl};
    }
    
    private static void addPawnAdvanceWithPossiblePromos(Board board, boolean isPromoting, int turn, int from, int to, List<Move> moves) {
        if (isPromoting) {
            int[] promos = (turn == Constants.WHITE) ? WHITE_PROMOTIONS : BLACK_PROMOTIONS;
            for (int promo : promos) {
                Move move = new Move(from, to, promo, NOT_CASTLE, (turn == Constants.WHITE) ? Constants.PAWN_W : Constants.PAWN_B);
                validateMove(board, move);
                if (move.validation == Constants.ILLEGAL) continue;
                moves.add(move);
            }
            return;
        }
        Move move = new Move(from, to, NO_PROMOTION, NOT_CASTLE, (turn == Constants.WHITE) ? Constants.PAWN_W : Constants.PAWN_B);
        validateMove(board, move);
        if (move.validation == Constants.ILLEGAL) return;
        moves.add(move);
    }
    
    // Validate a move by making a copy of the board, pushing the move, and checking king safety.
    public static void validateMove(Board board, Move move) {
        if (move.castle != 0) {
            int sq = -1;
            if (move.castle == Constants.K) sq = Constants.F1;
            else if (move.castle == Constants.Q) sq = Constants.D1;
            else if (move.castle == Constants.k) sq = Constants.F8;
            else if (move.castle == Constants.q) sq = Constants.D8;
            if (sq != -1 && isSquareAttacked(board, sq)) {
                move.validation = Constants.ILLEGAL;
                return;
            }
            if (isSquareAttacked(board, (board.turn == Constants.WHITE) ? board.whiteKingSq : board.blackKingSq)) {
                move.validation = Constants.ILLEGAL;
                return;
            }
        }
        Board copy = Search.copyBoard(board);
        pushMove(copy, move);
        int kingSq = (copy.turn == Constants.WHITE) ? copy.blackKingSq : copy.whiteKingSq;
        copy.turn = (copy.turn == Constants.WHITE) ? Constants.BLACK : Constants.WHITE;
        boolean inCheck = isSquareAttacked(copy, kingSq);
        move.validation = inCheck ? Constants.ILLEGAL : Constants.LEGAL;
    }
    
    // Main legal moves generator.
    public static List<Move> legalMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        
        int kingSquare = (board.turn == Constants.WHITE) ? board.whiteKingSq : board.blackKingSq;
        long friendlyOcc = (board.turn == Constants.WHITE) ? board.occupancyWhite : board.occupancyBlack;
        long bishopBB = (board.turn == Constants.WHITE) ? board.bishopW : board.bishopB;
        long rookBB   = (board.turn == Constants.WHITE) ? board.rookW   : board.rookB;
        long queenBB  = (board.turn == Constants.WHITE) ? board.queenW  : board.queenB;
        long knightBB = (board.turn == Constants.WHITE) ? board.knightW : board.knightB;
        long pawnBB   = (board.turn == Constants.WHITE) ? board.pawnW   : board.pawnB;
        
        long attackMask = 0;
        long[] pawnPushes = pawnSingleAndDoublePushes(board);
        long singlePush = pawnPushes[0];
        long doublePush = pawnPushes[1];
        long epBB = (board.epSquare == -1) ? 0 : Bitboards.SQUARE_BITBOARDS[board.epSquare];
        
        // Pawn captures
        while (pawnBB != 0) {
            int sq = Long.numberOfTrailingZeros(pawnBB);
            boolean isPromoting = false;
            if (board.turn == Constants.WHITE) {
                // White promotion if pawn on rank 7 (squares 48 to 55)
                isPromoting = ((Bitboards.SQUARE_BITBOARDS[sq] & 0xFF00000000000000L) != 0);
            } else {
                // Black promotion if pawn on rank 2 (squares 8 to 15)
                isPromoting = ((Bitboards.SQUARE_BITBOARDS[sq] & 0x000000000000FF00L) != 0);
            }
            long occ = epBB | ((board.turn == Constants.WHITE) ? board.occupancyBlack : board.occupancyWhite);
            if (board.turn == Constants.WHITE) {
                long eastAttacks = PAWN_W_ATTACKS_EAST[sq] & occ;
                attackMask |= PAWN_W_ATTACKS_EAST[sq] | PAWN_W_ATTACKS_WEST[sq];
                if (eastAttacks != 0) {
                    int toSq = sq + 7;
                    addPawnAdvanceWithPossiblePromos(board, isPromoting, board.turn, sq, toSq, moves);
                }
                long westAttacks = PAWN_W_ATTACKS_WEST[sq] & occ;
                if (westAttacks != 0) {
                    int toSq = sq + 9;
                    addPawnAdvanceWithPossiblePromos(board, isPromoting, board.turn, sq, toSq, moves);
                }
            } else {
                long eastAttacks = PAWN_B_ATTACKS_EAST[sq] & occ;
                attackMask |= PAWN_B_ATTACKS_EAST[sq] | PAWN_B_ATTACKS_WEST[sq];
                if (eastAttacks != 0) {
                    int toSq = sq - 7;
                    addPawnAdvanceWithPossiblePromos(board, isPromoting, board.turn, sq, toSq, moves);
                }
                long westAttacks = PAWN_B_ATTACKS_WEST[sq] & occ;
                if (westAttacks != 0) {
                    int toSq = sq - 9;
                    addPawnAdvanceWithPossiblePromos(board, isPromoting, board.turn, sq, toSq, moves);
                }
            }
            pawnBB &= pawnBB - 1;
        }
        
        // Pawn single pushes
        while (singlePush != 0) {
            int sq = Long.numberOfTrailingZeros(singlePush);
            int fromSq = (board.turn == Constants.WHITE) ? sq - 8 : sq + 8;
            boolean isPromoting = false;
            if (board.turn == Constants.WHITE) {
                isPromoting = ((Bitboards.SQUARE_BITBOARDS[fromSq] & 0xFF00000000000000L) != 0);
            } else {
                isPromoting = ((Bitboards.SQUARE_BITBOARDS[fromSq] & 0x000000000000FF00L) != 0);
            }
            addPawnAdvanceWithPossiblePromos(board, isPromoting, board.turn, fromSq, sq, moves);
            singlePush &= singlePush - 1;
        }
        
        // Pawn double pushes
        while (doublePush != 0) {
            int sq = Long.numberOfTrailingZeros(doublePush);
            int fromSq = (board.turn == Constants.WHITE) ? sq - 16 : sq + 16;
            Move move = new Move(fromSq, sq, NO_PROMOTION, NOT_CASTLE, (board.turn == Constants.WHITE) ? Constants.PAWN_W : Constants.PAWN_B);
            validateMove(board, move);
            if (move.validation == Constants.LEGAL) {
                moves.add(move);
            }
            doublePush &= doublePush - 1;
        }
        
        // King moves
        long kingMovesMask = getKingMask(board);
        attackMask |= KING_MOVEMENT[kingSquare];
        while (kingMovesMask != 0) {
            int sq = Long.numberOfTrailingZeros(kingMovesMask);
            Move move = new Move(kingSquare, sq, NO_PROMOTION, NOT_CASTLE, (board.turn == Constants.WHITE) ? Constants.KING_W : Constants.KING_B);
            validateMove(board, move);
            if (move.validation == Constants.LEGAL) {
                moves.add(move);
            }
            kingMovesMask &= kingMovesMask - 1;
        }
        
        // Bishop moves
        while (bishopBB != 0) {
            int sq = Long.numberOfTrailingZeros(bishopBB);
            long attacks = getBishopAttacks(sq, board.occupancy);
            attackMask |= attacks;
            attacks &= ~friendlyOcc;
            while (attacks != 0) {
                int toSq = Long.numberOfTrailingZeros(attacks);
                Move move = new Move(sq, toSq, NO_PROMOTION, NOT_CASTLE, (board.turn == Constants.WHITE) ? Constants.BISHOP_W : Constants.BISHOP_B);
                validateMove(board, move);
                if (move.validation == Constants.LEGAL) moves.add(move);
                attacks &= attacks - 1;
            }
            bishopBB &= bishopBB - 1;
        }
        
        // Rook moves
        while (rookBB != 0) {
            int sq = Long.numberOfTrailingZeros(rookBB);
            long attacks = getRookAttacks(sq, board.occupancy);
            attackMask |= attacks;
            attacks &= ~friendlyOcc;
            while (attacks != 0) {
                int toSq = Long.numberOfTrailingZeros(attacks);
                Move move = new Move(sq, toSq, NO_PROMOTION, NOT_CASTLE, (board.turn == Constants.WHITE) ? Constants.ROOK_W : Constants.ROOK_B);
                validateMove(board, move);
                if (move.validation == Constants.LEGAL) moves.add(move);
                attacks &= attacks - 1;
            }
            rookBB &= rookBB - 1;
        }
        
        // Queen moves
        while (queenBB != 0) {
            int sq = Long.numberOfTrailingZeros(queenBB);
            long rookAttacks = getRookAttacks(sq, board.occupancy);
            long bishopAttacks = getBishopAttacks(sq, board.occupancy);
            long attacks = rookAttacks | bishopAttacks;
            attackMask |= attacks;
            attacks &= ~friendlyOcc;
            while (attacks != 0) {
                int toSq = Long.numberOfTrailingZeros(attacks);
                Move move = new Move(sq, toSq, NO_PROMOTION, NOT_CASTLE, (board.turn == Constants.WHITE) ? Constants.QUEEN_W : Constants.QUEEN_B);
                validateMove(board, move);
                if (move.validation == Constants.LEGAL) moves.add(move);
                attacks &= attacks - 1;
            }
            queenBB &= queenBB - 1;
        }
        
        // Knight moves
        while (knightBB != 0) {
            int sq = Long.numberOfTrailingZeros(knightBB);
            attackMask |= KNIGHT_MOVEMENT[sq];
            long target = KNIGHT_MOVEMENT[sq] & ~friendlyOcc;
            while (target != 0) {
                int toSq = Long.numberOfTrailingZeros(target);
                Move move = new Move(sq, toSq, NO_PROMOTION, NOT_CASTLE, (board.turn == Constants.WHITE) ? Constants.KNIGHT_W : Constants.KNIGHT_B);
                validateMove(board, move);
                if (move.validation == Constants.LEGAL) moves.add(move);
                target &= target - 1;
            }
            knightBB &= knightBB - 1;
        }
        
        // Castling moves
        if (board.turn == Constants.WHITE) {
            if ((board.castling & Constants.K) != 0) {
                boolean pathClear = (board.occupancy & Constants.WHITE_CASTLE_K_PATH) == 0;
                if (pathClear) {
                    Move move = new Move(Constants.E1, Constants.G1, NO_PROMOTION, Constants.K, -1);
                    validateMove(board, move);
                    if (move.validation == Constants.LEGAL) moves.add(move);
                }
            }
            if ((board.castling & Constants.Q) != 0) {
                boolean pathClear = (board.occupancy & Constants.WHITE_CASTLE_Q_PATH) == 0;
                if (pathClear) {
                    Move move = new Move(Constants.E1, Constants.C1, NO_PROMOTION, Constants.Q, -1);
                    validateMove(board, move);
                    if (move.validation == Constants.LEGAL) moves.add(move);
                }
            }
        } else {
            if ((board.castling & Constants.k) != 0) {
                boolean pathClear = (board.occupancy & Constants.BLACK_CASTLE_K_PATH) == 0;
                if (pathClear) {
                    Move move = new Move(Constants.E8, Constants.G8, NO_PROMOTION, Constants.k, -1);
                    validateMove(board, move);
                    if (move.validation == Constants.LEGAL) moves.add(move);
                }
            }
            if ((board.castling & Constants.q) != 0) {
                boolean pathClear = (board.occupancy & Constants.BLACK_CASTLE_Q_PATH) == 0;
                if (pathClear) {
                    Move move = new Move(Constants.E8, Constants.C8, NO_PROMOTION, Constants.q, -1);
                    validateMove(board, move);
                    if (move.validation == Constants.LEGAL) moves.add(move);
                }
            }
        }
        board.attacks = attackMask;
        return moves;
    }
    
    // pushMove: updates the board state given a move.
    public static void pushMove(Board board, Move move) {
        // En passant?
        boolean isEnPassant = (move.toSquare == board.epSquare) &&
                              ((move.pieceType == Constants.PAWN_W) || (move.pieceType == Constants.PAWN_B));
        if (isEnPassant) {
            makeEnPassantMove(board, move);
            return;
        }
        // Castling?
        if (move.castle != 0) {
            makeCastleMove(board, move);
            return;
        }
        
        // Update hash: remove piece from fromSquare, toggle side, remove old castling and en passant info.
        board.hash ^= Zobrist.PIECES[move.pieceType][move.fromSquare];
        board.hash ^= Zobrist.WHITE_TO_MOVE;
        board.hash ^= Zobrist.CASTLING[board.castling];
        if (board.epSquare != -1) board.hash ^= Zobrist.EN_PASSANT[board.epSquare];
        
        board.epSquare = -1;
        // Set potential en passant square if pawn moves two squares.
        boolean starterPawnMoved = false;
        if (move.pieceType == Constants.PAWN_W) {
            int rank = move.fromSquare / 8;
            if (rank == 1) starterPawnMoved = true;
        } else if (move.pieceType == Constants.PAWN_B) {
            int rank = move.fromSquare / 8;
            if (rank == 6) starterPawnMoved = true;
        }
        if (starterPawnMoved && Math.abs(move.toSquare - move.fromSquare) == 16) {
            board.epSquare = (board.turn == Constants.WHITE) ? move.fromSquare + 8 : move.fromSquare - 8;
        }
        if (board.epSquare != -1) board.hash ^= Zobrist.EN_PASSANT[board.epSquare];
        
        // Update castling rights.
        if (move.pieceType == Constants.KING_W || move.pieceType == Constants.KING_B) {
            board.castling &= (board.turn == Constants.WHITE) ? Constants.ALL_CASTLE_B : Constants.ALL_CASTLE_W;
        } else if (isRookPiece(move.pieceType, board.turn)) {
            if (board.turn == Constants.WHITE) {
                if (move.fromSquare == Constants.A1) board.castling &= 0b1101;
                if (move.fromSquare == Constants.H1) board.castling &= 0b1110;
            } else {
                if (move.fromSquare == Constants.A8) board.castling &= 0b0111;
                if (move.fromSquare == Constants.H8) board.castling &= 0b1011;
            }
        }
        
        // Move the piece.
        updatePieceBitboards(board, move);
        
        // Update hash: add piece at destination.
        if (move.promotion == Constants.NO_PROMOTION) {
            board.hash ^= Zobrist.PIECES[move.pieceType][move.toSquare];
        } else {
            board.hash ^= Zobrist.PIECES[move.promotion][move.toSquare];
        }
        
        // Remove captured piece, if any.
        removeCapturedPiece(board, move);
        
        board.hash ^= Zobrist.CASTLING[board.castling];
        board.hash ^= Zobrist.WHITE_TO_MOVE;
        board.turn = (board.turn == Constants.WHITE) ? Constants.BLACK : Constants.WHITE;
        board.computeOccupancyMasks();
    }
    
    // Helper: determine if the piece type is a rook for the given side.
    private static boolean isRookPiece(int pieceType, int turn) {
        if (turn == Constants.WHITE) return pieceType == Constants.ROOK_W;
        else return pieceType == Constants.ROOK_B;
    }
    
    // Update the bitboards for the moving piece.
    private static void updatePieceBitboards(Board board, Move move) {
        switch(move.pieceType) {
            case Constants.PAWN_W:
                board.pawnW = toggleBit(board.pawnW, move.fromSquare);
                if (move.promotion == Constants.NO_PROMOTION) {
                    board.pawnW = setBit(board.pawnW, move.toSquare);
                } else {
                    switch(move.promotion) {
                        case Constants.QUEEN_W: board.queenW = setBit(board.queenW, move.toSquare); break;
                        case Constants.ROOK_W: board.rookW = setBit(board.rookW, move.toSquare); break;
                        case Constants.BISHOP_W: board.bishopW = setBit(board.bishopW, move.toSquare); break;
                        case Constants.KNIGHT_W: board.knightW = setBit(board.knightW, move.toSquare); break;
                    }
                }
                break;
            case Constants.KNIGHT_W:
                board.knightW = toggleBit(board.knightW, move.fromSquare);
                board.knightW = setBit(board.knightW, move.toSquare);
                break;
            case Constants.BISHOP_W:
                board.bishopW = toggleBit(board.bishopW, move.fromSquare);
                board.bishopW = setBit(board.bishopW, move.toSquare);
                break;
            case Constants.ROOK_W:
                board.rookW = toggleBit(board.rookW, move.fromSquare);
                board.rookW = setBit(board.rookW, move.toSquare);
                break;
            case Constants.QUEEN_W:
                board.queenW = toggleBit(board.queenW, move.fromSquare);
                board.queenW = setBit(board.queenW, move.toSquare);
                break;
            case Constants.KING_W:
                board.kingW = toggleBit(board.kingW, move.fromSquare);
                board.kingW = setBit(board.kingW, move.toSquare);
                board.whiteKingSq = move.toSquare;
                break;
            case Constants.PAWN_B:
                board.pawnB = toggleBit(board.pawnB, move.fromSquare);
                if (move.promotion == Constants.NO_PROMOTION) {
                    board.pawnB = setBit(board.pawnB, move.toSquare);
                } else {
                    switch(move.promotion) {
                        case Constants.QUEEN_B: board.queenB = setBit(board.queenB, move.toSquare); break;
                        case Constants.ROOK_B: board.rookB = setBit(board.rookB, move.toSquare); break;
                        case Constants.BISHOP_B: board.bishopB = setBit(board.bishopB, move.toSquare); break;
                        case Constants.KNIGHT_B: board.knightB = setBit(board.knightB, move.toSquare); break;
                    }
                }
                break;
            case Constants.KNIGHT_B:
                board.knightB = toggleBit(board.knightB, move.fromSquare);
                board.knightB = setBit(board.knightB, move.toSquare);
                break;
            case Constants.BISHOP_B:
                board.bishopB = toggleBit(board.bishopB, move.fromSquare);
                board.bishopB = setBit(board.bishopB, move.toSquare);
                break;
            case Constants.ROOK_B:
                board.rookB = toggleBit(board.rookB, move.fromSquare);
                board.rookB = setBit(board.rookB, move.toSquare);
                break;
            case Constants.QUEEN_B:
                board.queenB = toggleBit(board.queenB, move.fromSquare);
                board.queenB = setBit(board.queenB, move.toSquare);
                break;
            case Constants.KING_B:
                board.kingB = toggleBit(board.kingB, move.fromSquare);
                board.kingB = setBit(board.kingB, move.toSquare);
                board.blackKingSq = move.toSquare;
                break;
        }
    }
    
    // Remove a captured piece (if any) at the destination square.
    private static void removeCapturedPiece(Board board, Move move) {
        long target = Bitboards.SQUARE_BITBOARDS[move.toSquare];
        if (board.turn == Constants.WHITE) {
            if ((board.pawnB & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.PAWN_B][move.toSquare];
                board.pawnB = toggleBit(board.pawnB, move.toSquare);
            } else if ((board.knightB & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.KNIGHT_B][move.toSquare];
                board.knightB = toggleBit(board.knightB, move.toSquare);
            } else if ((board.bishopB & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.BISHOP_B][move.toSquare];
                board.bishopB = toggleBit(board.bishopB, move.toSquare);
            } else if ((board.rookB & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.ROOK_B][move.toSquare];
                board.rookB = toggleBit(board.rookB, move.toSquare);
            } else if ((board.queenB & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.QUEEN_B][move.toSquare];
                board.queenB = toggleBit(board.queenB, move.toSquare);
            } else if ((board.kingB & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.KING_B][move.toSquare];
                board.kingB = toggleBit(board.kingB, move.toSquare);
            }
        } else {
            if ((board.pawnW & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.PAWN_W][move.toSquare];
                board.pawnW = toggleBit(board.pawnW, move.toSquare);
            } else if ((board.knightW & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.KNIGHT_W][move.toSquare];
                board.knightW = toggleBit(board.knightW, move.toSquare);
            } else if ((board.bishopW & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.BISHOP_W][move.toSquare];
                board.bishopW = toggleBit(board.bishopW, move.toSquare);
            } else if ((board.rookW & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.ROOK_W][move.toSquare];
                board.rookW = toggleBit(board.rookW, move.toSquare);
            } else if ((board.queenW & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.QUEEN_W][move.toSquare];
                board.queenW = toggleBit(board.queenW, move.toSquare);
            } else if ((board.kingW & target) != 0) {
                board.hash ^= Zobrist.PIECES[Constants.KING_W][move.toSquare];
                board.kingW = toggleBit(board.kingW, move.toSquare);
            }
        }
    }
    
    // En passant move.
    private static void makeEnPassantMove(Board board, Move move) {
        int capturedSq = board.epSquare + ((board.turn == Constants.WHITE) ? -8 : 8);
        board.hash ^= Zobrist.EN_PASSANT[board.epSquare];
        board.hash ^= Zobrist.PIECES[move.pieceType][move.fromSquare];
        board.hash ^= Zobrist.PIECES[move.pieceType][move.toSquare];
        if (board.turn == Constants.WHITE) {
            board.hash ^= Zobrist.PIECES[Constants.PAWN_B][capturedSq];
            board.pawnB = toggleBit(board.pawnB, capturedSq);
            board.pawnW = toggleBit(board.pawnW, move.fromSquare);
            board.pawnW = setBit(board.pawnW, board.epSquare);
        } else {
            board.hash ^= Zobrist.PIECES[Constants.PAWN_W][capturedSq];
            board.pawnW = toggleBit(board.pawnW, capturedSq);
            board.pawnB = toggleBit(board.pawnB, move.fromSquare);
            board.pawnB = setBit(board.pawnB, board.epSquare);
        }
        board.epSquare = -1;
        board.turn = (board.turn == Constants.WHITE) ? Constants.BLACK : Constants.WHITE;
        board.computeOccupancyMasks();
    }
    
    // Castling move.
    private static void makeCastleMove(Board board, Move move) {
        if (move.castle == Constants.K) {
            board.hash ^= Zobrist.PIECES[Constants.KING_W][Constants.E1];
            board.hash ^= Zobrist.PIECES[Constants.KING_W][Constants.G1];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_W][Constants.H1];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_W][Constants.F1];
            board.kingW = toggleBit(board.kingW, Constants.E1);
            board.kingW = setBit(board.kingW, Constants.G1);
            board.rookW = toggleBit(board.rookW, Constants.H1);
            board.rookW = setBit(board.rookW, Constants.F1);
            board.whiteKingSq = Constants.G1;
        } else if (move.castle == Constants.Q) {
            board.hash ^= Zobrist.PIECES[Constants.KING_W][Constants.E1];
            board.hash ^= Zobrist.PIECES[Constants.KING_W][Constants.C1];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_W][Constants.A1];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_W][Constants.D1];
            board.kingW = toggleBit(board.kingW, Constants.E1);
            board.kingW = setBit(board.kingW, Constants.C1);
            board.rookW = toggleBit(board.rookW, Constants.A1);
            board.rookW = setBit(board.rookW, Constants.D1);
            board.whiteKingSq = Constants.C1;
        } else if (move.castle == Constants.k) {
            board.hash ^= Zobrist.PIECES[Constants.KING_B][Constants.E8];
            board.hash ^= Zobrist.PIECES[Constants.KING_B][Constants.G8];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_B][Constants.H8];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_B][Constants.F8];
            board.kingB = toggleBit(board.kingB, Constants.E8);
            board.kingB = setBit(board.kingB, Constants.G8);
            board.rookB = toggleBit(board.rookB, Constants.H8);
            board.rookB = setBit(board.rookB, Constants.F8);
            board.blackKingSq = Constants.G8;
        } else if (move.castle == Constants.q) {
            board.hash ^= Zobrist.PIECES[Constants.KING_B][Constants.E8];
            board.hash ^= Zobrist.PIECES[Constants.KING_B][Constants.C8];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_B][Constants.A8];
            board.hash ^= Zobrist.PIECES[Constants.ROOK_B][Constants.D8];
            board.kingB = toggleBit(board.kingB, Constants.E8);
            board.kingB = setBit(board.kingB, Constants.C8);
            board.rookB = toggleBit(board.rookB, Constants.A8);
            board.rookB = setBit(board.rookB, Constants.D8);
            board.blackKingSq = Constants.C8;
        }
        if (board.epSquare != -1) board.hash ^= Zobrist.EN_PASSANT[board.epSquare];
        board.hash ^= Zobrist.WHITE_TO_MOVE;
        board.hash ^= Zobrist.CASTLING[board.castling];
        board.castling = (board.turn == Constants.WHITE) ? Constants.ALL_CASTLE_B : Constants.ALL_CASTLE_W;
        board.hash ^= Zobrist.CASTLING[board.castling];
        board.epSquare = -1;
        board.turn = (board.turn == Constants.WHITE) ? Constants.BLACK : Constants.WHITE;
        board.computeOccupancyMasks();
    }
}
