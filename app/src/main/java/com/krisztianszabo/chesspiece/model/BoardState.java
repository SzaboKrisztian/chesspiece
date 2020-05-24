package com.krisztianszabo.chesspiece.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardState {
    private Piece[][] board;
    private Player currentPlayer;
    private Map<Piece, List<Coord>> legalMoves;
    private Map<Player, List<Piece>> active;
    private Map<Player, List<Piece>> captured;

    public BoardState() {
        this.legalMoves = new HashMap<>();
        active = new HashMap<>();
        active.put(Player.WHITE, new ArrayList<Piece>());
        active.put(Player.BLACK, new ArrayList<Piece>());
        captured = new HashMap<>();
        captured.put(Player.WHITE, new ArrayList<Piece>());
        captured.put(Player.BLACK, new ArrayList<Piece>());
    }
    
    public BoardState(BoardState other) {
        this();
        this.currentPlayer = other.currentPlayer;
        this.board = new Piece[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                this.board[x][y] = other.board[x][y];
            }
        }
        this.active.get(Player.WHITE).addAll(other.active.get(Player.WHITE));
        this.active.get(Player.BLACK).addAll(other.active.get(Player.BLACK));
        this.captured.get(Player.WHITE).addAll(other.captured.get(Player.WHITE));
        this.captured.get(Player.BLACK).addAll(other.captured.get(Player.BLACK));
    }

    public void initStandardChess() {
        currentPlayer = Player.WHITE;
        Piece[][] result = new Piece[8][8];
        // Setup pieces for the white player
        result[4][0] = new Piece(Piece.Type.KING, Player.WHITE, 4, 0);
        result[3][0] = new Piece(Piece.Type.QUEEN, Player.WHITE, 3, 0);
        result[2][0] = new Piece(Piece.Type.BISHOP, Player.WHITE, 2, 0);
        result[5][0] = new Piece(Piece.Type.BISHOP, Player.WHITE, 5, 0);
        result[6][0] = new Piece(Piece.Type.KNIGHT, Player.WHITE, 6, 0);
        result[1][0] = new Piece(Piece.Type.KNIGHT, Player.WHITE, 1, 0);
        result[0][0] = new Piece(Piece.Type.ROOK, Player.WHITE, 0, 0);
        result[7][0] = new Piece(Piece.Type.ROOK, Player.WHITE, 7, 0);
        for (int x = 0; x < 8; x++) {
            result[x][1] = new Piece(Piece.Type.PAWN, Player.WHITE, x, 1);
        }
        // Setup pieces for the black player
        result[4][7] = new Piece(Piece.Type.KING, Player.BLACK, 4, 7);
        result[3][7] = new Piece(Piece.Type.QUEEN, Player.BLACK, 3, 7);
        result[2][7] = new Piece(Piece.Type.BISHOP, Player.BLACK, 2, 7);
        result[5][7] = new Piece(Piece.Type.BISHOP, Player.BLACK, 5, 7);
        result[6][7] = new Piece(Piece.Type.KNIGHT, Player.BLACK, 6, 7);
        result[1][7] = new Piece(Piece.Type.KNIGHT, Player.BLACK, 1, 7);
        result[0][7] = new Piece(Piece.Type.ROOK, Player.BLACK, 0, 7);
        result[7][7] = new Piece(Piece.Type.ROOK, Player.BLACK, 7, 7);
        for (int x = 0; x < 8; x++) {
            result[x][6] = new Piece(Piece.Type.PAWN, Player.BLACK, x, 6);
        }
        for (Piece[] boardRow : result) {
            for (Piece piece : boardRow) {
                if (piece != null) {
                    active.get(piece.getBelongsTo()).add(piece);
                }
            }
        }
        this.board = result;
        generateLegalMoves();
    }

    public Piece[][] getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    private void swapCurrentPlayer() {
        this.currentPlayer = this.currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
    }

    public BoardState makeMove(Coord target, Coord destination) {
        Piece piece = board[target.getX()][target.getY()];
        if (legalMoves.get(piece).contains(destination)) {
            BoardState result = new BoardState(this);
            if (result.board[destination.getX()][destination.getY()] != null) {
                captured.get(currentPlayer).add(result.board[destination.getX()][destination.getY()]);
                active.get(currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE)
                        .remove(result.board[destination.getX()][destination.getY()]);
                result.board[destination.getX()][destination.getY()] = null;
            }
            result.board[target.getX()][target.getY()] = null;
            result.board[destination.getX()][destination.getY()] = piece;
            piece.setCoords(destination);
            piece.setMoved();
            result.swapCurrentPlayer();
            result.generateLegalMoves();
            return result;
        }
        return null;
    }

    private void generateLegalMoves() {
        Piece myKing = null;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Piece piece = board[x][y];
                if (piece != null) {
                    switch (piece.getType()) {
                        case PAWN:
                            legalMoves.put(piece, generatePawnMoves(x, y, piece));
                            break;
                        case BISHOP:
                            legalMoves.put(piece, generateBishopMoves(x, y, piece));
                            break;
                        case KNIGHT:
                            legalMoves.put(piece, generateKnightMoves(x, y, piece));
                            break;
                        case ROOK:
                            legalMoves.put(piece, generateRookMoves(x, y, piece));
                            break;
                        case QUEEN:
                            legalMoves.put(piece, generateQueenMoves(x, y, piece));
                            break;
                        case KING:
                            if (piece.getBelongsTo() == currentPlayer) {
                                myKing = piece;
                            }
                            legalMoves.put(piece, generateKingMoves(x, y, piece));
                            break;
                    }
                }
            }
        }

        Player opponent = currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;

        // Remove the king's illegal moves (moving into check)
        int movesRemoved = 0;
        for (Coord move : legalMoves.get(myKing)) {
            for (Piece piece : active.get(opponent)) {
                if (legalMoves.get(piece).contains(move)) {
                    legalMoves.get(myKing).remove(move);
                    movesRemoved++;
                    break;
                }
            }
        }
        Log.d("MOVES", "1st pass: removed " + movesRemoved);
        movesRemoved = 0;
        // Remove the remaining illegal moves (if in check)
        List<Piece> attackers = getAttackers(myKing.getCoords());
        Log.d("MOVES", "Num attackers: " + attackers.size());
        if (attackers.size() == 1) {
            // Remove all moves that don't take or block the attacker
            Piece attacker = attackers.get(0);
            List<Coord> attackVector = generateAttackVector(attacker, myKing.getCoords());
            for (Piece piece : active.get(currentPlayer)) {
                Coord legal = null;
                List<Coord> moves = legalMoves.get(piece);
                if (moves.contains(attacker.getCoords())) {
                    movesRemoved++;
                    legal = attacker.getCoords();
                }
                movesRemoved += moves.size();
                moves.clear();
                if (legal != null) {
                    moves.add(legal);
                }
            }
        } else if (attackers.size() > 1) {
            // Clear all legal moves except for the king's
            for (Piece piece : active.get(currentPlayer)) {
                if (piece.getType() == Piece.Type.KING) {
                    continue;
                }
                legalMoves.get(piece).clear();
            }
        }
        Log.d("MOVES", "2nd pass: removed " + movesRemoved);
    }

    private List<Coord> generatePawnMoves(int x, int y, Piece pawn) {
        List<Coord> result = new ArrayList<>();
        int direction = pawn.getBelongsTo() == Player.WHITE ? 1 : -1;

        // One step ahead
        int targetY = y + direction;
        int targetX = x;
        if (isWithinBounds(targetX, targetY) && board[targetX][targetY] == null) {
            result.add(new Coord(targetX, targetY));
        }

        // Two steps ahead
        if (!pawn.hasItMoved()) {
            targetY = y + 2 * direction;

            if (isWithinBounds(targetX, targetY) && board[targetX][targetY] == null) {
                result.add(new Coord(targetX, targetY));
            }
        }

        // Attack west
        targetX = x - 1;
        targetY = y + direction;
        if (isWithinBounds(targetX, targetY)) {
            Piece square = board[targetX][targetY];
            if (square != null && square.getBelongsTo() != pawn.getBelongsTo()) {
                result.add(new Coord(targetX, targetY));
            }
        }

        // Attack east
        targetX = x + 1;
        targetY = y + direction;
        if (isWithinBounds(targetX, targetY)) {
            Piece square = board[targetX][targetY];
            if (square != null && square.getBelongsTo() != pawn.getBelongsTo()) {
                result.add(new Coord(targetX, targetY));
            }
        }

        return result;
    }

    private List<Coord> generateSlidingMoves(int x, int y, Piece piece, int[][] directions, int limit) {
        List<Coord> result = new ArrayList<>();

        int targetX, targetY;

        for (int i = 0; i < directions[0].length; i++) {
            targetX = x;
            targetY = y;

            limit = limit == 0 ? 99 : limit;
            int distance = 0;
            while (distance < limit) {
                targetX += directions[0][i];
                targetY += directions[1][i];
                if (isWithinBounds(targetX, targetY)) {
                    Piece current = board[targetX][targetY];
                    if (current == null || current.getBelongsTo() != piece.getBelongsTo()) {
                        result.add(new Coord(targetX, targetY));
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
    
    private List<Coord> generateBishopMoves(int x, int y, Piece bishop) {
        int[][] directions = {{-1, 1, 1, -1}, {1, 1, -1, -1}};
        return generateSlidingMoves(x, y, bishop, directions, 0);
    }

    private List<Coord> generateRookMoves(int x, int y, Piece rook) {
        int[][] directions = {{0, 1, 0, -1}, {1, 0, -1, 0}};
        return generateSlidingMoves(x, y, rook, directions, 0);
    }
    
    private List<Coord> generateQueenMoves(int x, int y, Piece queen) {
        int[][] directions = {{0, 1, 1, 1, 0, -1, -1, -1}, {1, 1, 0, -1, -1, -1, 0, 1}};
        return generateSlidingMoves(x, y, queen, directions, 0);
    }
    
    private List<Coord> generateKingMoves(int x, int y, Piece king) {
        int[][] directions = {{0, 1, 1, 1, 0, -1, -1, -1}, {1, 1, 0, -1, -1, -1, 0, 1}};
        return generateSlidingMoves(x, y, king, directions, 1);
    }

    private List<Coord> generateKnightMoves(int x, int y, Piece knight) {
        int[][] directions = {{1, 2, 2, 1, -1, -2, -2, -1}, {2, 1, -1, -2, -2, -1, 1, 2}};
        return generateSlidingMoves(x, y, knight, directions, 1);
    }

    public boolean anyLegalMoves() {
        int total = 0;
        for (List<Coord> moves : legalMoves.values()) {
            total += moves.size();
        }
        return total > 0;
    }

    public List<Coord> generateAttackVector(Piece attacker, Coord target) {
        List<Coord> result = new ArrayList<>();
        if (attacker.getType() == Piece.Type.BISHOP ||
            attacker.getType() == Piece.Type.ROOK ||
            attacker.getType() == Piece.Type.QUEEN) {

            Coord source = attacker.getCoords();
            Coord current = new Coord(source);
            int directionX = Integer.compare(target.getX(), source.getX());
            int directionY = Integer.compare(target.getY(), source.getY());
            do {
                result.add(new Coord(current));
                current.set(current.getX() + directionX, current.getY() + directionY);
            } while (isWithinBounds(current.getX(), current.getY()) && !current.equals(target));
        }

        return result;
    }

    public List<Piece> getAttackers(Coord coord) {
        Player opponent = currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
        List<Piece> result = new ArrayList<>();

        for (Piece piece : active.get(opponent)) {
            if (legalMoves.get(piece).contains(coord)) {
                result.add(piece);
            }
        }
        Log.d("ATTACKERS", "On " + coord + ": " + result.size());
        return result;
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public List<Coord> getLegalMoves(Piece piece) {
        return legalMoves.get(piece);
    }
}
