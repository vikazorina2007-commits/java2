import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class QueryExecutor {
    private Database database;
    private SqlParser parser;

    public QueryExecutor(Database database) {
        this.database = database;
        this.parser = new SqlParser();
    }
    public void execute(String sql) {
        try {
            Query query = parser.parse(sql);
            switch (query.getType()) {
                case INSERT: insert(query); break;
                case SELECT: select(query); break;
                case UPDATE: update(query); break;
                case DELETE: delete(query); break;
                case CREATE_TABLE: createTable(query); break;
                case DROP_TABLE: dropTable(query); break;
                case CREATE_INDEX: createIndex(query); break;
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
    private void createTable(Query query) throws IOException {
        List<Column> columns = new ArrayList<>();
        String foreignKeyColumn = null;
        String referencedTable = null;
        String referencedColumn = null;
        for (String definition : query.getColumns()) {
            String[] parts = definition.trim().split("\\s+");
            if (parts.length < 2) throw new IllegalArgumentException("Неверное описание столбца");
            String columnName = parts[0];
            if (definition.toUpperCase().startsWith("FOREIGN KEY")) continue;
            DataType type = parseDataType(parts[1]);
            boolean primaryKey = false;
            for (int i = 2; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("PRIMARY") && i + 1 < parts.length && parts[i + 1].equalsIgnoreCase("KEY")) primaryKey = true;
                if (parts[i].equalsIgnoreCase("REFERENCES") && i + 1 < parts.length) {
                    String reference = parts[i + 1];
                    int bracket = reference.indexOf('(');
                    if (bracket > 0 && reference.endsWith(")")) {
                        foreignKeyColumn = columnName;
                        referencedTable = reference.substring(0, bracket);
                        referencedColumn = reference.substring(bracket + 1, reference.length() - 1);
                    }
                }
            }
            if (hasColumn(columns, columnName)) throw new IllegalArgumentException("Столбец уже существует");
            columns.add(new Column(columnName, type, primaryKey));
        }
        Table table = new Table(query.getTableName(), columns);
        if (foreignKeyColumn != null) {
            Table referenced = database.getTable(referencedTable);
            if (referenced == null) throw new IllegalArgumentException("Связанная таблица не существует");
            if (!referenced.hasColumn(referencedColumn)) throw new IllegalArgumentException("Связанный столбец не существует");
            table.setForeignKey(foreignKeyColumn, referencedTable, referencedColumn);
        }
        database.createTable(table);
        System.out.println("Таблица " + query.getTableName() + " создана");
    }
    private void dropTable(Query query) throws IOException {
        database.dropTable(query.getTableName());
        System.out.println("Таблица " + query.getTableName() + " удалена");
    }
    private void insert(Query query) throws IOException {
        Table table = database.getTable(query.getTableName());
        if (table == null) throw new IllegalArgumentException("Таблица не существует");
        List<String> columns = query.getColumns();
        if (columns.isEmpty()) {
            for (Column column : table.getColumns()) columns.add(column.getName());
        }
        List<Row> newRows = new ArrayList<>();
        for (List<String> values : query.getValues()) {
            if (values.size() != columns.size()) throw new IllegalArgumentException("Количество значений не совпадает с количеством столбцов");
            Row row = new Row();
            for (int i = 0; i < columns.size(); i++) {
                Column column = table.getColumn(columns.get(i));
                if (column == null) throw new IllegalArgumentException("Столбец не существует: " + columns.get(i));
                Object value = parser.convertValue(values.get(i), column.getType());
                row.setValue(column.getName(), value);
            }
            for (Column column : table.getColumns()) {
                if (row.getValue(column.getName()) == null) row.setValue(column.getName(), null);
            }
            checkPrimaryKey(table, row, newRows);
            checkForeignKey(table, row);
            newRows.add(row);
        }
        for (Row row : newRows) table.addRow(row);
        saveTableAndIndexes(table);
        System.out.println("Добавлено записей: " + newRows.size());
    }
    private void select(Query query) {
        Table table = database.getTable(query.getTableName());
        if (table == null) throw new IllegalArgumentException("Таблица не существует");
        Condition condition = convertCondition(table, query.getCondition());
        List<Row> result = new ArrayList<>();
        if (condition != null && condition.getOperator().equals("=")) {
            TableIndex index = database.getIndex(table.getName(), condition.getColumn());
            if (index != null) {
                List<Integer> positions = index.find(condition.getValue());
                for (Integer position : positions) {
                    Row row = table.getRows().get(position);
                    if (condition.matches(row)) result.add(row);
                }
            } else {
                scanTable(table, condition, result);
            }
        } else {
            scanTable(table, condition, result);
        }
        printTable(table, query, result);
    }
    private void scanTable(Table table, Condition condition, List<Row> result) {
        for (Row row : table) {
            if (condition == null || condition.matches(row)) result.add(row);
        }
    }
    private void update(Query query) throws IOException {
        Table table = database.getTable(query.getTableName());
        if (table == null) throw new IllegalArgumentException("Таблица не существует");
        Column setColumn = table.getColumn(query.getSetColumn());
        if (setColumn == null) throw new IllegalArgumentException("Столбец не существует");
        Object newValue = parser.convertValue(query.getSetValue(), setColumn.getType());
        Condition condition = convertCondition(table, query.getCondition());
        int count = 0;
        for (Row row : table) {
            if (condition == null || condition.matches(row)) {
                row.setValue(setColumn.getName(), newValue);
                count++;
            }
        }
        saveTableAndIndexes(table);
        System.out.println("Изменено записей: " + count);
    }
    private void delete(Query query) throws IOException {
        Table table = database.getTable(query.getTableName());
        if (table == null) throw new IllegalArgumentException("Таблица не существует");
        Condition condition = convertCondition(table, query.getCondition());
        List<Row> toDelete = new ArrayList<>();
        for (Row row : table) {
            if (condition == null || condition.matches(row)) toDelete.add(row);
        }
        for (Row row : toDelete) {
            if (isReferencedByOtherTable(table, row)) throw new IllegalArgumentException("Запись используется другой таблицей");
        }
        table.getRows().removeAll(toDelete);
        saveTableAndIndexes(table);
        System.out.println("Удалено записей: " + toDelete.size());
    }
    private void createIndex(Query query) throws IOException {
        Table table = database.getTable(query.getTableName());
        if (table == null) throw new IllegalArgumentException("Таблица не существует");
        TableIndex index = new TableIndex(query.getIndexName(), query.getTableName(), query.getIndexColumn());
        database.addIndex(index);
        System.out.println("Индекс " + query.getIndexName() + " создан");
    }
    private Condition convertCondition(Table table, Condition condition) {
        if (condition == null) return null;
        Column column = table.getColumn(condition.getColumn());
        if (column == null) throw new IllegalArgumentException("Столбец условия не существует");
        Object value = parser.convertValue(String.valueOf(condition.getValue()), column.getType());
        return new Condition(column.getName(), condition.getOperator(), value);
    }
    private void checkPrimaryKey(Table table, Row row, List<Row> newRows) {
        Column primaryKey = table.getPrimaryKey();
        if (primaryKey == null) return;
        Object value = row.getValue(primaryKey.getName());
        if (value == null) throw new IllegalArgumentException("Первичный ключ не может быть NULL");
        if (table.hasPrimaryKeyValue(value)) throw new IllegalArgumentException("Такой первичный ключ уже существует");
        for (Row newRow : newRows) {
            Object newValue = newRow.getValue(primaryKey.getName());
            if (value.equals(newValue)) throw new IllegalArgumentException("Дублирующийся первичный ключ");
        }
    }
    private void checkForeignKey(Table table, Row row) {
        if (table.getForeignKeyColumn() == null) return;
        Object value = row.getValue(table.getForeignKeyColumn());
        if (value == null) return;
        Table referenced = database.getTable(table.getReferencedTable());
        if (referenced == null) throw new IllegalArgumentException("Связанная таблица не существует");
        boolean found = false;
        for (Row referencedRow : referenced) {
            Object referencedValue = referencedRow.getValue(table.getReferencedColumn());
            if (value.equals(referencedValue)) {
                found = true;
                break;
            }
        }
        if (!found) throw new IllegalArgumentException("Нарушение внешнего ключа");
    }
    private boolean isReferencedByOtherTable(Table table, Row row) {
        Column primaryKey = table.getPrimaryKey();
        if (primaryKey == null) return false;
        Object primaryValue = row.getValue(primaryKey.getName());
        for (Table otherTable : database.getTables().values()) {
            if (otherTable == table) continue;
            if (otherTable.getForeignKeyColumn() == null) continue;
            if (!otherTable.getReferencedTable().equalsIgnoreCase(table.getName())) continue;
            for (Row otherRow : otherTable) {
                Object value = otherRow.getValue(otherTable.getForeignKeyColumn());
                if (primaryValue != null && primaryValue.equals(value)) return true;
            }
        }
        return false;
    }
    private void saveTableAndIndexes(Table table) throws IOException {
        database.saveTable(table);
        for (TableIndex index : database.getIndexesForTable(table.getName())) {
            index.build(table);
            Storage.saveIndex(index);
        }
    }
    private DataType parseDataType(String value) {
        switch (value.toUpperCase()) {
            case "INT":
            case "INTEGER": return DataType.INT;
            case "DOUBLE":
            case "FLOAT": return DataType.DOUBLE;
            case "STRING":
            case "VARCHAR": return DataType.STRING;
            case "DATE": return DataType.DATE;
            case "BOOLEAN":
            case "BOOL": return DataType.BOOLEAN;
            default: throw new IllegalArgumentException("Неизвестный тип: " + value);
        }
    }
    private boolean hasColumn(List<Column> columns, String name) {
        for (Column column : columns) {
            if (column.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }
    private void printTable(Table table, Query query, List<Row> rows) {
        List<String> columns = query.getColumns();
        if (columns.isEmpty()) {
            for (Column column : table.getColumns()) columns.add(column.getName());
        }
        for (String column : columns) {
            if (!table.hasColumn(column)) throw new IllegalArgumentException("Столбец не существует: " + column);
        }
        for (String column : columns) System.out.print(String.format("%-20s", column));
        System.out.println();
        for (int i = 0; i < columns.size(); i++) System.out.print("--------------------");
        System.out.println();
        for (Row row : rows) {
            for (String column : columns) {
                Object value = row.getValue(column);
                System.out.print(String.format("%-20s", String.valueOf(value)));
            }
            System.out.println();
        }
        System.out.println("Найдено записей: " + rows.size());
    }
}
