import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
public class Storage {
    private static final String TABLE_DIRECTORY = "data/tables";
    private static final String INDEX_DIRECTORY = "data/indexes";
    public static void saveTable(Table table) throws IOException {
        createDirectories();
        File file = new File(TABLE_DIRECTORY + "/" + table.getName() + ".db");
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(table);
        }
    }
    public static Table loadTable(String tableName)
            throws IOException, ClassNotFoundException {
        File file = new File(TABLE_DIRECTORY + "/" + tableName + ".db");
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            return (Table) input.readObject();
        }
    }
    public static void saveIndex(TableIndex index)
            throws IOException {
        createDirectories();
        File file = new File(INDEX_DIRECTORY + "/" + index.getName() + ".idx");
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(index);
        }
    }
    public static TableIndex loadIndex(String indexName)
            throws IOException, ClassNotFoundException {
        File file = new File(INDEX_DIRECTORY + "/" + indexName + ".idx");
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            return (TableIndex) input.readObject();
        }
    }
    public static void deleteTable(String tableName)
            throws IOException {
        File file = new File(TABLE_DIRECTORY + "/" + tableName + ".db");
        if (file.exists() && !file.delete()) {
            throw new IOException("Не удалось удалить таблицу");
        }
    }
    public static void deleteIndex(String indexName)
            throws IOException {
        File file = new File(INDEX_DIRECTORY + "/" + indexName + ".idx");
        if (file.exists() && !file.delete()) {
            throw new IOException("Не удалось удалить индекс");
        }
    }
    private static void createDirectories() {
        new File(TABLE_DIRECTORY).mkdirs();
        new File(INDEX_DIRECTORY).mkdirs();
    }
}