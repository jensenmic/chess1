package tysonline.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EngineTests {

    // This method is run before each test to ensure a fresh board
    private Board createBoardFromFen(String fen) {
        Board board = new Board();
        Fen.setFen(board, fen);
        return board;
    }
    
    @BeforeEach
    public void setUp() {
        // Initialize common engine components once
        Bitboards.initBitboards();
        Zobrist.initZobrist();
        MoveGen.initMoveGeneration();
        Evaluation.initEvaluation();
    }
    
    // Test FEN parsing for side to move
    @Test
    public void testParseFenTurn() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1";
        Board board = createBoardFromFen(fen);
        assertEquals(Constants.BLACK, board.turn, "Board turn should be BLACK");
    }
    
    // Test that piece bitboards are set (i.e. not zero) after FEN parsing
    @Test
    public void testParseFenPieceMasks() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1";
        Board board = createBoardFromFen(fen);
        // Check that at least one white pawn and one black pawn exist.
        assertNotEquals(0, board.pawnW, "White pawn bitboard should not be zero");
        assertNotEquals(0, board.pawnB, "Black pawn bitboard should not be zero");
    }
    
    // Test FEN parsing for en passant square
    @Test
    public void testParseFenEpSquare() {
        // For fen "… e3" the en passant target square should be set.
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e3 0 1";
        Board board = createBoardFromFen(fen);
        // Calculate expected square:
        // File: 'e' = 4; Rank: '3' -> 3 - 1 = 2; square = 2*8 + 4 = 20.
        assertEquals(20, board.epSquare,"En passant square should be 20");
    }
    
    // Test FEN parsing for king squares (using a custom fen)
    @Test
    public void testParseFenKingSquares() {
        // In this FEN, assume white king is on d3 and black king is on e6.
        String fen = "8/8/4k3/8/8/3K4/8/8 b - - 0 1";
        Board board = createBoardFromFen(fen);
        // The actual square numbers depend on our numbering (0 = a1, …, 63 = h8).
        // Here we assume: d3 = 3 + 2*8 = 19, e6 = 4 + 5*8 = 44.
        assertEquals(19, board.whiteKingSq, "White king square should be 19");
        assertEquals(44, board.blackKingSq, "Black king square should be 44");
    }
    
    // Test FEN parsing for castling rights
    @Test
    public void testParseFenCastlingRights() {
        String fen = "r3k3/8/8/8/8/8/8/4K2R b Kq - 0 1";
        Board board = createBoardFromFen(fen);
        int expected = Constants.K | Constants.q;
        assertEquals(expected, board.castling, "Castling rights should be K and q");
    }
    
    // Test that the hash computed during FEN parsing matches the computed hash value.
    @Test
    public void testParseFenHash() {
        Board board = createBoardFromFen(Constants.START_FEN);
        long computedHash = Zobrist.hash(board);
        assertEquals(computedHash, board.hash, "Board hash should match computed hash");
    }
    
    // Test move-to-SAN conversion for a normal move.
    @Test
    public void testMoveToSanNormal() {
        // For testing, we define a move from e2 to e3.
        // (Assume we have defined appropriate constants for squares in Constants.java.)
        Move move = new Move(Constants.E2, Constants.E3, Constants.NO_PROMOTION, 0, 0);
        String san = San.moveToSan(move);
        assertEquals("e2e3", san);
    }
    
    // Test SAN-to-move conversion for a normal move.
    @Test
    public void testSanToMoveNormal() {
        Board board = createBoardFromFen(Constants.START_FEN);
        Move move = San.sanToMove(board, "e2e3");
        assertEquals(Constants.E2, move.fromSquare, "Move from-square should be e2");
        assertEquals(Constants.E3, move.toSquare, "Move to-square should be e3");
    }
    
    // Test a simple pushMove and verify that the board hash changes.
    @Test
    public void testPushMoveHashNoCapture() {
        Board board = createBoardFromFen(Constants.START_FEN);
        long oldHash = board.hash;
        Move move = San.sanToMove(board, "e2e3");
        // Push move (assuming pushMove properly updates the board state and hash)
        MoveGen.pushMove(board, move);
        long newHash = board.hash;
        assertNotEquals(oldHash, newHash, "Hash should change after a non-capture move");
    }
    
    // Test evaluation function (starting position should be near 0)
    @Test
    public void testEvaluation() {
        Board board = createBoardFromFen(Constants.START_FEN);
        List<Move> moves = MoveGen.legalMoves(board);
        int res = Evaluation.result(board, moves);
        int eval = Evaluation.evaluate(board, res);
        assertTrue(Math.abs(eval) < 100,"Evaluation of starting position should be near 0");
    }
    
    // Additional tests for castling, promotion, and en passant can be added similarly.
}
