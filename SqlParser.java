import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class SqlParser {
    public Query parse(String sql) {
        sql = sql.trim();
        if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1).trim();
        String upper = sql.toUpperCase();
        if (upper.startsWith("INSERT INTO")) return parseInsert(sql);
        if (upper.startsWith("SELECT")) return parseSelect(sql);
        if (upper.startsWith("UPDATE")) return parseUpdate(sql);
        if (upper.startsWith("DELETE FROM")) return parseDelete(sql);
        if (upper.startsWith("CREATE TABLE")) return parseCreateTable(sql);
        if (upper.startsWith("DROP TABLE")) return parseDropTable(sql);
        if (upper.startsWith("CREATE INDEX")) return parseCreateIndex(sql);
        throw new IllegalArgumentException("Неизвестная команда");
    }
    private Query parseInsert(String sql) {
        Query query = new Query(Query.Type.INSERT);
        Pattern pattern = Pattern.compile("INSERT\\s+INTO\\s+(\\w+)\\s*(?:\\((.*?)\\))?\\s+VALUES\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) throw new IllegalArgumentException("Неверный INSERT");
        query.setTableName(matcher.group(1));
        if (matcher.group(2) != null) query.setColumns(splitComma(matcher.group(2)));
        String valuesText = matcher.group(3).trim();
        List<String> groups = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quotes = false;
        int level = 0;
        for (char c : valuesText.toCharArray()) {
            if (c == '\'') quotes = !quotes;
            if (c == '(' && !quotes) level++;
            if (c == ')' && !quotes) level--;
            current.append(c);
            if (level == 0 && c == ')' && !quotes) { groups.add(current.toString()); current.setLength(0); }
        }
        for (String group : groups) {
            String text = group.trim();
            if (text.startsWith("(")) text = text.substring(1);
            if (text.endsWith(")")) text = text.substring(0, text.length() - 1);
            query.getValues().add(splitComma(text));
        }
        return query;
    }
    private Query parseSelect(String sql) {
        Query query = new Query(Query.Type.SELECT);
        Pattern pattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) throw new IllegalArgumentException("Неверный SELECT");
        String columns = matcher.group(1).trim();
        if (!columns.equals("*")) query.setColumns(splitComma(columns));
        query.setTableName(matcher.group(2));
        if (matcher.group(3) != null) query.setCondition(parseCondition(matcher.group(3)));
        return query;
    }
    private Query parseUpdate(String sql) {
        Query query = new Query(Query.Type.UPDATE);
        Pattern pattern = Pattern.compile("UPDATE\\s+(\\w+)\\s+" + "SET\\s+(\\w+)\\s*=\\s*(.+?)" + "(?:\\s+WHERE\\s+(.+))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) throw new IllegalArgumentException("Неверный UPDATE");
        query.setTableName(matcher.group(1));
        query.setSetColumn(matcher.group(2));
        query.setSetValue(matcher.group(3).trim());
        if (matcher.group(4) != null) query.setCondition(parseCondition(matcher.group(4)));
        return query;
    }
    private Query parseDelete(String sql) {
        Query query = new Query(Query.Type.DELETE);
        Pattern pattern = Pattern.compile("DELETE\\s+FROM\\s+(\\w+)" + "(?:\\s+WHERE\\s+(.+))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) throw new IllegalArgumentException("Неверный DELETE");
        query.setTableName(matcher.group(1));
        if (matcher.group(2) != null) query.setCondition(parseCondition(matcher.group(2)));
        return query;
    }
    private Query parseCreateTable(String sql) {
        Query query = new Query(Query.Type.CREATE_TABLE);
        Pattern pattern = Pattern.compile("CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) throw new IllegalArgumentException("Неверный CREATE TABLE");
        query.setTableName(matcher.group(1));
        query.setColumns(splitComma(matcher.group(2)));
        return query;
    }
    private Query parseDropTable(String sql) {
        Query query = new Query(Query.Type.DROP_TABLE);
        Pattern pattern = Pattern.compile("DROP\\s+TABLE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) throw new IllegalArgumentException("Неверный DROP TABLE");
        query.setTableName(matcher.group(1));
        return query;
    }
    private Query parseCreateIndex(String sql) {
        Query query = new Query(Query.Type.CREATE_INDEX);
        Pattern pattern = Pattern.compile("CREATE\\s+INDEX\\s+(\\w+)" + "\\s+ON\\s+(\\w+)\\s*\\((\\w+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) throw new IllegalArgumentException("Неверный CREATE INDEX");
        query.setIndexName(matcher.group(1));
        query.setTableName(matcher.group(2));
        query.setIndexColumn(matcher.group(3));
        return query;
    }
    private Condition parseCondition(String text) {
        Pattern pattern = Pattern.compile("(\\w+)\\s*(>=|<=|!=|=|>|<)\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text.trim());
        if (!matcher.matches()) throw new IllegalArgumentException("Неверное условие WHERE");
        return new Condition(
                matcher.group(1),
                matcher.group(2),
                removeQuotes(matcher.group(3).trim())
        );
    }
    private String removeQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
    private List<String> splitComma(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quotes = false;
        for (char c : text.toCharArray()) {
    if (c == '\'') quotes = !quotes;
    if (c == ',' && !quotes) { result.add(current.toString().trim()); current.setLength(0); } 
    else 
        current.append(c);
}
        if (current.length() > 0) result.add(current.toString().trim());
        return result;
    }
    public Object convertValue(String value, DataType type) value = removeQuotes(value.trim());
switch (type) {
    case INT: return Integer.parseInt(value);
    case DOUBLE: return Double.parseDouble(value);
    case STRING: return value;
    case DATE: return java.time.LocalDate.parse(value);
    case BOOLEAN: return Boolean.parseBoolean(value);
    default: throw new IllegalArgumentException("Неизвестный тип данных");
}
}
