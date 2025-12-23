package Pieces;

import Effect.JavaFXSound;
import Tool.Tool;
import Tool.Unit;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import main.Board;

public class Piece {

    JavaFXSound sound = new JavaFXSound();

    public double radius;
    protected int x, y;
    protected int _rows, _cols;
    protected int speedX;
    protected int speedY;
    protected int team;

    public int pieceValue () {
        return 0;
    }

    public static int TYPE_GENERAL = 100000000;
    public static int TYPE_CANNON = 500;
    public static int TYPE_TANK = 900;
    public static int TYPE_HORSE = 450;
    public static int TYPE_XIANG = 200;
    public static int TYPE_GUARD = 200;
    public static int TYPE_SOLDIER = 100;

    protected int preRow, preCol;

    Tool tool;

    protected boolean isMoving = false;
    public boolean chosable = false;

    protected Image image;

    //这段先留着
    public void setPreRow(int preRow)
    {
        this.preRow = preRow;
    }

    public void setPreCol(int preCol)
    {
        this.preCol = preCol;
    }

    public void draw(GraphicsContext g2) {
        if (image != null) {
            g2.drawImage(image, x, y, Board.tileSize, Board.tileSize);
        }
    }

    public boolean canMoveTo(int newRow,int newCol, Board board) {
        if (Math.abs(newCol - _cols) + Math.abs(newRow - _rows) == 1 && opponent(board.getPieceAt(newRow,newCol))) {
            return true;
        }

        return false;
    }

    public boolean moveTo(int newRow, int  newCol, Board board) {

        if (canMoveTo(newRow, newCol, board))
        {

            sound.playEffect();

            board.movingPiece = this;

            board.setDelatingPiece(board.getPieceAt(newRow, newCol));

            preRow = _rows;
            preCol = _cols;

            int targetX = Unit.coltoX(newCol, 0);
            int targetY = Unit.rowtoY(newRow, 0);

            speedX = calculateSpeed(targetX, x, 20);
            speedY = calculateSpeed(targetY, y, 20);

            _rows = newRow;
            _cols = newCol;

            isMoving = true;

            board.pieceMoving = true;
            board.movingPiece = this;

            return true;
        } else {
            return false;
        }
    }

    public void forceTo(int newRow, int  newCol, Board board)
    {

        sound.playEffect();

        board.movingPiece = this;

        preRow = _rows;
        preCol = _cols;

        int targetX = Unit.coltoX(newCol, 0);
        int targetY = Unit.rowtoY(newRow, 0);

        speedX = calculateSpeed(targetX, x, 20);
        speedY = calculateSpeed(targetY, y, 20);

        _rows = newRow;
        _cols = newCol;

        isMoving = true;

        board.pieceMoving = true;
        board.movingPiece = this;
    }

    private int calculateSpeed(int target, int current, int frames) {
        int distance = target - current;
        if (distance == 0) return 0;

        int speed = distance / frames;

        if (speed == 0) {
            return distance > 0 ? 1 : -1;
        }

        if (Math.abs(speed) > 4) {
            return distance > 0 ? 4 : -4;
        }

        return speed;
    }

    public boolean opponent(Piece piece){
        if (piece == null)
            return true;
        return piece.team != team;
    }

    public void update() {

    }

    public int getRows() {
        return _rows;
    }

    public int getCols() {
        return _cols;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setRow(int rows){
        _rows = rows;
        y = Unit.rowtoY(_rows, 0);
    }

    public void setCol(int cols) {
        _cols = cols;
        x = Unit.coltoX(_cols, 0);
    }

    public void addX(int interval) {
        x += interval;
    }

    public void addY(int interval) {
        y += interval;
    }

    public void switchMovingStatus(boolean bool) {
        isMoving = bool;
    }

    public boolean getMovingStatus() {
        return isMoving;
    }

    public int getSpeedX() {
        return speedX;
    }

    public int getSpeedY() {
        return speedY;
    }

    public void setX(int newX) {
        x = newX;
    }

    public void setY(int newY) {
        y = newY;
    }

    public int getTeam() {
        return team;
    }

    public int[][] getPst() {
        return null;
    }

    public String pieceType() {
        return this.getClass().toString();
    }

    public int getPreCol() {
        return preCol;
    }

    public int getPreRow() {
        return preRow;
    }

    public boolean isChecking(int newRow,int newCol,Board board)
    {
        if (team == 0) {
            if (canMoveTo(newRow, newCol, board) && board.getPieceAt(newRow, newCol).getClass() == RedGeneralPiece.class)
                return true;
            return false;
        }
        else {
            if (canMoveTo(newRow, newCol, board) && board.getPieceAt(newRow, newCol).getClass() == BlackGeneralPiece.class)
                return true;
            return false;
        }
    }

    public boolean isAttacking(int newRow,int newCol,Board board)
    {
        if(team==0)
        {
            if(canMoveTo(newRow,newCol,board))
            {
                return true;
            }
            return false;
        }
        else
        {
            if(canMoveTo(newRow,newCol,board))
            {
                return true;
            }
            return false;
        }
    }
}