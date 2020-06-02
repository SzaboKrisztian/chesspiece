package com.krisztianszabo.chesspiece.online.games;

public class Utils {
    public static String getPlayersText(String whiteName, String blackName, String myName) {
        return "White: " + (whiteName.equals(myName) ? "You" : whiteName)
                + " vs. Black: " + (blackName.equals(myName) ? "You" : blackName);
    }

    public static String getStatusText(int statusCode, String whiteName,
                                       String blackName, String myName) {
        switch (statusCode) {
            case -2:
                return (whiteName.equals(myName) ? "Your turn" : "Opponent's turn");
            case -1:
                return (blackName.equals(myName) ? "Your turn" : "Opponent's turn");
            case 0:
                return (whiteName.equals(myName) ? "You win" : "Opponent wins")
                        + " by checkmate.";
            case 1:
                return (blackName.equals(myName) ? "You win" : "Opponent wins")
                        + " by checkmate.";
            case 2:
                return (whiteName.equals(myName) ? "You win" : "Opponent wins")
                        + " by resignation.";
            case 3:
                return (blackName.equals(myName) ? "You win" : "Opponent wins")
                        + " by resignation.";
            case 4:
                return "Draw by insufficient material";
            case 5:
                return "Draw by stalemate";
            case 6:
                return "Draw by fifty moves rule";
            case 7:
                return "Draw by agreement";
            default:
                return null;
        }
    }

    public static String addNumMoves(String str, int numMoves) {
        return str + " " + (numMoves / 2) + (numMoves % 2 != 0 ? "½ " : " ") + "moves played.";
    }
}
