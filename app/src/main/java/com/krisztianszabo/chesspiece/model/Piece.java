package com.krisztianszabo.chesspiece.model;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

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

    private final static Map<Type, String> typeCodes = new HashMap<Type, String>() {{
        put(Type.PAWN, "PAWN");
        put(Type.BISHOP, "BISHOP");
        put(Type.KNIGHT, "KNIGHT");
        put(Type.ROOK, "ROOK");
        put(Type.QUEEN, "QUEEN");
        put(Type.KING, "KING");
    }};

    private Type type;
    private Player owner;
    private int position;

    public Piece(Type type, Player belongsToPlayer, int position) {
        this.type = type;
        this.owner = belongsToPlayer;
        this.position = position;
    }

    public Player getOwner() {
        return owner;
    }

    public Type getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getCode() {
        String prefix = owner == Player.WHITE ? "WH_" : "BL_";
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

    @NotNull
    public String toString() {
        return getCode();
    }
}
