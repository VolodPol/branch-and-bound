package vntu.edu;

import vntu.edu.simplex_methods.BaseSimplex;
import vntu.edu.simplex_methods.DualSimplex;
import vntu.edu.simplex_methods.Simplex;

import java.util.*;

import static java.lang.Math.*;
import static java.lang.Math.abs;

public class Solver {
    private final double[][] constraints;
    private final double[] freeVars;
    private final BaseSimplex simplex;
    private Solution baseSolution;

    public Solver(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {
        this.constraints = Arrays.copyOf(constraints, constraints.length);
        this.freeVars = Arrays.copyOf(freeVars, freeVars.length);
        simplex = new Simplex(constraints, signs, freeVars, objective);
    }

    public Solution solve(boolean max) {
        baseSolution = simplex.solve(max);
        List<Solution> stageSolutions;

        do {
            stageSolutions = new LinkedList<>();
            double[] plan = baseSolution.optimalPlan();

            for (int i = 0; i < plan.length; i++) {//restrict for only valid variables
                if (hasFraction(plan[i])) {
                    computeNextPlan(plan, i, max, stageSolutions);
                }
            }
            updateBaseSolution(stageSolutions);
        } while (Arrays.stream(baseSolution.optimalPlan()).filter(this::hasFraction).count() != 0);
        return baseSolution;
    }

    private void updateBaseSolution(List<Solution> stageSolutions) {
        stageSolutions.sort(Comparator.comparingDouble(Solution::objValue).reversed());
        List<Solution> withIntegerPlan = stageSolutions.stream()
                .filter(solution -> {
                    for (double var : solution.optimalPlan())
                        if (hasFraction(var)) return false;
                    return true;
                }).toList();

        if (withIntegerPlan.isEmpty()) {
            baseSolution = stageSolutions.get(0);
            return;
        }
        List<Solution> integerSolutions = withIntegerPlan.stream()
                .filter(s -> !hasFraction(s.objValue()))
                .toList();

        if (integerSolutions.isEmpty()) {
            baseSolution = withIntegerPlan.get(0);
            return;
        }
        baseSolution = integerSolutions.get(0);
    }

    private void computeNextPlan(double[] plan, int idx, boolean max, List<Solution> accumulator) {
        double[][] updatedC = getUpdateConstraints(idx);
        double[] newFreeVars = getUpdatedFreeVars(plan[idx], (byte) 0);
        boolean[] signs = new boolean[newFreeVars.length];
        double[] objective = getUpdatedObjective();

        Solution left = compute(updatedC, signs, newFreeVars, objective, max);
        if (left.objValue() < baseSolution.objValue())
            accumulator.add(left);

        updatedC = getUpdateConstraints(idx);
        newFreeVars = getUpdatedFreeVars(plan[idx], (byte) 1);
        signs = new boolean[newFreeVars.length];
        signs[newFreeVars.length - 1] = true;
        objective = getUpdatedObjective();

        Solution right = compute(updatedC, signs, newFreeVars, objective, max);
        if (right.objValue() < baseSolution.objValue())
            accumulator.add(right);
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

    private double[][] getUpdateConstraints(int idx) {
        double[][] updatedC = new double[constraints.length + 1][constraints[0].length];
        for (int i = 0; i < constraints.length; i++) {
            System.arraycopy(constraints[i], 0, updatedC[i], 0, constraints[i].length);
        }
        updatedC[constraints.length][idx] = 1;
        return updatedC;
    }

    private double[] getUpdatedObjective() {
        double[] newObjective = new double[constraints[0].length];
        System.arraycopy(simplex.getObjective(), 0, newObjective, 0, constraints[0].length);
        return newObjective;
    }

    private double[] getUpdatedFreeVars(double plan, byte flag) {
        double[] newFreeVars = new double[freeVars.length + 1];
        System.arraycopy(freeVars, 0, newFreeVars, 0, freeVars.length);
        newFreeVars[freeVars.length] = flag == 0 ? floor(plan) : ceil(plan);
        return newFreeVars;
    }

    private boolean hasFraction(double element) {
        final double epsilon = 1e-10;

        double currentElement = abs(element);
        double currentFraction = 0;
        if (abs(ceil(currentElement) - currentElement) > epsilon)
            currentFraction = currentElement % 1;

        return currentFraction > epsilon;
    }
}
