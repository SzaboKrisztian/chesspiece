package com.krisztianszabo.chesspiece.model;

import java.io.Serializable;
import java.util.Stack;

public class Game implements Serializable {
    public enum State {
        WHITE_MOVES,
        BLACK_MOVES,
        WHITE_WINS,
        BLACK_WINS,
        DRAW
    }

    private Stack<BoardState> undoneMoves = new Stack<>();
    private Stack<BoardState> boardHistory;
    private State state;
    private String white;
    private String black;

    public State getState() {
        return state;
    }

    public BoardState getBoard() {
        return boardHistory.empty() ? null : boardHistory.peek();
    }

    public void initOffline() {
        this.boardHistory = new Stack<>();
        BoardState board = new BoardState();
        board.initStandardChess();
//        board.initTestPosition();
        board.generateLegalMoves();
        this.boardHistory.push(board);
        this.state = checkState();
    }

    public void makeMove(int piecePos, int move) {
        BoardState newBoard = this.boardHistory.peek().makeMove(piecePos, move);
        if (newBoard != null) {
            changeBoard(newBoard);
        }
        if (!undoneMoves.isEmpty()) {
            undoneMoves.clear();
        }
    }

    private void changeBoard(BoardState newBoard) {
        newBoard.generateLegalMoves();
        this.boardHistory.push(newBoard);
        this.state = checkState();
    }

    private State checkState() {
        BoardState board = this.boardHistory.peek();
        if (board.anyLegalMoves()) {
            if (board.getFiftyMoves() > 100) {
                return State.DRAW;
            } else {
                return board.getCurrentPlayer() == Player.WHITE ? State.WHITE_MOVES : State.BLACK_MOVES;
            }
        } else if (board.isKingInCheck()) {
            return board.getCurrentPlayer() == Player.WHITE ? State.BLACK_WINS : State.WHITE_WINS;
        } else {
            return State.DRAW;
        }
    }

    public boolean canUndo() {
        return boardHistory.size() > 1;
    }

    public boolean canRedo() {
        return !undoneMoves.isEmpty();
    }

    public void undoMove() {
        if (canUndo()) {
            undoneMoves.push(boardHistory.pop());
        }
    }

    public void redoMove() {
        if (canRedo()) {
            boardHistory.push(undoneMoves.pop());
        }
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }
}
