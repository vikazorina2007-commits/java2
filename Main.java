import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Database database = new Database();
        QueryExecutor executor = new QueryExecutor(database);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Упрощенная СУБД");
        System.out.println("Для выхода введите EXIT");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("EXIT")) {
                break;
            }
            if (input.isEmpty()) {
                continue;
            }
            executor.execute(input);
        }
        scanner.close();
    }
}