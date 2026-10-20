import java.io.Serializable;
public class Condition implements Serializable {
    private String column;
    private String operator;
    private Object value;
    public Condition(String column, String operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }
    public boolean matches(Row row) {
        Object left = row.getValue(column);
        if (left == null) {
            return operator.equals("=") && value == null;
        }
        int result;
        if (left instanceof Number && value instanceof Number) {
            double a = ((Number) left).doubleValue();
            double b = ((Number) value).doubleValue();
            result = Double.compare(a, b);
        } else {
            result = String.valueOf(left).compareToIgnoreCase(String.valueOf(value));
        }
        switch (operator) {
            case "=":
                return result == 0;
            case "!=":
                return result != 0;
            case ">":
                return result > 0;
            case "<":
                return result < 0;
            case ">=":
                return result >= 0;
            case "<=":
                return result <= 0;
            default:
                return false;
        }
    }
    public String getColumn() {
        return column;
    }
    public String getOperator() {
        return operator;
    }
    public Object getValue() {
        return value;
    }
}