package vntu.edu.simplex_methods;

import lombok.Getter;
import vntu.edu.Solution;

import java.util.Arrays;

@Getter
public abstract class BaseSimplex {
    protected final double[][] constraints;
    protected final boolean[] signs;
    protected final double[] freeVars;
    protected double[] objective;

    public BaseSimplex(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {
        this.constraints = constraints;
        this.signs = signs;
        this.freeVars = freeVars;
        this.objective = objective;
    }

    public abstract Solution solve(boolean max);

    protected double[] getOptimalPlan(int[] basicVars) {
        double[] output = new double[constraints[0].length];
        int idx = 0;
        for (int var : basicVars)
            output[var] = freeVars[idx++];

        return output;
    }

    protected void standardToCanonicalForm(boolean max) {
        objectiveFromMinToMax(max);
        extendConstraints();
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

    protected void divideRowByPivot(int rowIdx, int colIdx) {
        double divider = constraints[rowIdx][colIdx];
        for (int i = 0; i < constraints[rowIdx].length; i++)
            constraints[rowIdx][i] /= divider;
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

    protected int[] getBasicVars() {
        int[] basicVars = new int[constraints.length];
        for (int i = 0; i < constraints.length; i++)
            basicVars[i] = objective.length + i;
        return basicVars;
    }

    protected void addRows(int rowIdx, int colIdx) {
        for (int i = 0; i < constraints.length; i++)
            if (rowIdx != i) {
                freeVars[i] += freeVars[rowIdx] * (- constraints[i][colIdx]);

                double multiplier = - constraints[i][colIdx];
                for (int j = 0; j < constraints[rowIdx].length; j++)
                    constraints[i][j] += constraints[rowIdx][j] * multiplier;
            }
    }

    protected double calculateObjectiveValue(double[] basicVarCoEffs) {
        double sum = 0;
        for (int i = 0; i < basicVarCoEffs.length; i++) {
            sum += basicVarCoEffs[i] * freeVars[i];
        }
        return sum;
    }
}
