package com.krisztianszabo.chesspiece;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.krisztianszabo.chesspiece.model.BoardState;
import com.krisztianszabo.chesspiece.model.Piece;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardView extends SurfaceView implements SurfaceHolder.Callback, SurfaceView.OnTouchListener {

    private boolean rotate = false;
    private SurfaceHolder holder;
    private Resources res = getContext().getResources();
    private Bitmap background = BitmapFactory.decodeResource(res, R.drawable.board);
    private Bitmap pieces = BitmapFactory.decodeResource(res, R.drawable.chess_pieces);
    private Bitmap selection = BitmapFactory.decodeResource(res, R.drawable.selection);
    private Bitmap legalMove = BitmapFactory.decodeResource(res, R.drawable.legal_move);
    private Map<String, Rect> pieceGfx = generateMap();
    private BoardState boardState;
    private Paint defaultPaint;
    private int totalWidth;
    private int totalHeight;
    private int squareWidth;
    private int squareHeight;
    private Matrix matrix;
    private RectF source, dest;
    private Rect[][] squares;
    private final int MARGIN = 0;
    private Rect selectedSquare = null;
    private Piece selectedPiece = null;
    private int[] selectedCoord = null;

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
        this.boardState = new BoardState();
        this.boardState.initStandardChess();
        this.matrix = new Matrix();
        this.matrix.setRotate(180);
        this.source = new RectF();
        this.dest = new RectF();
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
                canvas.drawBitmap(pieces, src, squares[coord[0]][coord[1]], defaultPaint);
            }
            if (selectedPiece != null) {
                List<Integer> legalMoves = boardState.getLegalMoves(selectedPiece);
                for (int move : legalMoves) {
                    int[] coords = indexToCoords(move);
                    canvas.drawBitmap(legalMove, null,
                            squares[coords[0]][coords[1]], defaultPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int[] t = touchToCoord(event);
            int x = t[0];
            int y = t[1];
            Piece piece = boardState.getSquares()[coordsToIndex(x, y)];
            if (selectedPiece == null) {
                if (piece != null) {
                    if (piece.getOwner() == boardState.getCurrentPlayer()) {
                        if (!boardState.getLegalMoves(piece).isEmpty()) {
                            this.selectedSquare = squares[x][y];
                            this.selectedPiece = piece;
                            this.selectedCoord = t;
                        }
                    }
                }
            } else {
                if (boardState.getLegalMoves(selectedPiece).contains(t)) {
                    boardState = boardState.makeMove(
                            coordsToIndex(selectedCoord[0], selectedCoord[1]), coordsToIndex(x, y));
                    invalidate();
                }
                deselect();
            }
            this.invalidate();
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

    private int[] touchToCoord(MotionEvent event) {
        return new int[]{((int)event.getX() - MARGIN) / squareWidth,
                (totalHeight - ((int)event.getY() - MARGIN)) / squareHeight};
    }

    private int coordsToIndex(int x, int y) {
        return (y << 4) + x;
    }

    private int[] indexToCoords(int index) {
        return new int[]{index & 0x07, (index & 0x70) >> 4};
    }
}
