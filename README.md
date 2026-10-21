# Лабораторная работа 2
**Цель:** разработать систему управления базами данных на Java
В программе можно создавать таблицы, добавлять, изменять, удалять записи и выполнять запросы на выборку. Данные сохраняются в файлы. Используются 2 таблицы: `owners` и `cars`
## Реализовано:
1. Две связанные таблицы
2. Первичный ключ и проверка его уникальности
3. Внешний ключ
4. Типы `INT`, `DOUBLE`, `STRING`, `DATE`, `BOOLEAN`
5. `INSERT`
6. `UPDATE`
7. `DELETE`
8. `SELECT`
9. Условие `WHERE`
10. Добавление нескольких записей одной командой.
11. `CREATE TABLE`
12. `DROP TABLE`
13. `CREATE INDEX`
14. Сохранение данных на диске
15. Загрузка данных при запуске
16. Сохранение и загрузка индексов
17. Консольный интерфейс
18. Сравнение поиска с индексом и без него
## Используются классы:
`Main`, `Database`, `Table`, `Row`, `Column`, `DataType`, `Condition`, `Query`, `SqlParser`, `QueryExecutor`, `Storage`, `TableIndex`, `PerformanceTest`
## Последовательность работы:
* **User -> Main:** Вводит `SELECT`
* **Main -> Executor:** Передаёт команду 
* **Executor -> Parser:** Разбирает запрос 
* **Parser -> Executor:** Возвращает запрос
* **Query Executor -> DB:** Получает таблицу `cars`
* **DB --> Executor:** Возвращает таблицу 
* **Executor -> Index:** Ищет марку через индекс 
* **Index -> Executor:** Возвращает найденные записи 
* **Executor -> Main:** Передаёт результат 
* **Main -> User:** Выводит таблицу
## Инструкция пользователя
### Запуск программы:
1. Открыть проект в IntelliJ IDEA.
2. Открыть класс `Main`.
3. Нажать кнопку запуска программы.
4. После появления приглашения вводить SQL-команды.
### Примеры команд:
Создание таблицы владельцев:
CREATE TABLE owners (id INT PRIMARY KEY, name STRING, birthDate DATE, active BOOLEAN);

Создание таблицы автомобилей:
CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE, owner_id INT REFERENCES owners(id));

Добавление записи:
INSERT INTO owners VALUES (1, 'Ivan', '2005-05-10', TRUE);

Добавление автомобиля:
INSERT INTO cars VALUES (1, 'Toyota', 25000.0, 1);

Вывод всех автомобилей:
SELECT * FROM cars;

## Распределение обязанностей
### Виктория Зорина
* Реализация логики выполнения запросов и работы с условиями (`QueryExecutor.java`, `Condition.java`)
* Разработка модуля индексации и ускорения поиска (`TableIndex.java`)
* Написание тестов производительности (`PerformanceTest.java`)
* Поддержка связности строк и таблиц (`Row.java`)
### Юля Залетова 
* Разработка классов структуры данных: `Column.java`, `DataType.java`, `Table.java`, `Database.java`
* Реализация логики хранения данных на диске (`Storage.java`)
* Написание parser SQL-команд (`SqlParser.java`, `Query.java`)
* Реализация интерфейса и запуска приложения (`Main.java`)
