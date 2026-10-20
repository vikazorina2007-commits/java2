import java.util.ArrayList;
import java.util.List;
public class PerformanceTest {
    public static void main(String[] args) {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Row row = new Row();
            row.setValue("id", i);
            row.setValue("brand", "Brand" + (i % 100));
            rows.add(row);
        }
        Table table = new Table("cars",
                List.of(new Column(
                "id", DataType.INT, true),
                                new Column("brand", DataType.STRING, false)));
        for (Row row : rows) {
            table.addRow(row);
        }
        TableIndex index =
                new TableIndex("idx_cars_brand", "cars", "brand");
        index.build(table);
        long startWithoutIndex = System.nanoTime();
        int countWithoutIndex = 0;
        for (Row row : rows) {
            if ("Brand50".equals(row.getValue("brand"))) {
                countWithoutIndex++;
            }
        }
        long endWithoutIndex = System.nanoTime();
        long startWithIndex = System.nanoTime();
        List<Integer> result = index.find("Brand50");
        long endWithIndex = System.nanoTime();
        System.out.println("Количество найденных записей без индекса: " + countWithoutIndex);
        System.out.println("Время без индекса: " + (endWithoutIndex - startWithoutIndex) + " нс");
        System.out.println("Количество найденных записей с индексом: " + result.size());
        System.out.println("Время с индексом: " + (endWithIndex - startWithIndex) + " нс");
    }
}