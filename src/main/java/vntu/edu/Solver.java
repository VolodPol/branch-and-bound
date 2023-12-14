package vntu.edu;

import vntu.edu.simplex_methods.BaseSimplex;
import vntu.edu.simplex_methods.DualSimplex;
import vntu.edu.simplex_methods.Simplex;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.*;
import static java.lang.Math.abs;

public class Solver {
    private final Model initialModel;
    private final BaseSimplex simplex;
    private final Map<Solution, Model> cache;
    private Solution baseSolution;

    public Solver(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {
        this.initialModel = new Model(constraints, signs, freeVars, objective);
        this.simplex = new Simplex(constraints, signs, freeVars, objective);
        this.cache = new HashMap<>();
    }

    public Solution solve(boolean max) {
        baseSolution = simplex.solve(max);
        List<Solution> stageSolutions;

        do {
            System.out.print("=".repeat(60) + "\n");
            stageSolutions = new LinkedList<>();
            double[] plan = baseSolution.optimalPlan();

            for (int i = 0; i < initialModel.constraints()[0].length; i++) {
                if (hasFraction(plan[i])) {
                    Solution left = computeLeftSolution(baseSolution, plan, i, max);
                    Solution right = computeRightSolution(baseSolution, plan, i, max);
                    addSolutions(List.of(left, right), stageSolutions);
                }
            }
            updateBaseSolution(stageSolutions);
            System.out.print("=".repeat(60) + "\n");
        } while (Arrays.stream(baseSolution.optimalPlan()).filter(this::hasFraction).count() != 0);
        System.out.printf("Optimal integer plan: %s", baseSolution);
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
            if (!stageSolutions.isEmpty())
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

    private Solution computeLeftSolution(Solution last, double[] plan, int idx, boolean max) {// if x < 0 -> assign old baseSolution
        double[][] updatedC;
        double[] newFreeVars, objective;
        boolean[] signs;
        Model leftModel;

        leftModel = cache.get(last);
        if (leftModel != null) {
            updatedC = getUpdateConstraints(leftModel.constraints, idx);
            newFreeVars = getUpdatedFreeVars(leftModel.freeVars, plan[idx], Math::floor);
            signs = getUpdatedSigns(leftModel.signs, newFreeVars.length);
            objective = getUpdatedObjective(leftModel.objective);
        } else {
            updatedC = getUpdateConstraints(initialModel.constraints(), idx);
            newFreeVars = getUpdatedFreeVars(initialModel.freeVars(), plan[idx], Math::floor);
            signs = getUpdatedSigns(initialModel.signs(), newFreeVars.length);
            objective = getUpdatedObjective(simplex.getObjective());
        }
        leftModel = new Model(updatedC, signs, newFreeVars, objective);
        Solution left = compute(leftModel, max);
        cache.put(left, leftModel);
        return left;
    }

    private Solution computeRightSolution(Solution last, double[] plan, int idx, boolean max) {
        double[][] updatedC;
        double[] newFreeVars, objective;
        boolean[] signs;
        Model rightModel;

        rightModel = cache.get(last);
        if (rightModel != null) {
            updatedC = getUpdateConstraints(rightModel.constraints, idx);
            newFreeVars = getUpdatedFreeVars(rightModel.freeVars, plan[idx], Math::ceil);
            signs = getUpdatedSigns(rightModel.signs, newFreeVars.length);
            signs[newFreeVars.length - 1] = true;
            objective = getUpdatedObjective(rightModel.objective);
        } else {
            updatedC = getUpdateConstraints(initialModel.constraints(), idx);
            newFreeVars = getUpdatedFreeVars(initialModel.freeVars(), plan[idx], Math::ceil);
            signs = getUpdatedSigns(initialModel.signs(), newFreeVars.length);
            signs[newFreeVars.length - 1] = true;
            objective = getUpdatedObjective(simplex.getObjective());
        }
        rightModel = new Model(updatedC, signs, newFreeVars, objective);
        Solution right = compute(rightModel, max);
        cache.put(right, rightModel);
        return right;
    }

    private void addSolutions(List<Solution> source, List<Solution> destination) {
        source.forEach(solution -> {
            if (solution.objValue() < baseSolution.objValue() && solution.objValue() != 0)
                destination.add(solution);
        });
    }

    private Solution compute(Model m, boolean max) {
        Solution solution;
        BaseSimplex solver;
        boolean isDual = false;
        for (int i = 0; i < m.freeVars().length; i++)
            if (m.freeVars()[i] < 0 && !m.signs()[i]) {//m.freeVars()[i] > 0 && m.signs()[i])
                isDual = true;
                break;
            }
        solver = isDual
                ? new DualSimplex(m.constraints(), m.signs(), m.freeVars(), m.objective())
                : new Simplex(m.constraints(), m.signs(), m.freeVars(), m.objective());
        try {
            solution = solver.solve(max);
        } catch (IllegalArgumentException iae) {
            solution = solver instanceof Simplex
                    ? new DualSimplex(m.constraints(), m.signs(), m.freeVars(), m.objective()).solve(max)
                    : new Simplex(m.constraints(), m.signs(), m.freeVars(), m.objective()).solve(max);
        }
        return solution;
    }

    private double[][] getUpdateConstraints(double[][] lastConstraints, int idx) {
        double[][] updatedC = new double[lastConstraints.length + 1][lastConstraints[0].length];
        for (int i = 0; i < lastConstraints.length; i++) {
            System.arraycopy(lastConstraints[i], 0, updatedC[i], 0, lastConstraints[i].length);
        }
        updatedC[lastConstraints.length][idx] = 1;
        return updatedC;
    }

    private double[] getUpdatedObjective(double[] lastObj) {
        double[] newObjective = new double[initialModel.constraints()[0].length];
        System.arraycopy(lastObj, 0, newObjective, 0, initialModel.constraints()[0].length);
        return newObjective;
    }

    private double[] getUpdatedFreeVars(double[] lastFreeVars, double bound, Function<Double, Double> action) {//function instead of 'byte flat'
        double[] newFreeVars = new double[lastFreeVars.length + 1];
        System.arraycopy(lastFreeVars, 0, newFreeVars, 0, lastFreeVars.length);
        newFreeVars[lastFreeVars.length] = action.apply(bound);
        return newFreeVars;
    }

    private boolean[] getUpdatedSigns(boolean[] lastSigns, int length) {
        boolean[] newSigns = new boolean[length];
        System.arraycopy(lastSigns, 0, newSigns, 0, lastSigns.length);
        return newSigns;
    }

    private boolean hasFraction(double element) {
        final double epsilon = 1e-10;

        double currentElement = abs(element);
        double currentFraction = 0;
        if (abs(ceil(currentElement) - currentElement) > epsilon)
            currentFraction = currentElement % 1;

        return currentFraction > epsilon;
    }

    private record Model(double[][] constraints, boolean[] signs, double[] freeVars, double[] objective) {}
}