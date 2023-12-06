package vntu.edu.simplex_methods;

import java.util.*;

public class Simplex {
    private final double[][] constraints;
    private final boolean[] signs;
    private final double[] freeVars;
    private double[] objective;

    public Simplex(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {
        this.constraints = constraints;
        this.signs = signs;
        this.freeVars = freeVars;
        this.objective = objective;
    }

    public double[] solve(boolean max) {
        int[] basicVars = getBasicVars();
        objectiveFromMinToMax(max);
        extendConstraints();
        double[] basicVarCoEffs = new double[constraints.length];
        double[] indexRow = calculateIndexRow(basicVarCoEffs);

        int colIdx = findNegative(indexRow);
        while (colIdx != -1) {
            double[] estimates = calculateEstimates(colIdx);
            int rowIdx = findKeyRow(colIdx, estimates);
            if (rowIdx == -1)
                throw new IllegalArgumentException("There is no optimal plan (F → ∞)");
            //divide row
            divideRowByPivot(rowIdx, colIdx);

            basicVarCoEffs[rowIdx] = objective[colIdx];
            basicVars[rowIdx] = colIdx;

            //Jordan-Gauss
            addRows(rowIdx, colIdx);

            // update index row
            indexRow = calculateIndexRow(basicVarCoEffs);
            colIdx = findNegative(indexRow);
        }
        System.out.println(calculateObjectiveValue(basicVarCoEffs));
        return getOptimalPlan(basicVars);
    }

    private double[] getOptimalPlan(int[] basicVars) {
        double[] output = new double[constraints[0].length];
        int idx = 0;
        for (int var : basicVars)
            output[var] = freeVars[idx++];

        return output;
    }

    private void extendConstraints() {
        extendWithBasicVars();
        extendObjective();
    }

    private void divideRowByPivot(int rowIdx, int colIdx) {
        double divider = constraints[rowIdx][colIdx];
        for (int i = 0; i < constraints[rowIdx].length; i++) {
            constraints[rowIdx][i] /= divider;
        }
        freeVars[rowIdx] /= divider;
    }

    private void objectiveFromMinToMax(boolean max) {
        checkObjective(max);
        checkConstraints();
    }

    private void checkObjective(boolean max) {
        if (max) return;
        for (int i = 0; i < objective.length; i++)
            objective[i] *= -1;
    }

    private void checkConstraints() {
        for (int i = 0; i < constraints.length; i++)
            if (signs[i]) {
                for (int j = 0; j < constraints[i].length; j++)
                    constraints[i][j] *= -1;
                signs[i] = false;
                freeVars[i] *= -1;
            }
    }

    private int[] getBasicVars() {
        int[] basicVars = new int[constraints.length];
        for (int i = 0; i < constraints.length; i++) {
            basicVars[i] = objective.length + i;
        }
        return basicVars;
    }

    private void addRows(int rowIdx, int colIdx) {
        for (int i = 0; i < constraints.length; i++)
            if (rowIdx != i) {
                freeVars[i] += freeVars[rowIdx] * (- constraints[i][colIdx]);

                double multiplier = - constraints[i][colIdx];
                for (int j = 0; j < constraints[rowIdx].length; j++)
                    constraints[i][j] += constraints[rowIdx][j] * multiplier;
            }
    }

    private int findKeyRow(int smallestColIdx, double[] estimates) {
        int idx = -1;
        double leastEstimation = Double.MAX_VALUE;

        for (int i = 0; i < estimates.length; i++) {
            if (constraints[i][smallestColIdx] > 0 && estimates[i] <= leastEstimation) {
                idx = i;
                leastEstimation = estimates[i];
            }
        }
        return idx;
    }

    private double[] calculateEstimates(int idx) {
        int length = constraints.length;
        double[] estimates = new double[length];

        for (int i = 0; i < length; i++) {
            estimates[i] = freeVars[i] / constraints[i][idx];
        }
        return estimates;
    }

    private double calculateObjectiveValue(double[] basicVarCoEffs) {
        double sum = 0;
        for (int i = 0; i < basicVarCoEffs.length; i++) {
            sum += basicVarCoEffs[i] * freeVars[i];
        }
        return sum;
    }

    private int findNegative(double[] indexRow) {
        int idx = -1;
        double smallest = 0;
        for (int i = 0; i < indexRow.length; i++) {
            if (indexRow[i] <= 0 && smallest > indexRow[i]){
                smallest = indexRow[i];
                idx = i;
            }
        }
        return idx;
    }

    private void extendObjective() {
        int oldLength = objective.length;
        double[] output = new double[oldLength + constraints.length];
        double[] temp = Arrays.copyOf(objective, oldLength);

        System.arraycopy(temp, 0, output, 0, oldLength);
        objective = output;
    }
    private void extendWithBasicVars() {
        int rows = constraints.length;
        int cols = constraints[0].length;

        for (int i = 0; i < rows; i++) {
            double[] old = Arrays.copyOf(constraints[i], cols);
            double[] extension = new double[rows];
            extension[i] = 1;
            constraints[i] = new double[cols + rows];

            System.arraycopy(old, 0, constraints[i], 0, cols);
            System.arraycopy(extension, 0, constraints[i], cols, rows);
        }
    }

    private double[] calculateIndexRow(double[] basicVarCoEffs) {
        double[] indexRow = new double[constraints[0].length];

        for (int i = 0; i < constraints[0].length; i++) {
            double sum = 0;
            for (int j = 0; j < basicVarCoEffs.length; j++) {
                sum += constraints[j][i] * basicVarCoEffs[j];
            }
            indexRow[i] = sum - objective[i];
        }
        return indexRow;
    }
}