package com.krisztianszabo.chesspiece.model;

import androidx.annotation.Nullable;

import com.krisztianszabo.chesspiece.model.Coord;
import com.krisztianszabo.chesspiece.model.Player;

import java.util.HashMap;
import java.util.Map;

public class Piece {

    public enum Type {
        PAWN,
        BISHOP,
        KNIGHT,
        ROOK,
        QUEEN,
        KING
    }

    private static Map<Type, String> typeCodes = new HashMap<Type, String>() {{
        put(Type.PAWN, "PAWN");
        put(Type.BISHOP, "BISHOP");
        put(Type.KNIGHT, "KNIGHT");
        put(Type.ROOK, "ROOK");
        put(Type.QUEEN, "QUEEN");
        put(Type.KING, "KING");
    }};

    private Type type;
    private Player belongsTo;
    private boolean moved = false;
    private int x;
    private int y;

    public Piece(Type type, Player belongsToPlayer, int x, int y) {
        this.type = type;
        this.belongsTo = belongsToPlayer;
        this.x = x;
        this.y = y;
    }

    public Player getBelongsTo() {
        return belongsTo;
    }

    public Type getType() {
        return type;
    }

    public Coord getCoords() {
        return new Coord(this.x, this.y);
    }

    public void setCoords(Coord coords) {
        this.x = coords.getX();
        this.y = coords.getY();
    }

    public boolean hasItMoved() {
        return this.moved;
    }

    public void setMoved() {
        this.moved = true;
    }

    public String getCode() {
        String prefix = belongsTo == Player.WHITE ? "WH_" : "BL_";
        return prefix + typeCodes.get(type);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
