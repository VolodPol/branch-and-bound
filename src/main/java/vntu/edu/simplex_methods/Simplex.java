package vntu.edu.simplex_methods;

import vntu.edu.Solution;
import static java.util.Arrays.copyOf;

public class Simplex extends BaseSimplex {
    public Simplex(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {
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
        Solution solution = new Solution(calculateObjectiveValue(basicVarCoEffs), getOptimalPlan(basicVars));
        System.out.println(solution);
        return solution;
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