package vntu.edu;

import vntu.edu.simplex_methods.BaseSimplex;
import vntu.edu.simplex_methods.DualSimplex;
import vntu.edu.simplex_methods.Simplex;
import vntu.edu.simplex_methods.Solution;

import java.util.Arrays;

import static java.lang.Math.*;
import static java.lang.Math.abs;

public class Solver {
    private final double[][] constraints;
    private final double[] freeVars;
    private Solution baseSolution;
    private BaseSimplex simplex;

    public Solver(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {
        this.constraints = Arrays.copyOf(constraints, constraints.length);
        this.freeVars = Arrays.copyOf(freeVars, freeVars.length);
        simplex = new Simplex(constraints, signs, freeVars, objective);
    }

    public Solution solve(boolean max) {
        baseSolution = simplex.solve(max);

        while (true) {
            int varIdx = biggestFraction(baseSolution.optimalPlan());
            if (varIdx == -1)
                break;

            baseSolution = computeNextPlan(baseSolution.optimalPlan(), varIdx, max);
        }

        return baseSolution;
    }

    private Solution computeNextPlan(double[] plan, int idx, boolean max) {
        double[][] updatedC = getUpdateConstraints(idx);
        double[] newFreeVars = getUpdateFreeVars(plan[idx], (byte) 0);
        boolean[] signs = new boolean[newFreeVars.length];
        double[] objective = getUpdateObjective();

        Solution left = compute(updatedC, signs, newFreeVars, objective, max);

        updatedC = getUpdateConstraints(idx);
        newFreeVars = getUpdateFreeVars(plan[idx], (byte) 1);
        signs = new boolean[newFreeVars.length];
        signs[newFreeVars.length - 1] = true;
        objective = getUpdateObjective();

        Solution right = compute(updatedC, signs, newFreeVars, objective, max);
        return selectBestSolution(left, right);
    }

    private Solution compute(double[][] c, boolean[] s, double[] f, double[] o, boolean max) {
        Solution solution;
        BaseSimplex solver = new Simplex(c, s, f, o);
        if (Arrays.stream(f).allMatch(operand -> operand > 0)) {
            solution = solver.solve(max);
        } else {
            try {
                solution = new DualSimplex(c, s, f, o).solve(max);
            } catch (IllegalArgumentException exception) {
                solution = solver.solve(max);
            }
        }
        return solution;
    }

    private Solution selectBestSolution(Solution left, Solution right) {
        return left.objValue() > right.objValue() ? left : right;
    }

    private double[][] getUpdateConstraints(int idx) {
        double[][] updatedC = new double[constraints.length + 1][constraints[0].length];
        for (int i = 0; i < constraints.length; i++) {
            System.arraycopy(constraints[i], 0, updatedC[i], 0, constraints[i].length);
        }
        updatedC[constraints.length][idx] = 1;
        return updatedC;
    }

    private double[] getUpdateObjective() {
        double[] newObjective = new double[constraints[0].length];
        System.arraycopy(simplex.getObjective(), 0, newObjective, 0, constraints[0].length);
        return newObjective;
    }

    private double[] getUpdateFreeVars(double plan, byte flag) {
        double[] newFreeVars = new double[freeVars.length + 1];
        System.arraycopy(freeVars, 0, newFreeVars, 0, freeVars.length);
        newFreeVars[freeVars.length] = flag == 0 ? floor(plan) : ceil(plan);
        return newFreeVars;
    }

    private int biggestFraction(double[] array) {
        int index = -1;
        double maxFraction = 0;
        final double epsilon = 1e-10;

        for (int i = 0; i < array.length; i++) {
            double currentElement = abs(array[i]);
            double next = ceil(currentElement);
            double prev = floor(currentElement);

            double currentFraction = 0;
            if (abs(currentElement - prev) < abs(next - currentElement))
                currentFraction = currentElement % 1;
            else if (abs(next - currentElement) > epsilon)
                currentFraction = currentElement % 1;

            if (currentFraction > epsilon && currentFraction > maxFraction) {
                maxFraction = currentFraction;
                index = i;
            }
        }
        return index;
    }
}
