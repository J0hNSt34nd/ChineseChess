package Tool;

import Ai.simpleBoard;
import java.util.ArrayList;

public class Record {
    public boolean isUndoing = false;
    private  simpleBoard currentBoard = new simpleBoard();
    public ArrayList<int[][]> history;

    public Record() {
        history = new ArrayList<>();
        // 初始记录
        record(currentBoard);
    }

    public void reset() {
        currentBoard = new simpleBoard();
        history = new ArrayList<>();
        record (currentBoard);
    }

    public void updateGameState(int fromRow, int fromCol, int toRow, int toCol) {
        currentBoard.Move(fromRow - 1, fromCol - 1, toRow - 1, toCol - 1);
        record(currentBoard);
    }

    public void record(simpleBoard board) {
        int[][] original = board.getBoard();
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone(); // 克隆每一行
        }
        history.add(copy);
    }

    public void removeLast() {
        if (!history.isEmpty()) {
            history.remove(history.size() - 1);
        }
    }

    public void syncToLastHistory() {
        if (!history.isEmpty()) {
            int[][] lastState = history.get(history.size() - 1);
            currentBoard.setBoard(lastState);
        }
    }

    public simpleBoard getCurrentBoard()
    {
        return currentBoard;
    }

    public void synchronize(int[][] data)
    {
        currentBoard.setBoard(data);
        record(currentBoard);
    }
}