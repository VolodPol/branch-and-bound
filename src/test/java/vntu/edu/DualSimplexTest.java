package vntu.edu;

import org.junit.jupiter.api.Test;
import vntu.edu.simplex_methods.BaseSimplex;
import vntu.edu.simplex_methods.DualSimplex;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DualSimplexTest {
    @Test
    public void testMyCase25() {
        double[][] constraints = new double[][] {
                {2, 1},
                {1, 2},
                {1, -2}
        };
        double[] freeVars = {18, 14, 10};
        boolean[] signs = {false, true, false};
        double[] objective = {1, 2};

        BaseSimplex simplex = new DualSimplex(constraints, signs, freeVars, objective);
        assertArrayEquals(new double[]{0.0, 7.0, 11.0, 0.0, 24.0}, simplex.solve(false).optimalPlan());
    }

    @Test
    public void testTeacherCase() {
        double[][] constraints = new double[][] {
                {2, 1, -2},
                {1, 2, 4},
                {1, -1, 2}
        };
        double[] freeVars = {18, 22, 10};
        boolean[] signs = {false, false, true};
        double[] objective = {-2, 3, 6};
        BaseSimplex simplex = new DualSimplex(constraints, signs, freeVars, objective);
        assertArrayEquals(new double[]{0.0, 0.0, 5.0, 28.0, 2.0, 0.0}, simplex.solve(false).optimalPlan());
    }

    @Test
    public void testCaseFromLecture() {
        double[][] constraints = new double[][] {
                {1.5, 3, -1, 1},
                {3, 2, 0, -1}
        };
        double[] freeVars = {18, 24};
        boolean[] signs = {true, true};
        double[] objective = {5, 6, 1, 1};
        BaseSimplex simplex = new DualSimplex(constraints, signs, freeVars, objective);
        assertArrayEquals(new double[]{6.0, 3.0, 0.0, 0.0, 0.0, 0.0}, simplex.solve(false).optimalPlan());
    }

    @Test
    public void testCase28() {
        double[][] constraints = new double[][] {
                {1, 1},
                {5, 1},
                {1, 5}
        };
        double[] freeVars = {3, 5, 5};
        boolean[] signs = {true, true, true};
        double[] objective = {7, 1};

        BaseSimplex simplex = new DualSimplex(constraints, signs, freeVars, objective);
        assertArrayEquals(new double[]{0.0, 5.0, 2.0, 0.0, 20.0}, simplex.solve(false).optimalPlan());
    }
}