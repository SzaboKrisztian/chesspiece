package com.krisztianszabo.chesspiece;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
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
    private ChessActivity parentActivity;
    private BoardState boardState;
    private boolean currentlyRotated = false;
    private boolean rotateBoard = false;
    private boolean rotateUpperPieces = false;
    private boolean rotateBoardOnEachMove = false;
    private boolean showLegalMoves = true;
    private SurfaceHolder holder;
    private Resources res = getContext().getResources();
    private Bitmap background = BitmapFactory.decodeResource(res, R.drawable.board);
    private Bitmap pieces = BitmapFactory.decodeResource(res, R.drawable.chess_pieces);
    private Bitmap piecesFlipped = BitmapFactory.decodeResource(res, R.drawable.chess_pieces_flipped);
    private Bitmap selection = BitmapFactory.decodeResource(res, R.drawable.selection);
    private Bitmap legalMove = BitmapFactory.decodeResource(res, R.drawable.legal_move);
    private Map<String, Rect> pieceGfx = generateMap();
    private Paint defaultPaint;
    private Paint debugPaint;
    private Paint promotionPaint;
    private int totalWidth;
    private int totalHeight;
    private int squareWidth;
    private int squareHeight;
    private Matrix matrix;
    private Rect[][] squares;
    private Rect promotionMenu;
    private Rect promotionSquare = new Rect();
    private final int MARGIN = 0;
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

    private void init() {
        this.holder = getHolder();
        this.holder.addCallback(this);
        this.setOnTouchListener(this);
        this.defaultPaint = new Paint();
        this.defaultPaint.setAntiAlias(true);
        this.defaultPaint.setDither(true);
        this.matrix = new Matrix();
        this.matrix.setRotate(180);
        this.promotionPaint = new Paint();
        this.debugPaint = new Paint();
        this.debugPaint.setARGB(63, 255, 0, 0);
    }

    public void setParentActivity(ChessActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void setBoardState(BoardState boardState) {
        this.boardState = boardState;
        this.invalidate();
    }

    public void setDisplaySettings(DisplaySettings settings) {
        rotateBoard = settings.isRotateBoard();
        rotateBoardOnEachMove = settings.isRotateBoardOneEachMove();
        rotateUpperPieces = settings.isRotateTopPieces();
        showLegalMoves = settings.isShowLegalMoves();
        updateCurrentlyRotated();
        invalidate();
    }

    private void updateCurrentlyRotated() {
        currentlyRotated = rotateBoard != (rotateBoardOnEachMove && boardState.getCurrentPlayer() == Player.BLACK);
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

    private void generateSquares(Canvas canvas) {
        this.totalWidth = canvas.getWidth() - MARGIN * 2;
        this.totalHeight = canvas.getHeight() - MARGIN * 2;
        this.squareWidth = this.totalWidth / 8;
        this.squareHeight = this.totalHeight / 8;
        squares = new Rect[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                squares[x][y] = new Rect(x * squareWidth, (7 - y) * squareHeight,
                        (x + 1) * squareWidth, (8 - y) * squareHeight);
            }
        }
        this.promotionMenu = new Rect(
                totalWidth / 2 - 2 * squareWidth,
                totalHeight / 2 - squareHeight / 2,
                totalWidth / 2 + 2 * squareWidth,
                totalHeight / 2 + squareHeight / 2);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false);
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            generateSquares(canvas);
            draw(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.totalWidth = width;
        this.totalHeight = height;
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            generateSquares(canvas);
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
        canvas.drawBitmap(background, null, canvas.getClipBounds(), defaultPaint);
        if (boardState != null) {
            if (selectedPiece != null) {
                canvas.drawBitmap(selection, null, selectedSquare, defaultPaint);
            }
            for (Piece piece : boardState.getPieces()) {
                Rect src = pieceGfx.get(piece.getCode());
                int[] coord = indexToCoords(piece.getPosition());
                if (rotateUpperPieces && (currentlyRotated == (piece.getOwner() != Player.BLACK))) {
                    canvas.drawBitmap(piecesFlipped, src, squares[coord[0]][coord[1]], defaultPaint);
                } else {
                    canvas.drawBitmap(pieces, src, squares[coord[0]][coord[1]], defaultPaint);
                }
            }
            if (showLegalMoves && selectedPiece != null) {
                List<Integer> legalMoves = boardState.getLegalMoves(selectedPiece);
                for (int move : legalMoves) {
                    int[] coords = indexToCoords(move);
                    canvas.drawBitmap(legalMove, null,
                            squares[coords[0]][coords[1]], defaultPaint);
                }
            }
            if (promotionMove != null) {
                promotionPaint.setARGB(50, 0, 0, 0);
                canvas.drawRect(canvas.getClipBounds(), promotionPaint);
                promotionPaint.setARGB(255, 255, 255, 255);
                canvas.drawRect(promotionMenu, promotionPaint);
                String[] options = boardState.getCurrentPlayer() == Player.WHITE ?
                        WH_PROMOTION : BL_PROMOTION;
                for (int i = 0; i < options.length; i++) {
                    promotionSquare.set(promotionMenu.left + (squareWidth * i),
                            promotionMenu.top,
                            promotionMenu.left + (squareWidth * (i + 1)),
                            promotionMenu.bottom);
                    canvas.drawBitmap(pieces, pieceGfx.get(options[i]),
                            promotionSquare, defaultPaint);
                }
            }
//            for (int i = 8; i < 128; i++) {
//                if ((i & 8) == 0) i += 8;
//                if (boardState.getSquares()[i] != null) {
//                    int[] coords = indexToCoords(i ^ 8);
//                    canvas.drawRect(squares[coords[0]][coords[1]], debugPaint);
//                }
//            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
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
                    parentActivity.makeMove(selectionIndex, promotionMove | promotionBits);
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
                        parentActivity.makeMove(selectionIndex, move);
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
        int x = (int) event.getX() - MARGIN;
        int y = (int) event.getY() - MARGIN;
        if (x > promotionMenu.left && x < promotionMenu.right &&
                y > promotionMenu.top && y < promotionMenu.bottom) {
            return (x - promotionMenu.left) / squareWidth;
        } else {
            return -1;
        }
    }

    private int[] touchToCoord(MotionEvent event) {
        int x = ((int) event.getX() - MARGIN) / squareWidth;
        int y = (totalHeight - ((int) event.getY() - MARGIN)) / squareHeight;
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
