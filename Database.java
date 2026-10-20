import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Database {
    private Map<String, Table> tables;
    private Map<String, TableIndex> indexes;
    public Database() {
        tables = new HashMap<>();
        indexes = new HashMap<>();
        loadTables();
        loadIndexes();
    }
    public void createTable(Table table) throws IOException {
        String key = table.getName().toLowerCase();
        if (tables.containsKey(key)) {
            throw new IllegalArgumentException("Таблица уже существует");
        }
        tables.put(key, table);
        Storage.saveTable(table);
    }
    public void dropTable(String tableName) throws IOException {
        String key = tableName.toLowerCase();
        if (!tables.containsKey(key)) {
            throw new IllegalArgumentException("Таблица не существует");
        }
        List<String> indexesToDelete = new ArrayList<>();
        for (TableIndex index : indexes.values()) {
            if (index.getTableName().equalsIgnoreCase(tableName)) {
                indexesToDelete.add(index.getName());
            }
        }
        for (String indexName : indexesToDelete) {
            indexes.remove(indexName.toLowerCase());
            Storage.deleteIndex(indexName);
        }
        tables.remove(key);
        Storage.deleteTable(tableName);
    }
    public Table getTable(String name) {
        return tables.get(name.toLowerCase());
    }
    public Map<String, Table> getTables() {
        return tables;
    }
    public void saveTable(Table table) throws IOException {
        Storage.saveTable(table);
    }
    public void addIndex(TableIndex index) throws IOException {
        String key = index.getName().toLowerCase();
        if (indexes.containsKey(key)) {
            throw new IllegalArgumentException("Индекс уже существует");
        }
        Table table = getTable(index.getTableName());
        if (table == null) {
            throw new IllegalArgumentException("Таблица не существует");
        }
        if (!table.hasColumn(index.getColumnName())) {
            throw new IllegalArgumentException("Столбец не существует");
        }
        index.build(table);
        indexes.put(key, index);
        Storage.saveIndex(index);
    }
    public TableIndex getIndex(String tableName, String columnName) {
        for (TableIndex index : indexes.values()) {
            if (index.getTableName().equalsIgnoreCase(tableName)
                    && index.getColumnName().equalsIgnoreCase(columnName)) {
                return index;
            }
        }
        return null;
    }
    public List<TableIndex> getIndexesForTable(String tableName) {
        List<TableIndex> result = new ArrayList<>();
        for (TableIndex index : indexes.values()) {
            if (index.getTableName().equalsIgnoreCase(tableName)) {
                result.add(index);
            }
        }
        return result;
    }
    private void loadTables() {
        File directory = new File("data/tables");
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.getName().endsWith(".db")) {
                continue;
            }
            String name = file.getName()
                    .substring(0, file.getName().length() - 3);
            try {
                Table table = Storage.loadTable(name);
                tables.put(name.toLowerCase(), table);
            } catch (Exception e) {
                System.out.println("Не удалось загрузить таблицу: " + name);
            }
        }
    }
    private void loadIndexes() {
        File directory = new File("data/indexes");
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;}
        for (File file : files) {
            if (!file.getName().endsWith(".idx")) {
                continue;
            }
            String name = file.getName()
                    .substring(0, file.getName().length() - 4);
            try {
                TableIndex index = Storage.loadIndex(name);
                Table table = getTable(index.getTableName());
                if (table != null) {
                    index.build(table);
                    indexes.put(name.toLowerCase(), index);
                }
            } catch (Exception e) {
                System.out.println("Не удалось загрузить индекс: " + name);
            }
        }
    }
}