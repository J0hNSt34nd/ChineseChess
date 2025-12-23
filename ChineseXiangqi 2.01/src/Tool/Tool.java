package Tool;

public class Tool {

    int [][] matrixInverse = new int[10][9];

    public int[][] verticleInverse(int[][] matrix) {
        for (int i = 0; i < 10; i++) {
            matrixInverse[i] = matrix[9 - i];
        }

        return matrixInverse;
    }
}
