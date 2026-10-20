import java.io.Serializable;
public class Column implements Serializable {
    private String name;
    private DataType type;
    private boolean primaryKey;
    public Column(String name, DataType type, boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
    }
    public String getName() {
        return name;
    }
    public DataType getType() {
        return type;
    }
    public boolean isPrimaryKey() {
        return primaryKey;
    }
}