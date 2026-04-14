package tysonline.engine;

public class Search {
    public static int SEARCH_NODES_SEARCHED = 0;
    public static Move SEARCH_BEST_MOVE = new Move();
    private static int _DEPTH;
    
    public static int search(Board board, int depth) {
        SEARCH_NODES_SEARCHED = 0;
        _DEPTH = depth;
        SEARCH_BEST_MOVE = new Move();
        int eval = alphabeta(board, depth, Constants.MIN_EVAL, Constants.MAX_EVAL);
        return eval;
    }
    
    public static int alphabeta(Board board, int depth, int alpha, int beta) {
        SEARCH_NODES_SEARCHED++;
        int origAlpha = alpha;
        TTEntry entry = TT.getTTEntry(board.hash);
        if (board.hash == entry.zobrist && entry.depth >= depth) {
            if (entry.nodeType == TTEntry.EXACT) {
                if (depth == _DEPTH)
                    SEARCH_BEST_MOVE = entry.move;
                return entry.eval;
            } else if (entry.nodeType == TTEntry.LOWER) {
                alpha = Math.max(alpha, entry.eval);
            } else if (entry.nodeType == TTEntry.UPPER) {
                beta = Math.min(beta, entry.eval);
            }
            if (alpha >= beta) {
                if (depth == _DEPTH)
                    SEARCH_BEST_MOVE = entry.move;
                return entry.eval;
            }
        }
        java.util.List<Move> moves = MoveGen.legalMoves(board);
        int res = Evaluation.result(board, moves);
        if (res != Constants.UN_DETERMINED) {
            int eval = Evaluation.evaluate(board, res);
            if (res != Constants.DRAW)
                eval += (_DEPTH - depth) * ((board.turn == Constants.WHITE) ? 1 : -1);
            return eval * ((board.turn == Constants.WHITE) ? 1 : -1);
        } else if (depth == 0) {
            return Evaluation.evaluate(board, res) * ((board.turn == Constants.WHITE) ? 1 : -1);
        }
        int eval = Constants.MIN_EVAL;
        Move bestMove = new Move();
        MoveOrderer.scoreMoves(board, entry, moves);
        int nextMove;
        while ((nextMove = MoveOrderer.selectMove(moves)) != -1) {
            Move move = moves.get(nextMove);
            if (move.validation == Constants.LEGAL) {
                Board child = copyBoard(board);
                MoveGen.pushMove(child, move);
                int childEval = -alphabeta(child, depth - 1, -beta, -alpha);
                if (childEval > eval) {
                    eval = childEval;
                    bestMove = move;
                    if (depth == _DEPTH)
                        SEARCH_BEST_MOVE = bestMove;
                }
                alpha = Math.max(alpha, childEval);
                if (alpha >= beta)
                    break;
            }
        }
        TT.addTTEntry(board, eval, bestMove, depth, beta, origAlpha);
        return eval;
    }
    
    public static Board copyBoard(Board board) {
        Board copy = new Board();
        copy.pawnW = board.pawnW;
        copy.knightW = board.knightW;
        copy.bishopW = board.bishopW;
        copy.rookW = board.rookW;
        copy.queenW = board.queenW;
        copy.kingW = board.kingW;
        copy.pawnB = board.pawnB;
        copy.knightB = board.knightB;
        copy.bishopB = board.bishopB;
        copy.rookB = board.rookB;
        copy.queenB = board.queenB;
        copy.kingB = board.kingB;
        copy.turn = board.turn;
        copy.castling = board.castling;
        copy.epSquare = board.epSquare;
        copy.halfmoves = board.halfmoves;
        copy.fullmoves = board.fullmoves;
        copy.whiteKingSq = board.whiteKingSq;
        copy.blackKingSq = board.blackKingSq;
        copy.occupancy = board.occupancy;
        copy.occupancyWhite = board.occupancyWhite;
        copy.occupancyBlack = board.occupancyBlack;
        copy.hash = board.hash;
        copy.attacks = board.attacks;
        return copy;
    }
}
