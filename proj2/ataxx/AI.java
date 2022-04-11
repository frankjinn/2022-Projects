/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Frank
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** Safety offset, how safe the AI will play. **/
    private static int safety = 2;
    /** Aggression factor, how aggressive the AI will play. **/
    private static int aggress = 2;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical  behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -WINNING_VALUE, WINNING_VALUE);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -WINNING_VALUE, WINNING_VALUE);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        int bestScore = -sense * INFTY;
        PieceColor curntCol = RED;
        if (sense == -1) {
            curntCol = BLUE;
        }
        Board testBoard = new Board(board);
        ArrayList<Move> bestMoves = new ArrayList<>();
        scannerLoop:
        for (int i = Board.index('a', '1');
             i <= Board.index('g', '7'); i++) {
            char[] fromCo = coordMake(i, 0, 0);
            if (board.get(i) == curntCol) {
                for (int y = -2; y <= 2; y++) {
                    for (int x = -2; x <= 2; x++) {
                        char[] toCo = coordMake(i, y, x);
                        if (board.legalMove(fromCo[0], fromCo[1],
                                toCo[0], toCo[1])) {
                            testBoard.makeMove(Move.move
                                    (fromCo[0], fromCo[1], toCo[0], toCo[1]));
                            int response = minMax(testBoard, depth - 1,
                                    false, -sense, alpha, beta);
                            if (response == bestScore) {
                                bestMoves.add(Move.move
                                    (fromCo[0], fromCo[1], toCo[0], toCo[1]));
                            } else if (curntCol == RED
                                    ? response > bestScore
                                    : response < bestScore) {
                                bestMoves.clear();
                                bestMoves.add(Move.move
                                     (fromCo[0], fromCo[1], toCo[0], toCo[1]));
                                bestScore = response;
                                if (curntCol == myColor()) {
                                    alpha = max(alpha, bestScore);
                                } else {
                                    beta = min(beta, bestScore);
                                }
                                if (alpha >= beta) {
                                    break scannerLoop;
                                }
                            }
                            testBoard.undo();
                        }
                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = bestMoves.get(_random.nextInt(bestMoves.size()));
        }
        return bestScore;
    }
    private char[] coordMake(int index, int offsetY, int offsetX) {
        return new char[]
        {(char) ('a' - 2 + index % 11 + offsetY),
            (char) ('1' - 2 + Math.floorDiv(index, 11) + offsetX)};
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }
        int pieceDiff = board.numPieces(RED) - board.numPieces(BLUE);

        int score = aggress * pieceDiff;
        for (int start = Board.index('a', '1');
             start <= Board.index('g', '7'); start++) {
            if (board.get(start) == RED) {
                for (int y = -1; y <= 1; y++) {
                    for (int x = -1; x <= 1; x++) {
                        if (x == 0 && y == 0) {
                            continue;
                        } else if (board.get(Board.neighbor(start, y, x))
                                != EMPTY) {
                            score += safety;
                        }
                    }
                }
            } else if (board.get(start) == BLUE) {
                for (int y = -1; y <= 1; y++) {
                    for (int x = -1; x <= 1; x++) {
                        if (x == 0 && y == 0) {
                            continue;
                        } else if (board.get(Board.neighbor(start, y, x))
                                != EMPTY) {
                            score -= aggress;
                        }
                    }
                }
            }
        }
        return score;
    }



    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
}

