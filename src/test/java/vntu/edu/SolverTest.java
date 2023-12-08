package vntu.edu;

import org.junit.jupiter.api.Test;
import vntu.edu.simplex_methods.Solution;

import static org.junit.jupiter.api.Assertions.*;

class SolverTest {
    private Solver solver;
    @Test
    public void testCase8() {//mine
        double[][] constraints = {
                {1, 1},
                {1, 3}
        };
        boolean[] signs = {false, false};
        double[] freeVars = {7, 12};
        double[] objective = {2, 5};
        boolean max = true;

        solver = new Solver(
                constraints,
                signs,
                freeVars,
                objective
        );
        Solution actual = solver.solve(max);
        Solution expected = new Solution(
                21, new double[]{3, 3, 1, 0, 0}
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testCase3() {
        double[][] constraints = {
                {-2, 5},
                {9, -4}
        };
        boolean[] signs = {false, false};
        double[] freeVars = {9, 24};
        double[] objective = {15, -5};
        boolean max = true;

        solver = new Solver(
                constraints,
                signs,
                freeVars,
                objective
        );
        Solution actual = solver.solve(max);
        Solution expected = new Solution(
                45, new double[]{4, 3, 2, 0, 0}
        );
        assertEquals(expected, actual);
    }
}