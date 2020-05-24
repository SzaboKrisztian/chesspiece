package com.krisztianszabo.chesspiece.model;

import androidx.annotation.Nullable;

public class Coord {
    private int x;
    private int y;

    public Coord() {
        this.x = -1;
        this.y = -1;
    }

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coord(Coord other) {
        this.x = other.x;
        this.y = other.y;
    }

    public static Coord parseString(String coord) {
        if (coord != null && coord.length() == 2) {
            char first = coord.charAt(0);
            if (first >= 'a' && first <= 'h') {
                char second = coord.charAt(1);
                if (second >= '1' && second <= '8') {
                    return new Coord(first - 97, second - 49);
                }
            }
        }
        return null;
    }

    public int getX() {
        return x;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Coord other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Coord) {
            Coord other = (Coord)obj;
            return this.x == other.x && this.y == other.y;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return (char)(this.x + 97) + String.valueOf(this.y + 1);
    }
}
