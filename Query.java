import java.util.ArrayList;
import java.util.List;
public class Query {
    public enum Type {
        INSERT,
        SELECT,
        UPDATE,
        DELETE,
        CREATE_TABLE,
        DROP_TABLE,
        CREATE_INDEX
    }
    private Type type;
    private String tableName;
    private List<String> columns;
    private List<List<String>> values;
    private String setColumn;
    private String setValue;
    private Condition condition;
    private String indexName;
    private String indexColumn;
    public Query(Type type) {
        this.type = type;
        this.columns = new ArrayList<>();
        this.values = new ArrayList<>();
    }
    public Type getType() {
        return type;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    public List<List<String>> getValues() {
        return values;
    }
    public void setValues(List<List<String>> values) {
        this.values = values;
    }
    public String getSetColumn() {
        return setColumn;
    }
    public void setSetColumn(String setColumn) {
        this.setColumn = setColumn;
    }
    public String getSetValue() {
        return setValue;
    }
    public void setSetValue(String setValue) {
        this.setValue = setValue;
    }
    public Condition getCondition() {
        return condition;
    }
    public void setCondition(Condition condition) {
        this.condition = condition;
    }
    public String getIndexName() {
        return indexName;
    }
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    public String getIndexColumn() {
        return indexColumn;
    }
    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }
}
