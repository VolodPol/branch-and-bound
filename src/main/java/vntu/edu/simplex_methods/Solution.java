package vntu.edu.simplex_methods;

import java.util.Arrays;

public record Solution(double objValue, double[] optimalPlan) {
    @Override
    public String toString() {
        String str = "F = %f %nX = %s %n";
        return String.format(str, objValue, Arrays.toString(optimalPlan));
    }
}
