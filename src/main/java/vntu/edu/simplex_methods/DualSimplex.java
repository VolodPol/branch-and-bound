package vntu.edu.simplex_methods;

import java.util.Arrays;

import static java.lang.Math.abs;

public class DualSimplex {
    private final double[][] constraints;
    private final boolean[] signs;
    private final double[] freeVars;
    private double[] objective;

    public DualSimplex(double[][] constraints, boolean[] signs, double[] freeVars, double[] obj) {
        this.constraints = constraints;
        this.signs = signs;
        this.freeVars = freeVars;
        this.objective = obj;
    }

    public double[] solve(boolean max) {
        int[] basicVars = getBasicVars();
        standardToCanonicalForm(max);
        double[] basicVarCoEffs = new double[constraints.length];

        while (true) {
            int rowIdx = findResolvingRow();
            if (rowIdx == -1) break;

            double[] indexRow = calculateIndexRow(basicVarCoEffs);
            double[] estimates = calculateEstimates(indexRow, rowIdx);
            int colIdx = findResolvingColumn(estimates, rowIdx);
            if (colIdx == -1)
                throw new IllegalArgumentException("There is no optimal plan (F → ∞)");

            divideRowByPivot(rowIdx, colIdx);
            basicVarCoEffs[rowIdx] = objective[colIdx];
            basicVars[rowIdx] = colIdx;
            addRows(rowIdx, colIdx);
        }
        System.out.println(calculateObjValue(basicVarCoEffs));
        return getOptimalPlan(basicVars);
    }

    private int[] getBasicVars() {
        int[] basicVars = new int[constraints.length];
        for (int i = 0; i < constraints.length; i++)
            basicVars[i] = objective.length + i;
        return basicVars;
    }
    private void standardToCanonicalForm(boolean max) {
        objectiveFromMinToMax(max);
        extendConstraints();
    }
    private void objectiveFromMinToMax(boolean max) {
        checkObjective(max);
        checkConstraints();
    }
    private void extendConstraints() {
        extendWithBasicVars();
        extendObjective();
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
    private void extendObjective() {
        int oldLength = objective.length;
        double[] output = new double[oldLength + constraints.length];
        double[] temp = Arrays.copyOf(objective, oldLength);
        System.arraycopy(temp, 0, output, 0, oldLength);
        objective = output;
    }
    private void checkObjective(boolean max) {
        if (max) return;
        for (int i = 0; i < objective.length; i++)
            objective[i] *= -1;
    }
    private void checkConstraints() {// change sign from >= to <=, by: * (-1)
        for (int i = 0; i < constraints.length; i++)
            if (signs[i]) {
                for (int j = 0; j < constraints[i].length; j++)
                    constraints[i][j] *= -1;
                signs[i] = false;
                freeVars[i] *= -1;
            }
    }
    private int findResolvingRow() {
        int idx = -1;
        double saved = Double.MIN_VALUE;

        for (int i = 0; i < freeVars.length; i++)
            if (freeVars[i] < 0 && abs(freeVars[i]) > abs(saved)) {
                saved = freeVars[i];
                idx = i;
            }
        return idx;
    }
    private double[] calculateIndexRow(double[] basicVarCoEffs) {
        double[] indexRow = new double[constraints[0].length];
        for (int i = 0; i < constraints[0].length; i++) {
            double sum = 0;
            for (int j = 0; j < basicVarCoEffs.length; j++)
                sum += constraints[j][i] * basicVarCoEffs[j];
            indexRow[i] = sum - objective[i];
        }
        return indexRow;
    }
    private double[] calculateEstimates(double[] indexRow, int idx) {
        int length = objective.length;
        double[] estimates = new double[length];
        for (int i = 0; i < length; i++)
            estimates[i] = -indexRow[i] / constraints[idx][i];
        return estimates;
    }
    private int findResolvingColumn(double[] estimates, int rowIdx) {
        int idx = -1;
        double saved = Double.MAX_VALUE;
        for (int i = 0; i < estimates.length; i++) {
            if (estimates[i] > 0 && estimates[i] < saved) {// was: estimates[i] > 0
                saved = estimates[i];
                idx = i;
            } else if (estimates[i] == saved && rowIdx < constraints.length - 1) {
                double lastDivision = constraints[rowIdx + 1][idx] / constraints[rowIdx][idx];
                double newDivision = constraints[rowIdx + 1][i] / constraints[rowIdx][i];
                idx =  (lastDivision > 0 && lastDivision < newDivision) ? idx : i;
            }
        }
        return idx;
    }
    private void divideRowByPivot(int rowIdx, int colIdx) {
        double divider = constraints[rowIdx][colIdx];
        for (int i = 0; i < constraints[rowIdx].length; i++)
            constraints[rowIdx][i] /= divider;
        freeVars[rowIdx] /= divider;
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
    private double[] getOptimalPlan(int[] basicVars) {
        double[] output = new double[constraints[0].length];
        int idx = 0;
        for (int var : basicVars)
            output[var] = freeVars[idx++];
        return output;
    }
    private double calculateObjValue(double[] basicVarCoEffs) {
        double sum = 0;
        for (int i = 0; i < basicVarCoEffs.length; i++)
            sum += basicVarCoEffs[i] * freeVars[i];
        return sum;
    }
}