package com.krisztianszabo.chesspiece.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoardState implements Serializable {
    private Piece[] squares;
    private Player currentPlayer;
    private boolean isKingInCheck;
    private Map<Piece, List<Integer>> legalMoves;
    private List<Piece> pieces;
    private boolean whiteCastleKingside;
    private boolean whiteCastleQueenside;
    private boolean blackCastleKingside;
    private boolean blackCastleQueenside;
    private Integer enPassant;
    private int fiftyMoves;
    private Piece attackMarker = new Piece();

    private static final int N = 16, NE = 17, E = 1, SE = -15, S = -16, SW = -17, W = -1, NW = 15;
    private static final int[] bishopDirs = {NE, SE, SW, NW};
    private static final int[] rookDirs = {N, E, S, W};
    private static final int[] queenDirs = {N, NE, E, SE, S, SW, W, NW};
    private static final int[] knightDirs = {33, 18, -14, -31, -33, -18, 14, 31}; // Weirdo
    private static final Map<Integer, Piece.Type> types = new HashMap<Integer, Piece.Type>() {{
        put(1, Piece.Type.PAWN);
        put(2, Piece.Type.KNIGHT);
        put(3, Piece.Type.KING);
        put(5, Piece.Type.BISHOP);
        put(6, Piece.Type.ROOK);
        put(7, Piece.Type.QUEEN);
    }};
    private static final int[] promotionOptions = {2 << 10, 5 << 10, 6 << 10, 7 << 10};


    private final int enPassantBit = 1 << 8;
    private final int castlingBit = 1 << 9;
    private final int promotionBits = 7 << 10;

    public BoardState() {
        this.legalMoves = new HashMap<>();
        pieces = new ArrayList<>();
    }
    
    public BoardState(BoardState other) {
        this();
        this.currentPlayer = other.currentPlayer;
        this.squares = new Piece[128];
        this.pieces = other.clonePieces();
        this.fiftyMoves = other.fiftyMoves;
        for (Piece piece : this.pieces) {
            this.squares[piece.getPosition()] = piece;
        }
        this.enPassant = other.enPassant;
        this.blackCastleKingside = other.blackCastleKingside;
        this.blackCastleQueenside = other.blackCastleQueenside;
        this.whiteCastleKingside = other.whiteCastleKingside;
        this.whiteCastleQueenside = other.whiteCastleQueenside;
    }

    public boolean isKingInCheck() {
        return isKingInCheck;
    }

    public void initStandardChess() {
        currentPlayer = Player.WHITE;
        enPassant = null;
        fiftyMoves = 0;
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

    public void initTestPosition() {
        currentPlayer = Player.WHITE;
        enPassant = null;
        fiftyMoves = 0;
        whiteCastleKingside = false;
        whiteCastleQueenside = false;
        blackCastleKingside = false;
        blackCastleQueenside = false;
        Piece[] result = new Piece[128];
        // Setup pieces for the white player
        result[4] = new Piece(Piece.Type.KING, Player.WHITE, 4);
        result[96] = new Piece(Piece.Type.PAWN, Player.WHITE, 96);
        result[103] = new Piece(Piece.Type.PAWN, Player.WHITE, 103);
        // Setup pieces for the black player
        result[116] = new Piece(Piece.Type.KING, Player.BLACK, 116);
        result[16] = new Piece(Piece.Type.PAWN, Player.BLACK, 16);
        result[23] = new Piece(Piece.Type.PAWN, Player.BLACK, 23);
        for (Piece piece : result) {
            if (piece != null) {
                pieces.add(piece);
            }
        }
        this.squares = result;
        generateLegalMoves();
    }

    public void loadState(JSONObject state) throws JSONException {
        this.squares = new Piece[128];
        this.pieces = new ArrayList<>();
        JSONArray piecesData = state.getJSONArray("pieces");
        for (int i = 0; i < piecesData.length(); i++) {
            JSONObject pieceData = piecesData.getJSONObject(i);
            Piece newPiece = new Piece(types.get(pieceData.getInt("type")),
                    pieceData.getInt("owner") == 0 ? Player.WHITE : Player.BLACK,
                    pieceData.getInt("position"));
            pieces.add(newPiece);
            squares[newPiece.getPosition()] = newPiece;
            JSONArray moves = pieceData.getJSONArray("moves");
            List<Integer> legal = new ArrayList<>();
            for (int j = 0; j < moves.length(); j++) {
                legal.add(moves.getInt(j));
            }
        }
        this.currentPlayer = state.getInt("currentPlayer") == 0 ? Player.WHITE : Player.BLACK;
        this.fiftyMoves = state.getInt("fiftyMoves");
        this.enPassant = state.isNull("enPassant") ? null : state.getInt("enPassant");
        JSONArray kingside = state.getJSONArray("kingsideCastle");
        whiteCastleKingside = kingside.getBoolean(0);
        blackCastleKingside = kingside.getBoolean(1);
        JSONArray queenside = state.getJSONArray("queensideCastle");
        whiteCastleQueenside = queenside.getBoolean(0);
        blackCastleQueenside = queenside.getBoolean(1);
        this.generateLegalMoves();
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

    public int getFiftyMoves() {
        return fiftyMoves;
    }

    private void swapCurrentPlayer() {
        this.currentPlayer = this.currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
    }

    private List<Piece> clonePieces() {
        List<Piece> result = new ArrayList<>();
        for (Piece piece : pieces) {
            result.add(new Piece(piece.getType(), piece.getOwner(), piece.getPosition()));
        }
        return result;
    }

    public BoardState makeMove(int piecePos, int move) {
        boolean resetFiftyMoves = false;
        boolean clearEnPassant = true;
        Piece piece = squares[piecePos];
        int destination = move & 0x77;
        if (piece != null && legalMoves.get(piece).contains(move)) {
            BoardState result = new BoardState(this);
            piece = result.squares[piecePos];
            if ((move & enPassantBit) > 0) {
                // Check if we're doing an en passant capture
                Piece captured = result.squares[result.enPassant];
                result.pieces.remove(captured);
                result.squares[result.enPassant] = null;
            } else if ((move & castlingBit) > 0) {
                // If we're castling, move the rook to the appropriate square
                int rookPos = piecePos + (piecePos < destination ? 3 * E : 4 * W);
                int rookTarget = piecePos + (piecePos < destination ? E : W);
                Piece rook = result.squares[rookPos];
                result.squares[rookTarget] = rook;
                rook.setPosition(rookTarget);
                result.squares[rookPos] = null;
                if (result.currentPlayer == Player.WHITE) {
                    result.whiteCastleKingside = false;
                    result.whiteCastleQueenside = false;
                } else {
                    result.blackCastleKingside = false;
                    result.blackCastleQueenside = false;
                }
            } else {
                // Normal move (possibly pawn promotion)
                // Check if we're capturing something
                Piece target = result.squares[destination];
                if (target != null) {
                    result.pieces.remove(target);
                    resetFiftyMoves = true;
                } else if (piece.getType() == Piece.Type.PAWN) {
                    // Check if we're promoting a pawn
                    int promotion = (move & promotionBits) >> 10;
                    if (promotion > 0) {
                        switch (promotion) {
                            case 2:
                                piece.setType(Piece.Type.KNIGHT);
                                break;
                            case 5:
                                piece.setType(Piece.Type.BISHOP);
                                break;
                            case 6:
                                piece.setType(Piece.Type.ROOK);
                                break;
                            case 7:
                                piece.setType(Piece.Type.QUEEN);
                                break;
                        }
                    } else if (Math.abs(piecePos - destination) == 32) {
                        // If the pawn opened by two ranks, mark en passant as possible
                        result.enPassant = destination;
                        clearEnPassant = false;
                    }
                    resetFiftyMoves = true;
                }
            }

            // Disable castling if the case
            boolean mayCastle = result.currentPlayer == Player.WHITE ?
                    (result.whiteCastleKingside || whiteCastleQueenside) :
                    (result.blackCastleKingside || blackCastleQueenside);
            if (mayCastle) {
                if (piece.getType() == Piece.Type.KING) {
                    if (result.currentPlayer == Player.WHITE) {
                        result.whiteCastleKingside = false;
                        result.whiteCastleQueenside = false;
                    } else {
                        result.blackCastleKingside = false;
                        result.blackCastleQueenside = false;
                    }
                } else if (piece.getType() == Piece.Type.ROOK) {
                    Piece king = pieces.stream()
                            .filter(p -> p.getOwner() == result.currentPlayer &&
                                    p.getType() == Piece.Type.KING)
                            .findAny()
                            .orElse(null);
                    if (result.currentPlayer == Player.WHITE) {
                        if (piece.getPosition() < king.getPosition()) {
                            result.whiteCastleQueenside = false;
                        } else {
                            result.whiteCastleKingside = false;
                        }
                    } else {
                        if (piece.getPosition() < king.getPosition()) {
                            result.blackCastleQueenside = false;
                        } else {
                            result.blackCastleKingside = false;
                        }
                    }
                }
            }

            // And finally, in all cases, we actually make the move
            result.squares[piecePos] = null;
            result.squares[destination] = piece;
            piece.setPosition(destination);
            if (clearEnPassant) {
                result.enPassant = null;
            }
            fiftyMoves = resetFiftyMoves ? 0 : fiftyMoves + 1;
            result.swapCurrentPlayer();
            return result;
        }
        return null;
    }

    public void generateLegalMoves() {
        // Clear all the attack markers from the off-board
        clearAttackMarkers();

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
            if (squares[move | 8] == null) {
                // Checks the off-board whether square is attacked
                legalKingMoves.add(move);
            }
        }
        legalMoves.replace(king, legalKingMoves);

        // Check for check
        List<Piece> attackers = findAttackers(king.getPosition());
        if (attackers.size() == 1) {
            isKingInCheck = true;
            Piece attacker = attackers.get(0);
            // In case the attacking piece is a slider, we have to remove the
            // normally legal move that is on the far side of the king
            if (isSlidingPiece(attacker)) {
                Integer dir = getDirection(attacker.getPosition(), king.getPosition());
                if (dir != null) {
                    int target = king.getPosition() + dir;
                    List<Integer> moves = legalMoves.get(king);
                    if (moves.contains(target)) {
                        moves.remove(moves.indexOf(target));
                    }
                }
            }

            // Find and remove all moves that don't block the check, or take the checking piece
            List<Integer> vector = generateAttackVector(attacker, king.getPosition());

            for (Piece piece : currentPieces) {
                // All king's moves are legal at this stage, so we skip him
                if (piece.getType() == Piece.Type.KING) continue;
                List<Integer> legal = new ArrayList<>();
                for (int move : legalMoves.get(piece)) {
                    if (move == attacker.getPosition() || vector.contains(move)) {
                        legal.add(move);
                    }
                }
                legalMoves.replace(piece, legal);
            }
        } else if (attackers.size() > 1) {
            // Remove all moves except for the king's moves that get him out of check
            isKingInCheck = true;
            for (Piece piece : currentPieces) {
                if (piece.getType() != Piece.Type.KING) {
                    legalMoves.get(piece).clear();
                }
            }
        } else {
            // The king is not under check in this branch
            isKingInCheck = false;
            // Check to see if king may castle, and add the move/s if so
            boolean kingSide = (currentPlayer == Player.WHITE ?
                    whiteCastleKingside : blackCastleKingside) &&
                    squares[king.getPosition() + E] == null &&
                    squares[king.getPosition() + (2 * E)] == null;
            boolean queenSide = (currentPlayer == Player.WHITE ?
                    whiteCastleQueenside : blackCastleQueenside) &&
                    squares[king.getPosition() + W] == null &&
                    squares[king.getPosition() + (2 * W)] == null &&
                    squares[king.getPosition() + (3 * W)] == null;
            if (kingSide && isCastlingClear(king.getPosition(), E)) {
                int castlingMove = (king.getPosition() + (2 * E)) | castlingBit;
                legalMoves.get(king).add(castlingMove);
            }
            if (queenSide && isCastlingClear(king.getPosition(), W)) {
                int castlingMove = (king.getPosition() + (2 * W)) | castlingBit;
                legalMoves.get(king).add(castlingMove);
            }
        }

        // Check and add if there are en passant moves
        if (enPassant != null) {
            if (canTakeEnPassant(enPassant + W)) {
                Piece pawn = squares[enPassant + W];
                legalMoves.get(pawn).add((enPassant + N) | enPassantBit);
            }
            if (canTakeEnPassant(enPassant + E)) {
                Piece pawn = squares[enPassant + E];
                legalMoves.get(pawn).add((enPassant + N) | enPassantBit);
            }
        }

        // Remove illegal moves for any pinned pieces
        List<Piece> opponentSliders = opponentPieces.stream().filter(this::isSlidingPiece)
                .collect(Collectors.toList());
        for (Piece piece : opponentSliders) {
            List<Integer> vector = generateAttackVector(piece, king.getPosition());
            if (vector.size() > 0) {
                // The piece and the king are in line with at least one square separating them
                List<Piece> potentiallyPinned = new ArrayList<>();
                for (int index : vector) {
                    if (squares[index] != null && squares[index].getOwner() == currentPlayer) {
                        potentiallyPinned.add(squares[index]);
                    }
                }

                if (potentiallyPinned.size() == 1 &&
                        potentiallyPinned.get(0).getOwner() == currentPlayer) {
                    // The case of there being a single piece between the attacker
                    // and the king means that the piece is pinned.
                    Piece pinned = potentiallyPinned.get(0);
                    List<Integer> legal = new ArrayList<>();
                    for (int move : legalMoves.get(pinned)) {
                        if (move == piece.getPosition() || vector.contains(move)) {
                            legal.add(move);
                        }
                    }
                    legalMoves.replace(pinned, legal);
                } else if (enPassant != null && potentiallyPinned.size() == 2 &&
                        potentiallyPinned.stream().allMatch(p -> p.getType() == Piece.Type.PAWN)) {
                    // An edge case where a pawn that could otherwise do an en passant capture is
                    // prohibited of doing so, because that would leave its king in check
                    Integer direction = getDirection(piece.getPosition(), king.getPosition());
                    if (direction != null) {
                        int step = piece.getPosition();
                        do {
                            step += direction;
                        } while (squares[step] == null);
                        Piece firstPawn = squares[step];
                        Piece secondPawn = squares[step + direction];
                        // The two pawns have to be next to each other, one black and one white
                        if (secondPawn != null && firstPawn.getOwner() != secondPawn.getOwner()) {
                            Piece myPawn = firstPawn.getOwner() == currentPlayer ?
                                    firstPawn : secondPawn;
                            Piece enemyPawn = myPawn == firstPawn ? secondPawn : firstPawn;
                            if (enemyPawn.getPosition() == enPassant &&
                                    legalMoves.get(myPawn).contains(enPassant + N)) {
                                legalMoves.get(myPawn).remove(enPassant + N);
                            }
                        }
                    }
                }
            }
        }

        // Clear all the opponent's moves
        for (Piece piece : opponentPieces) {
            legalMoves.get(piece).clear();
        }
    }

    private boolean isCastlingClear(int kingPosition, int direction) {
        return squares[(kingPosition + direction) | 8] == null &&
               squares[(kingPosition + (2 * direction)) | 8] == null;
    }

    private boolean canTakeEnPassant(int square) {
        if (isWithinBounds(square)) {
            Piece piece = squares[square];
            return piece != null && piece.getOwner() != currentPlayer &&
                    piece.getType() == Piece.Type.PAWN;
        }
        return false;
    }

    private List<Integer> generatePawnMoves(Piece pawn) {
        List<Integer> result = new ArrayList<>();
        int direction = pawn.getOwner() == Player.WHITE ? N : S;

        // One rank
        int target = pawn.getPosition() + direction;
        boolean firstStepClear = false;
        if (isWithinBounds(target) && squares[target] == null) {
            if (isLastRank(target)) {
                for (int promotionBits : promotionOptions) {
                    result.add(target + promotionBits);
                }
            } else {
                result.add(target);
                firstStepClear = true;
            }
        }

        // Two ranks
        if (isOnHomeRank(pawn) && firstStepClear) {
            target += direction;
            if (squares[target] == null) {
                result.add(target);
            }
        }

        // Attack W and E
        int[] directions;
        if (pawn.getOwner() == Player.WHITE) {
            directions = new int[]{NW, NE};
        } else {
            directions = new int[]{SW, SE};
        }
        for (int dir : directions) {
            target = pawn.getPosition() + dir;
            if (isWithinBounds(target)) {
                Piece square = squares[target];
                if (square != null && square.getOwner() != pawn.getOwner()) {
                    result.add(target);
                }
                if (pawn.getOwner() != currentPlayer) {
                    squares[target | 8] = attackMarker;
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
                    if (piece.getOwner() != currentPlayer) {
                        squares[target | 8] = attackMarker;
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

    public boolean isPromotionMove(int piecePos, int dest) {
        return squares[piecePos].getType() == Piece.Type.PAWN && isLastRank(dest);
    }

    private void clearAttackMarkers() {
        for (int i = 0; i < 128; i++) {
            if ((i & 8) == 0) i += 8;
            this.squares[i] = null;
        }
    }

    private List<Integer> generateAttackVector(Piece attacker, int target) {
        List<Integer> result = new ArrayList<>();
        if (isSlidingPiece(attacker)) {
            int[] validDirections = null;
            switch (attacker.getType()) {
                case BISHOP:
                    validDirections = bishopDirs;
                    break;
                case ROOK:
                    validDirections = rookDirs;
                    break;
                case QUEEN:
                    validDirections = queenDirs;
                    break;
            }
            Integer direction = getDirection(attacker.getPosition(), target);
            if (direction == null || !contains(validDirections, direction)) {
                // Piece is not in line with the target
                return result;
            }
            int step = attacker.getPosition() + direction;
            while (isWithinBounds(step) && step != target) {
                result.add(step);
                step += direction;
            }
        }
        return result;
    }

    private boolean contains(int[] array, int item) {
        for (int element : array) {
            if (element == item) {
                return true;
            }
        }
        return false;
    }

    private Integer getDirection(int source, int destination) {
        int diff = destination - source;
        if (Math.abs(diff) < 7) {
            return diff < 0 ? W : E;
        }
        for (int dir : queenDirs) {
            if (Math.abs(dir) == 1) continue;
            int mod = diff % dir;
            if (mod == 0 && ((diff < 0 && dir < 0) || (diff > 0 && dir > 0))) {
                return dir;
            }
        }
        return null;
    }

    private List<Piece> findAttackers(int position) {
        Player opponent = currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
        List<Piece> result = new ArrayList<>();

        for (Piece piece : pieces.stream().filter(p -> p.getOwner() == opponent)
                .collect(Collectors.toList())) {
            if (legalMoves.get(piece).contains(position)) {
                result.add(piece);
            }
        }
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

    private boolean isLastRank(int square) {
        int rank = (square & 0x70) >> 4;
        return currentPlayer == Player.WHITE ? rank == 7 : rank == 0;
    }

    private boolean isSlidingPiece(Piece piece) {
        switch (piece.getType()) {
            case BISHOP:
            case ROOK:
            case QUEEN:
                return true;
            default:
                return false;
        }
    }

    public List<Integer> getLegalMoves(Piece piece) {
        return legalMoves.get(piece);
    }
}
