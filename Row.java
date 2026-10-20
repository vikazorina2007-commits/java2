import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
public class Row implements Serializable {
    private Map<String, Object> values = new LinkedHashMap<>();
    public void setValue(String column, Object value) {
        values.put(column, value);
    }
    public Object getValue(String column) {
        return values.get(column);
    }
    public Map<String, Object> getValues() {
        return values;
    }
}