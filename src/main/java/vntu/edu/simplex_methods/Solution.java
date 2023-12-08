package vntu.edu.simplex_methods;

import java.util.Arrays;

public record Solution(double objValue, double[] optimalPlan) {

    @Override
    public String toString() {
        String str = "F = %f %nX = %s %n";
        return String.format(str, objValue, Arrays.toString(optimalPlan));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Solution solution = (Solution) object;

        if (Double.compare(objValue, solution.objValue) != 0) return false;
        return Arrays.equals(optimalPlan, solution.optimalPlan);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(objValue);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(optimalPlan);
        return result;
    }

}
