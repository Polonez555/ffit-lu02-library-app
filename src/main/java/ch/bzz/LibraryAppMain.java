package ch.bzz;

import java.util.*;

public class LibraryAppMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, Runnable> commands = new HashMap<>();

        commands.put("help", () -> {
            System.out.println("Verfügbare Befehle:");
            commands.keySet().forEach(System.out::println);
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
