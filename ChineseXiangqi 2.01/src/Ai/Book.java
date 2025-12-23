package Ai;

import main.Board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Book {

    private final simpleBoard currentBoard = new simpleBoard();
    private final List<int[]> moveHistory = new ArrayList<>();
    private volatile Map<Long, List<Move>> data = new HashMap<>();
    private final Random random = new Random();

    public Book() {
        currentBoard.reset();
    }

    public void reset() {
        currentBoard.reset();
    }

    public void updateGameState(int fromRow, int fromCol, int toRow, int toCol) {
        moveHistory.add(new int[]{fromRow, fromCol, toRow, toCol});
        currentBoard.Move(fromRow - 1, fromCol - 1, toRow - 1, toCol - 1);
    }

    public void back() {
        if (!moveHistory.isEmpty()) {
            moveHistory.remove(moveHistory.size() - 1);
            currentBoard.reset();
            for (int[] move : moveHistory) {
                currentBoard.Move(move[0] - 1, move[1] - 1, move[2] - 1, move[3] - 1);
            }
        }
    }


    public CompletableFuture<Integer> load(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            if (inputStream == null) {
                System.err.println("【错误】未找到开局库文件流！");
                return 0;
            }

            System.out.println("开始加载开局库...");
            Map<Long, Set<Move>> localData = new HashMap<>();
            int totalGame = 0;

            //InputStreamReader 读取流
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                boolean newGame = true;
                simpleBoard simBoard = new simpleBoard();
                Pattern movePatten = Pattern.compile("[A-I][0-9]-[A-I][0-9]");

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[Game")) {
                        newGame = true;
                        totalGame++;
                        if (totalGame % 5000 == 0) System.out.println("已处理 " + totalGame + " 局...");
                        continue;
                    }
                    if (newGame) {
                        simBoard.reset();
                        newGame = false;
                    }

                    Matcher matcher = movePatten.matcher(line);
                    while (matcher.find()) {
                        String moveStr = matcher.group();
                        String[] parts = moveStr.split("-");
                        if (parts.length != 2) continue;

                        int fromCol = parts[0].charAt(0) - 'A';
                        int fromRow = 9 - (parts[0].charAt(1) - '0');
                        int toCol = parts[1].charAt(0) - 'A';
                        int toRow = 9 - (parts[1].charAt(1) - '0');

                        if (fromRow < 0 || fromRow >= 10 || fromCol < 0 || fromCol >= 9 || toRow < 0 || toRow >= 10 || toCol < 0 || toCol >= 9)
                            continue;

                        Move move = new Move(fromRow, fromCol, toRow, toCol);

                        long currentHash = simBoard.getHash();
                        localData.computeIfAbsent(currentHash, k -> new HashSet<>()).add(move);

                        if (!simBoard.Move(fromRow, fromCol, toRow, toCol)) {
                            newGame = true;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Map<Long, List<Move>> listMap = new HashMap<>(localData.size());
            for (Map.Entry<Long, Set<Move>> entry : localData.entrySet()) {
                listMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            System.gc();
            this.data = listMap;
            System.out.println("开局库加载完成，共载入局面数: " + listMap.size());
            return listMap.size();
        });
    }

    public Move query() {
        long currentHash = currentBoard.getHash();
        if (data.containsKey(currentHash)) {
            List<Move> moves = data.get(currentHash);
            if (moves != null && !moves.isEmpty()) {
                Move rawMove = moves.get(random.nextInt(moves.size()));
                return new Move(rawMove.oriRow + 1, rawMove.oriCol + 1, rawMove.newRow + 1, rawMove.newCol + 1);
            }
        }
        return null;
    }

    public Move query(long hash) {
        if (data.containsKey(hash)) {
            List<Move> moves = data.get(hash);
            if (moves != null && !moves.isEmpty())
                return moves.get(random.nextInt(moves.size()));
        }
        return null;
    }

    public simpleBoard getBoard() {
        return currentBoard;
    }
}