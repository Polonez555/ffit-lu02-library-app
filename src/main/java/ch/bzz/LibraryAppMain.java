package ch.bzz;

import ch.bzz.model.Book;
import java.sql.*;
import java.util.*;
import java.util.Arrays;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryAppMain {
    private static final Logger log = LoggerFactory.getLogger(LibraryAppMain.class);
    // Book constants as specified in the requirements
    private static final Book BOOK_1 = new Book(1, "978-3-8362-9544-4", "Java ist auch eine Insel", "Christian Ullenboom", 2023);
    private static final Book BOOK_2 = new Book(2, "978-3-658-43573-8", "Grundkurs Java", "Dietmar Abts", 2024);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandHandler commandHandler = new CommandHandler();
        
        log.info("Willkommen zur Bibliotheks-App!");
        log.info("Geben Sie 'help' ein, um alle verfügbaren Befehle zu sehen.");
        
        // Add shutdown hook to properly close EntityManagerFactory
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Database.close();
            log.info("Anwendung beendet.");
        }));
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            
            switch (command) {
                case "help":
                    commandHandler.executeCommand("help");
                    break;
                case "listbooks":
                    if (parts.length > 1) {
                        commandHandler.listBooks(parts[1]);
                    } else {
                        commandHandler.executeCommand("listBooks");
                    }
                    break;
                case "list":
                    commandHandler.executeCommand("list");
                    break;
                case "importbooks":
                    if (parts.length < 2) {
                        log.warn("Bitte geben Sie den Dateipfad an: importBooks <FILE_PATH>");
                    } else {
                        commandHandler.importBooks(parts[1]);
                    }
                    break;
                case "createuser":
                    if (parts.length < 2) {
                        log.warn("Bitte geben Sie alle Benutzerinformationen an: createUser <firstname> <lastname> <dateOfBirth> <email> <password>");
                    } else {
                        commandHandler.createUser(parts[1]);
                    }
                    break;
                case "quit":
                    commandHandler.executeCommand("quit");
                    scanner.close();
                    Database.close();
                    return;
                default:
                    log.warn("Unbekannter Befehl: " + command);
                    log.info("Geben Sie 'help' ein, um alle verfügbaren Befehle zu sehen.");
            }
        }
    }
}
