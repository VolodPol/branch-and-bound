package vntu.edu;

import org.junit.jupiter.api.Test;
import vntu.edu.simplex_methods.Simplex;

import java.util.Arrays;

public class SimplexTest {
    @Test
    public void testCase1(){
        double[][] matrix = new double[][] {
                {7, 3},
                {4, 1}
        };
        double[] freeVars = {21, 8};
        boolean[] signs = {false, false};
        double[] objective = {8, 5};

        Simplex simplex = new Simplex(matrix, signs, freeVars, objective);//# 15
        System.out.println("Should print 35, printed: ");
        System.out.println(Arrays.toString(simplex.solve(true)));
    }

    @Test
    public void testCase2(){
        double[][] matrix = new double[][]{
                {-2, -3},
                {1, 1},
                {3, 1}
        };
        double[] freeVars = {15, 9, 24};
        boolean[] signs = {false, false, false};
        double[] objective = {2, 5};
        Simplex simplex = new Simplex(matrix, signs, freeVars, objective);// # 5

        System.out.println("Should print 45, printed: ");
        System.out.println(Arrays.toString(simplex.solve(true)));
    }

    @Test
    public void testCase3(){
        double[][] matrix = new double[][] {
                {2, 5},
                {-3, 4},
                {2, 4}
        };
        double[] freeVars = {18, 10, 8};
        boolean[] signs = {false, false, false};
        double[] objective = {1, -1};
        Simplex simplex = new Simplex(matrix, signs, freeVars, objective);//# 15

        System.out.println("Should print 4, printed: ");
        System.out.println(Arrays.toString(simplex.solve(true)));
    }

    @Test
    public void testCase4(){
        double[][] matrix = new double[][] {
                {3, 1},
                {-1, -5},
                {2, 4}
        };
        double[] freeVars = {9, 6, 8};
        boolean[] signs = {false, false, false};
        double[] objective = {5, -1};
        Simplex simplex = new Simplex(matrix, signs, freeVars, objective);

        System.out.println("Should print 15, printed: ");
        System.out.println(Arrays.toString(simplex.solve(true)));
    }

    @Test
    public void testCase5(){
        double[][] matrix = new double[][] {
                {5, 3},
                {3, 5},
                {0, 1}
        };
        double[] freeVars = {15, 15, 8};
        boolean[] signs = {false, false, false};
        double[] objective = {3, 1};
        Simplex simplex = new Simplex(matrix, signs, freeVars, objective);

        System.out.println("Should print 9, printed: ");
        System.out.println(Arrays.toString(simplex.solve(true)));
    }
}