package com.krisztianszabo.chesspiece;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.krisztianszabo.chesspiece.model.BoardState;
import com.krisztianszabo.chesspiece.model.Piece;
import com.krisztianszabo.chesspiece.model.Player;
import com.krisztianszabo.chesspiece.offline.DisplaySettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardView extends SurfaceView implements SurfaceHolder.Callback,
        SurfaceView.OnTouchListener {
    private Integer promotionMove = null;
    private ChessMoveReceiver moveReceiver;
    private BoardState boardState;
    private boolean currentlyRotated = false;
    private boolean rotateBoard = false;
    private boolean rotateUpperPieces = false;
    private boolean rotateBoardOnEachMove = false;
    private boolean showCoordinates = false;
    private boolean showLegalMoves = true;
    private boolean showLastMove = false;
    private boolean allowTouch;
    private Resources res = getContext().getResources();
    private Bitmap bg = BitmapFactory.decodeResource(res, R.drawable.board);
    private Bitmap bgCoord = BitmapFactory.decodeResource(res, R.drawable.board_coords);
    private Bitmap bgCoordFlip = BitmapFactory.decodeResource(res, R.drawable.board_coords_flipped);
    private Bitmap pieces = BitmapFactory.decodeResource(res, R.drawable.chess_pieces);
    private Bitmap piecesFlipped = BitmapFactory.decodeResource(res, R.drawable.chess_pieces_flipped);
    private Bitmap selection = BitmapFactory.decodeResource(res, R.drawable.selection);
    private Bitmap legalMove = BitmapFactory.decodeResource(res, R.drawable.legal_move);
    private Map<String, Rect> pieceGfx = generateMap();
    private Paint paint = new Paint();
    private int totalWidth;
    private int totalHeight;
    private int squareWidth;
    private int squareHeight;
    private Rect[][] squares;
    private Rect promotionMenu;
    private Rect promotionSquare = new Rect();
    private Rect selectedSquare = null;
    private Piece selectedPiece = null;
    private int[] selectedCoord = null;
    private final String[] WH_PROMOTION = {"WH_KNIGHT", "WH_BISHOP", "WH_ROOK", "WH_QUEEN"};
    private final String[] BL_PROMOTION = {"BL_KNIGHT", "BL_BISHOP", "BL_ROOK", "BL_QUEEN"};

    public BoardView(Context context) {
        super(context);
        init();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    // Set the SurfaceHolder callback and onTouchListener
    private void init() {
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    // Set the reference to the parent activity / fragment, needed to send out the player's move
    public void setMoveReceiver(ChessMoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    // Controls whether the view should respond to touch events. Used in online mode
    // to only allow touch events when it's the player's turn. Always true in offline mode.
    public void setAllowTouch(boolean allowTouch) {
        this.allowTouch = allowTouch;
    }

    // Set the board state to be displayed
    public void setBoardState(BoardState boardState) {
        this.boardState = boardState;
        this.invalidate();
    }

    // Set the view's boolean params to the given display settings
    public void setDisplaySettings(DisplaySettings settings) {
        rotateBoard = settings.isRotateBoard();
        rotateBoardOnEachMove = settings.isRotateBoardOneEachMove();
        rotateUpperPieces = settings.isRotateTopPieces();
        if (settings.isShowCoordinates() != showCoordinates) {
            showCoordinates = settings.isShowCoordinates();
            generateSquares(totalWidth, totalHeight);
        }
        showLegalMoves = settings.isShowLegalMoves();
        showLastMove = settings.isShowLastMove();
        updateCurrentlyRotated();
        invalidate();
    }

    public void setRotateBoard(boolean rotateBoard) {
        this.rotateBoard = rotateBoard;
        updateCurrentlyRotated();
        invalidate();
    }

    public void setRotateUpperPieces(boolean rotateUpperPieces) {
        this.rotateUpperPieces = rotateUpperPieces;
        invalidate();
    }

    public void setShowCoordinates(boolean showCoordinates) {
        if (showCoordinates != this.showCoordinates) {
            this.showCoordinates = showCoordinates;
            generateSquares(totalWidth, totalHeight);
        }
        invalidate();
    }

    public void setShowLegalMoves(boolean showLegalMoves) {
        this.showLegalMoves = showLegalMoves;
        invalidate();
    }

    public void setShowLastMove(boolean showLastMove) {
        this.showLastMove = showLastMove;
        invalidate();
    }

    // Figure out if the board should be rotated, according to the chosen settings
    private void updateCurrentlyRotated() {
        currentlyRotated = rotateBoard != (rotateBoardOnEachMove &&
                boardState.getCurrentPlayer() == Player.BLACK);
    }

    private Map<String, Rect> generateMap() {
        int totalWidth = pieces.getWidth();
        int totalHeight = pieces.getHeight();
        int width = totalWidth / 6;
        int height = totalHeight / 2;

        Map<String, Rect> result = new HashMap<>();
        result.put("WH_KING", new Rect(0, 0, width, height));
        result.put("WH_QUEEN", new Rect(width, 0, width * 2, height));
        result.put("WH_BISHOP", new Rect(width * 2, 0, width * 3, height));
        result.put("WH_KNIGHT", new Rect(width * 3, 0, width * 4, height));
        result.put("WH_ROOK", new Rect(width * 4, 0, width * 5, height));
        result.put("WH_PAWN", new Rect(width * 5, 0, width * 6, height));
        result.put("BL_KING", new Rect(0, height, width, height * 2));
        result.put("BL_QUEEN", new Rect(width, height, width * 2, height * 2));
        result.put("BL_BISHOP", new Rect(width * 2, height, width * 3, height * 2));
        result.put("BL_KNIGHT", new Rect(width * 3, height, width * 4, height * 2));
        result.put("BL_ROOK", new Rect(width * 4, height, width * 5, height * 2));
        result.put("BL_PAWN", new Rect(width * 5, height, width * 6, height * 2));
        return result;
    }

    private void generateSquares(int width, int height) {
        int coordsWidth = showCoordinates ? width / 32 + 2 : 0;
        int coordsHeight = showCoordinates ? width / 32 + 2 : 0;
        int squaresWidth = width - coordsWidth;
        int squaresHeight = width - coordsHeight;
        this.squareWidth = squaresWidth / 8;
        this.squareHeight = squaresHeight / 8;
        squares = new Rect[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                squares[x][y] = new Rect(
                        coordsWidth + (x * squareWidth),
                        (7 - y) * squareHeight,
                        coordsWidth + ((x + 1) * squareWidth),
                        (8 - y) * squareHeight);
            }
        }
        this.promotionMenu = new Rect(
                width / 2 - 2 * squareWidth,
                height / 2 - squareHeight / 2,
                width / 2 + 2 * squareWidth,
                height / 2 + squareHeight / 2);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false);
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            this.totalWidth = canvas.getWidth();
            this.totalHeight = canvas.getHeight();
            generateSquares(canvas.getWidth(), canvas.getHeight());
            draw(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            this.totalWidth = canvas.getWidth();
            this.totalHeight = canvas.getHeight();
            generateSquares(canvas.getWidth(), canvas.getHeight());
            draw(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Bitmap background = showCoordinates ? currentlyRotated ? bgCoordFlip : bgCoord : bg;
        paint.reset();
        paint.setAntiAlias(true);
        paint.setDither(true);
        canvas.drawBitmap(background, null,
                canvas.getClipBounds(), paint);
        if (boardState != null) {
            if (selectedPiece != null) {
                canvas.drawBitmap(selection, null, selectedSquare, paint);
            }
            for (Piece piece : boardState.getPieces()) {
                Rect src = pieceGfx.get(piece.getCode());
                int[] coord = indexToCoords(piece.getPosition());
                if (rotateUpperPieces && (currentlyRotated == (piece.getOwner() != Player.BLACK))) {
                    canvas.drawBitmap(piecesFlipped, src, squares[coord[0]][coord[1]], paint);
                } else {
                    canvas.drawBitmap(pieces, src, squares[coord[0]][coord[1]], paint);
                }
            }
            if (showLegalMoves && selectedPiece != null) {
                List<Integer> legalMoves = boardState.getLegalMoves(selectedPiece);
                for (int move : legalMoves) {
                    int[] coords = indexToCoords(move);
                    canvas.drawBitmap(legalMove, null,
                            squares[coords[0]][coords[1]], paint);
                }
            }
            if (promotionMove != null) {
                paint.setARGB(50, 0, 0, 0);
                canvas.drawRect(canvas.getClipBounds(), paint);
                paint.setARGB(255, 255, 255, 255);
                canvas.drawRect(promotionMenu, paint);
                String[] options = boardState.getCurrentPlayer() == Player.WHITE ?
                        WH_PROMOTION : BL_PROMOTION;
                for (int i = 0; i < options.length; i++) {
                    promotionSquare.set(promotionMenu.left + (squareWidth * i),
                            promotionMenu.top,
                            promotionMenu.left + (squareWidth * (i + 1)),
                            promotionMenu.bottom);
                    canvas.drawBitmap(pieces, pieceGfx.get(options[i]),
                            promotionSquare, paint);
                }
            }
            Integer[] lastMove = boardState.getLastMove();
            if (showLastMove && lastMove != null) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(squareWidth / 5);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setARGB(127, 127, 127, 255);
                int[] coord = indexToCoords(lastMove[0]);
                canvas.drawRect(squares[coord[0]][coord[1]], paint);
                coord = indexToCoords(lastMove[1]);
                canvas.drawRect(squares[coord[0]][coord[1]], paint);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (allowTouch && event.getAction() == MotionEvent.ACTION_DOWN) {
            int[] t = touchToCoord(event);
            int x = t[0];
            int y = t[1];
            Piece piece = boardState.getSquares()[coordsToIndex(x, y)];
            if (promotionMove != null) {
                int selectionIndex = coordsToIndex(selectedCoord[0], selectedCoord[1]);
                int option = optionSelected(event);
                if (option != -1) {
                    int promotionBits;
                    switch (option) {
                        case 0:
                            promotionBits = 2 << 10;
                            break;
                        case 1:
                            promotionBits = 5 << 10;
                            break;
                        case 2:
                            promotionBits = 6 << 10;
                            break;
                        default:
                            promotionBits = 7 << 10;
                            break;
                    }
                    moveReceiver.makeMove(selectionIndex, promotionMove | promotionBits);
                }
                promotionMove = null;
                deselect();
            } else if (selectedPiece == null) {
                if (piece != null) {
                    if (piece.getOwner() == boardState.getCurrentPlayer()) {
                        if (!boardState.getLegalMoves(piece).isEmpty()) {
                            this.selectedSquare = currentlyRotated ? squares[7 - x][7 - y] : squares[x][y];
                            this.selectedPiece = piece;
                            this.selectedCoord = t;
                        }
                    }
                }
            } else {
                int target = coordsToIndex(x, y);
                Integer move = boardState.getLegalMoves(selectedPiece).stream()
                        .filter(m -> (m & 0x77) == target).findAny().orElse(null);
                if (move != null) {
                    int selectionIndex = coordsToIndex(selectedCoord[0], selectedCoord[1]);
                    if (boardState.isPromotionMove(selectionIndex, move)) {
                        promotionMove = move & 0x77;
                    } else {
                        moveReceiver.makeMove(selectionIndex, move);
                        updateCurrentlyRotated();
                    }
                }
                if (promotionMove == null) {
                    deselect();
                }
            }
            invalidate();
            return true;
        }
        return false;
    }

    private void deselect() {
        this.selectedPiece = null;
        this.selectedSquare = null;
        this.selectedCoord = null;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int optionSelected(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (x > promotionMenu.left && x < promotionMenu.right &&
                y > promotionMenu.top && y < promotionMenu.bottom) {
            return (x - promotionMenu.left) / squareWidth;
        } else {
            return -1;
        }
    }

    private int[] touchToCoord(MotionEvent event) {
        int x = ((int) event.getX()) / squareWidth;
        int y = (totalHeight - ((int) event.getY())) / squareHeight;
        if (currentlyRotated) {
            return new int[]{7 - x, 7 - y};
        } else {
            return new int[]{x, y};
        }
    }

    private int coordsToIndex(int x, int y) {
        return (y << 4) + x;
    }

    private int[] indexToCoords(int index) {
        if (currentlyRotated) {
            return new int[]{7 - (index & 0x07), 7 - ((index & 0x70) >> 4)};
        } else {
            return new int[]{index & 0x07, (index & 0x70) >> 4};
        }
    }
}
