package com.krisztianszabo.chesspiece.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoardState {
    private Piece[] squares;
    private Player currentPlayer;
    private Map<Piece, List<Integer>> legalMoves;
    private List<Piece> pieces;
    private List<Piece> captured;
    private boolean whiteCastleKingside;
    private boolean whiteCastleQueenside;
    private boolean blackCastleKingside;
    private boolean blackCastleQueenside;
    private Integer enPassant;

    public static final int N = 16, NE = 17, E = 1, SE = -15, S = -16, SW = -17, W = -1, NW = 15;
    public static final int[] bishopDirs = {NE, SE, SW, NW};
    public static final int[] rookDirs = {N, E, S, W};
    public static final int[] queenDirs = {N, NE, E, SE, S, SW, W, NW};
    public static final int[] knightDirs = {33, 18, -14, -31, -33, -18, 14, 31}; // Weirdo

    public BoardState() {
        this.legalMoves = new HashMap<>();
        pieces = new ArrayList<>();
        captured = new ArrayList<>();
    }
    
    public BoardState(BoardState other) {
        this();
        this.currentPlayer = other.currentPlayer;
        this.squares = new Piece[128];
        System.arraycopy(other.squares, 0, this.squares, 0, 128);
        this.pieces.addAll(other.pieces);
        this.captured.addAll(other.captured);
        this.enPassant = other.enPassant;
        this.blackCastleKingside = other.blackCastleKingside;
        this.blackCastleQueenside = other.blackCastleQueenside;
        this.whiteCastleKingside = other.whiteCastleKingside;
        this.whiteCastleQueenside = other.whiteCastleQueenside;
    }

    public void initStandardChess() {
        currentPlayer = Player.WHITE;
        enPassant = null;
        whiteCastleKingside = true;
        whiteCastleQueenside = true;
        blackCastleKingside = true;
        blackCastleQueenside = true;
        Piece[] result = new Piece[128];
        // Setup pieces for the white player
        result[0] = new Piece(Piece.Type.ROOK, Player.WHITE, 0);
        result[1] = new Piece(Piece.Type.KNIGHT, Player.WHITE, 1);
        result[2] = new Piece(Piece.Type.BISHOP, Player.WHITE, 2);
        result[3] = new Piece(Piece.Type.QUEEN, Player.WHITE, 3);
        result[4] = new Piece(Piece.Type.KING, Player.WHITE, 4);
        result[5] = new Piece(Piece.Type.BISHOP, Player.WHITE, 5);
        result[6] = new Piece(Piece.Type.KNIGHT, Player.WHITE, 6);
        result[7] = new Piece(Piece.Type.ROOK, Player.WHITE, 7);
        for (int i = 16; i < 24; i++) {
            result[i] = new Piece(Piece.Type.PAWN, Player.WHITE, i);
        }
        // Setup pieces for the black player
        result[112] = new Piece(Piece.Type.ROOK, Player.BLACK, 112);
        result[113] = new Piece(Piece.Type.KNIGHT, Player.BLACK, 113);
        result[114] = new Piece(Piece.Type.BISHOP, Player.BLACK, 114);
        result[115] = new Piece(Piece.Type.QUEEN, Player.BLACK, 115);
        result[116] = new Piece(Piece.Type.KING, Player.BLACK, 116);
        result[117] = new Piece(Piece.Type.BISHOP, Player.BLACK, 117);
        result[118] = new Piece(Piece.Type.KNIGHT, Player.BLACK, 118);
        result[119] = new Piece(Piece.Type.ROOK, Player.BLACK, 119);
        for (int i = 96; i < 104; i++) {
            result[i] = new Piece(Piece.Type.PAWN, Player.BLACK, i);
        }
        for (Piece piece : result) {
            if (piece != null) {
                pieces.add(piece);
            }
        }
        this.squares = result;
        generateLegalMoves();
    }

    public Piece[] getSquares() {
        return squares;
    }

    public List<Piece> getPieces() {
        return this.pieces;
    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    private void swapCurrentPlayer() {
        this.currentPlayer = this.currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
    }

    public BoardState makeMove(int target, int destination) {
        Piece piece = squares[target];
        if (legalMoves.get(piece).contains(destination)) {
            BoardState result = new BoardState(this);
            // Check for and handle capturing
            if (result.squares[destination] != null) {
                captured.add(result.squares[destination]);
                pieces.remove(result.squares[destination]);
                result.squares[destination] = null;
            }
            // Make the move
            result.squares[target] = null;
            result.squares[destination] = piece;
            piece.setPosition(destination);
            result.swapCurrentPlayer();
            result.generateLegalMoves();
            return result;
        }
        return null;
    }

    private void generateLegalMoves() {
        Piece king = null;
        // Generate all the pseudo-legal moves
        for (Piece piece : pieces) {
            switch (piece.getType()) {
                case PAWN:
                    legalMoves.put(piece, generatePawnMoves(piece));
                    break;
                case BISHOP:
                    legalMoves.put(piece, generatePieceMoves(piece, bishopDirs, 0));
                    break;
                case KNIGHT:
                    legalMoves.put(piece, generatePieceMoves(piece, knightDirs, 1));
                    break;
                case ROOK:
                    legalMoves.put(piece, generatePieceMoves(piece, rookDirs, 0));
                    break;
                case QUEEN:
                    legalMoves.put(piece, generatePieceMoves(piece, queenDirs, 0));
                    break;
                case KING:
                    if (piece.getOwner() == currentPlayer) {
                        king = piece;
                    }
                    legalMoves.put(piece, generatePieceMoves(piece, queenDirs, 1));
                    break;
            }
        }

        // Prepare some references that will be often used below
        Player opponent = currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
        List<Piece> currentPieces = pieces.stream()
                .filter(piece -> piece.getOwner() == currentPlayer).collect(Collectors.toList());
        List<Piece> opponentPieces = pieces.stream()
                .filter(piece -> piece.getOwner() == opponent).collect(Collectors.toList());

        // Remove all the king's moves that would put him in check
        List<Integer> legalKingMoves = new ArrayList<>();
        for (int move: legalMoves.get(king)) {
            if (findAttackers(move).size() == 0) {
                legalKingMoves.add(move);
            }
        }
        legalMoves.replace(king, legalKingMoves);

        // Check for check
        List<Piece> attackers = findAttackers(king.getPosition());
        if (attackers.size() == 1) {
            // Find and remove all moves that don't block the check, or take the checking piece
        } else if (attackers.size() > 1) {
            // Remove all moves except for the king's moves that get him out of check
        } else {
            // The king is not under check in this branch
            // Check to see if king may castle, and add the move/s if so
        }

        // Check and add if there are en passant moves
        if (enPassant != null) {

        }

        // Remove illegal moves for any pinned pieces

        // Clear all the opponent's moves
    }

    private List<Integer> generatePawnMoves(Piece pawn) {
        List<Integer> result = new ArrayList<>();
        int direction = pawn.getOwner() == Player.WHITE ? N : S;
        int startRank = pawn.getOwner() == Player.WHITE ? 1 : 6;

        // One rank
        int target = pawn.getPosition() + direction;
        boolean firstStepClear = false;
        if (isWithinBounds(target) && squares[target] == null) {
            result.add(target);
            firstStepClear = true;
        }

        // Two ranks
        if (isOnHomeRank(pawn) && firstStepClear) {
            target += direction;
            if (squares[target] == null) {
                result.add(target);
            }
        }

        // Attack NW and NE
        int[] directions = {NW, NE};
        for (int dir : directions) {
            target = pawn.getPosition() + dir;
            if (isWithinBounds(target)) {
                Piece square = squares[target];
                if (square != null && square.getOwner() != pawn.getOwner()) {
                    result.add(target);
                }
            }
        }

        return result;
    }

    private List<Integer> generatePieceMoves(Piece piece, int[] directions, int limit) {
        List<Integer> result = new ArrayList<>();

        int target;

        for (int i = 0; i < directions.length; i++) {
            target = piece.getPosition();

            limit = limit == 0 ? 99 : limit;
            int distance = 0;
            while (distance < limit) {
                target += directions[i];
                if (isWithinBounds(target)) {
                    Piece current = squares[target];
                    if (current == null || current.getOwner() != piece.getOwner()) {
                        result.add(target);
                    }
                    if (current != null) {
                        break;
                    }
                } else {
                    break;
                }
                distance++;
            }
        }
        return result;
    }

    public boolean anyLegalMoves() {
        int total = 0;
        for (List<Integer> moves : legalMoves.values()) {
            total += moves.size();
        }
        return total > 0;
    }

    public List<Integer> generateAttackVector(Piece attacker, int target) {
        return null;
    }

    public List<Piece> findAttackers(int position) {
        Player opponent = currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
        List<Piece> result = new ArrayList<>();

        for (Piece piece : pieces.stream().filter(p -> p.getOwner() == opponent)
                .collect(Collectors.toList())) {
            if (legalMoves.get(piece).contains(position)) {
                result.add(piece);
            }
        }
        Log.d("ATTACKERS", "On " + position + ": " + result.size());
        return result;
    }

    private boolean isWithinBounds(int position) {
        if (position >= 0 && position < 128) {
            return (position & 0x88) == 0;
        }
        return false;
    }

    private boolean isOnHomeRank(Piece piece) {
        if (piece.getType() == Piece.Type.PAWN) {
            int homeRank = piece.getOwner() == Player.WHITE ? 1 : 6;
            return ((piece.getPosition() & 0x70) >> 4) == homeRank;
        }
        return false;
    }

    public List<Integer> getLegalMoves(Piece piece) {
        return legalMoves.get(piece);
    }
}
