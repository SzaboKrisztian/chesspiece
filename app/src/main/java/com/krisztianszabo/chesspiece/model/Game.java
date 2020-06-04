package com.krisztianszabo.chesspiece.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Stack;

public class Game implements Serializable {
    public enum State {
        WHITE_MOVES,
        BLACK_MOVES,
        WHITE_WINS_CHECKMATE,
        BLACK_WINS_CHECKMATE,
        WHITE_WINS_RESIGNATION,
        BLACK_WINS_RESIGNATION,
        DRAW_MATERIAL,
        DRAW_STALEMATE,
        DRAW_FIFTY,
        DRAW_AGREEMENT
    }

    private Stack<BoardState> undoneMoves = new Stack<>();
    private Stack<BoardState> boardHistory = new Stack<>();;
    private State state;
    private String white;
    private String black;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public BoardState getBoard() {
        return boardHistory.empty() ? null : boardHistory.peek();
    }

    public BoardState getBoard(int i) {
        return boardHistory.get(i);
    }

    public int getHistorySize() {
        return boardHistory.size();
    }

    public void initOffline() {
        BoardState board = new BoardState();
        board.initStandardChess();
//        board.initTestPosition();
//        board.initEnPassantTest();
//        board.initEnPassantTest2();
//        board.initCheckPassantTest();
//        board.initCheckPassantTest2();
        board.generateLegalMoves();
        this.boardHistory.push(board);
        this.state = checkState();
    }

    public void initFromJSON(JSONObject data) throws JSONException {
        JSONObject meta = data.getJSONObject("meta");
        this.state = codeToState(meta.getInt("status"));
        this.white = meta.getString("whiteUser");
        this.black = meta.getString("blackUser");
        JSONArray history = data.getJSONObject("obj").getJSONArray("history");
        for (int i = 0; i < history.length(); i++) {
            BoardState board = new BoardState();
            board.initFromJSON(history.getJSONObject(i));
            this.boardHistory.push(board);
        }
    }

    private State codeToState(int code) {
        switch (code) {
            case -2:
                return State.WHITE_MOVES;
            case -1:
                return State.BLACK_MOVES;
            case 0:
                return State.WHITE_WINS_CHECKMATE;
            case 1:
                return State.BLACK_WINS_CHECKMATE;
            case 2:
                return State.WHITE_WINS_RESIGNATION;
            case 3:
                return State.BLACK_WINS_RESIGNATION;
            case 4:
                return State.DRAW_MATERIAL;
            case 5:
                return State.DRAW_STALEMATE;
            case 6:
                return State.DRAW_FIFTY;
            case 7:
                return State.DRAW_AGREEMENT;
            default:
                return null;
        }
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
                return State.DRAW_FIFTY;
            } else if (!board.enoughMaterial()) {
                return State.DRAW_MATERIAL;
            } else {
                return board.getCurrentPlayer() == Player.WHITE ?
                        State.WHITE_MOVES : State.BLACK_MOVES;
            }
        } else if (board.isKingInCheck()) {
            return board.getCurrentPlayer() == Player.WHITE ?
                    State.BLACK_WINS_CHECKMATE : State.WHITE_WINS_CHECKMATE;
        } else {
            return State.DRAW_STALEMATE;
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
