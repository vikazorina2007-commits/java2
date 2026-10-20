import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TableIndex implements Serializable {
    private String name;
    private String tableName;
    private String columnName;
    private Map<String, List<Integer>> values;
    public TableIndex(
            String name,
            String tableName,
            String columnName) {
        this.name = name;
        this.tableName = tableName;
        this.columnName = columnName;
        this.values = new HashMap<>();
    }
    public void build(Table table) {
        values.clear();
        for (int i = 0; i < table.getRows().size(); i++) {
            Row row = table.getRows().get(i);
            Object value = row.getValue(columnName);
            if (value != null) {
                String key = String.valueOf(value);
                if (!values.containsKey(key)) {
                    values.put(key, new ArrayList<Integer>());
                }
                values.get(key).add(i);
            }
        }
    }
    public List<Integer> find(Object value) {
        List<Integer> result =
                values.get(String.valueOf(value));
        if (result == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(result);
    }
    public String getName() {
        return name;
    }
    public String getTableName() {
        return tableName;
    }
    public String getColumnName() {
        return columnName;
    }
}