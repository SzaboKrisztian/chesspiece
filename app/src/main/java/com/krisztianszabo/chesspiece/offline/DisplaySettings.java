package com.krisztianszabo.chesspiece.offline;

import java.io.Serializable;

public class DisplaySettings implements Serializable {
    private boolean rotateBoard;
    private boolean rotateBoardOneEachMove;
    private boolean rotateTopPieces;
    private boolean showCoordinates;
    private boolean showLastMove;
    private boolean showLegalMoves = true;

    public boolean isRotateBoard() {
        return rotateBoard;
    }

    public void setRotateBoard(boolean rotateBoard) {
        this.rotateBoard = rotateBoard;
    }

    public boolean isRotateBoardOneEachMove() {
        return rotateBoardOneEachMove;
    }

    public void setRotateBoardOneEachMove(boolean rotateBoardOneEachMove) {
        this.rotateBoardOneEachMove = rotateBoardOneEachMove;
    }

    public boolean isRotateTopPieces() {
        return rotateTopPieces;
    }

    public void setRotateTopPieces(boolean rotateTopPieces) {
        this.rotateTopPieces = rotateTopPieces;
    }

    public boolean isShowLegalMoves() {
        return showLegalMoves;
    }

    public void setShowLegalMoves(boolean showLegalMoves) {
        this.showLegalMoves = showLegalMoves;
    }

    public boolean isShowCoordinates() {
        return showCoordinates;
    }

    public void setShowCoordinates(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
    }

    public boolean isShowLastMove() {
        return showLastMove;
    }

    public void setShowLastMove(boolean showLastMove) {
        this.showLastMove = showLastMove;
    }
}
