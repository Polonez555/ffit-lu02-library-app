package ch.bzz;

import java.sql.*;
import java.util.*;

public class LibraryAppMain {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        Map<String, Runnable> commands = new HashMap<>();

        Connection c = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/local_db", "postgres", "182007"
        );


        commands.put("help", () -> {
            System.out.println("Verfügbare Befehle:");
            commands.keySet().forEach(System.out::println);
        });

        commands.put("list", () -> {
            try {
                Statement statement = c.createStatement();
                ResultSet rs = null;

                rs = statement.executeQuery("SELECT id, isbn, title, author, publication_year FROM books");


                while (rs.next()) {
                    int id = rs.getInt("id");
                    String isbn = rs.getString("isbn");
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    int year = rs.getInt("publication_year");

                    System.out.printf("%d | %s | %s | %s | %d%n", id, isbn, title, author, year);
                }
                rs.close();
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        commands.put("quit", () -> {
            System.out.println("Programm wird beendet...");
            System.exit(0);
        });

        System.out.println("Willkommen zur LibraryApp! Geben Sie einen Befehl ein:");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            Runnable command = commands.get(input);
            if (command != null) {
                command.run();
            } else {
                System.out.println("Unbekannter Befehl: " + input);
            }
        }
    }
}
