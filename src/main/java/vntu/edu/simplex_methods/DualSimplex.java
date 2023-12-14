package vntu.edu.simplex_methods;

import vntu.edu.Solution;
import static java.lang.Math.abs;
import static java.util.Arrays.copyOf;

public class DualSimplex extends BaseSimplex {
    public DualSimplex(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {
        super(
                cloneMatrix(constraints),
                copyOf(signs, signs.length),
                copyOf(freeVars, freeVars.length),
                copyOf(objective, objective.length)
        );
    }

    @Override
    public Solution solve(boolean max) {
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
        Solution solution = new Solution(calculateObjectiveValue(basicVarCoEffs), getOptimalPlan(basicVars));
        System.out.println(solution);
        return solution;
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
            estimates[i] = -indexRow[i] / (constraints[idx][i] < 0
                    ? constraints[idx][i]
                    : 0);
        return estimates;
    }

    private int findResolvingColumn(double[] estimates, int rowIdx) {
        int idx = -1;
        double saved = Double.MAX_VALUE;
        for (int i = 0; i < estimates.length; i++) {
            if (estimates[i] < saved) {
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
}