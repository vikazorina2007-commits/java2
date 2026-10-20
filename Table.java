import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class Table implements Serializable, Iterable<Row> {
    private String name;
    private List<Column> columns;
    private List<Row> rows;
    private String foreignKeyColumn;
    private String referencedTable;
    private String referencedColumn;
    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }
    public String getName() {
        return name;
    }
    public List<Column> getColumns() {
        return columns;
    }
    public List<Row> getRows() {
        return rows;
    }
    public void addRow(Row row) {
        rows.add(row);
    }
    public Column getColumn(String name) {
        for (Column column : columns) {
            if (column.getName().equalsIgnoreCase(name)) {
                return column;
            }
        }
        return null;
    }
    public boolean hasColumn(String name) {
        return getColumn(name) != null;
    }
    public Column getPrimaryKey() {
        for (Column column : columns) {
            if (column.isPrimaryKey()) {
                return column;
            }
        }
        return null;
    }
    public boolean hasPrimaryKeyValue(Object value) {
        Column primaryKey = getPrimaryKey();
        if (primaryKey == null) {
            return false;
        }
        for (Row row : rows) {
            Object current = row.getValue(primaryKey.getName());
            if (current != null && current.equals(value)) {
                return true;
            }
        }
        return false;
    }
    public void setForeignKey(
            String foreignKeyColumn,
            String referencedTable,
            String referencedColumn) {
        this.foreignKeyColumn = foreignKeyColumn;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }
    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }
    public String getReferencedTable() {
        return referencedTable;
    }
    public String getReferencedColumn() {
        return referencedColumn;
    }
    public Iterator<Row> iterator() {
        return rows.iterator();
    }
}