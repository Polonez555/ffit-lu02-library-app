package ch.bzz;

import ch.bzz.model.Book;
import ch.bzz.model.User;
import ch.bzz.util.PasswordHandler;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommandHandler class handles all command operations
 * Following the SRP principle by separating command handling concerns
 */
public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    private final Map<String, Runnable> commands;

    public CommandHandler() {
        this.commands = new HashMap<>();
        initializeCommands();
    }

    /**
     * Initializes all available commands
     */
    private void initializeCommands() {
        commands.put("help", this::showHelp);
        commands.put("listBooks", this::listBooks);
        commands.put("list", this::listBooksDetailed);
        commands.put("quit", this::quit);
    }

    /**
     * Shows help information
     */
    private void showHelp() {
        log.info("Verfügbare Befehle:");
        log.info("  help - Zeigt diese Hilfe an");
        log.info("  listBooks [limit] - Zeigt alle Bücher aus der Datenbank an (optional mit Limit)");
        log.info("  list - Zeigt alle Bücher mit Details an");
        log.info("  importBooks <FILE_PATH> - Importiert Bücher aus einer TSV-Datei");
        log.info("  createUser <firstname> <lastname> <dateOfBirth> <email> <password> - Erstellt einen neuen Benutzer");
        log.info("  quit - Beendet das Programm");
    }

    /**
     * Lists all book titles from the database
     */
    private void listBooks() {
        listBooks(null);
    }

    /**
     * Lists book titles from the database with optional limit
     * @param limitStr optional limit as string, null for no limit
     */
    public void listBooks(String limitStr) {
        List<Book> books;
        if (limitStr != null && !limitStr.trim().isEmpty()) {
            try {
                int limit = Integer.parseInt(limitStr.trim());
                if (limit <= 0) {
                    log.warn("Limit muss eine positive Zahl sein. Zeige alle Bücher an.");
                    books = Database.getAllBooks();
                } else {
                    books = Database.getAllBooks(limit);
                }
            } catch (NumberFormatException e) {
                log.warn("Ungültiger Limit-Wert: '" + limitStr + "'. Zeige alle Bücher an.", e);
                books = Database.getAllBooks();
            }
        } else {
            books = Database.getAllBooks();
        }
        
        if (books.isEmpty()) {
            log.info("Keine Bücher in der Datenbank gefunden.");
            return;
        }

        for (Book book : books) {
            log.info(book.getTitle());
        }
    }

    /**
     * Lists all books with detailed information
     */
    private void listBooksDetailed() {
        List<Book> books = Database.getAllBooks();
        if (books.isEmpty()) {
            log.info("Keine Bücher in der Datenbank gefunden.");
        } else {
            for (Book book : books) {
                log.info(String.format("%d | %s | %s | %s | %d", 
                    book.getId(), book.getIsbn(), book.getTitle(), 
                    book.getAuthor(), book.getPublicationYear()));
            }
        }
    }

    /**
     * Handles the importBooks command
     * @param filePath path to the TSV file
     */
    public void importBooks(String filePath) {
        if (filePath.isEmpty()) {
            log.warn("Bitte geben Sie einen Dateipfad an: importBooks <FILE_PATH>");
        } else {
            List<Book> books = FileHandler.readBooksFromTSV(filePath);
            if (!books.isEmpty()) {
                Database.saveBooks(books);
            } else {
                log.warn("Keine Bücher aus der Datei gelesen. Überprüfen Sie den Dateipfad und das Dateiformat.");
            }
        }
    }

    /**
     * Creates a new user with the provided information
     * @param userInfo space-separated string containing firstname, lastname, dateOfBirth, email, password
     */
    public void createUser(String userInfo) {
        if (userInfo == null || userInfo.trim().isEmpty()) {
            log.warn("Bitte geben Sie alle Benutzerinformationen an: createUser <firstname> <lastname> <dateOfBirth> <email> <password>");
            return;
        }

        String[] parts = userInfo.trim().split("\\s+");
        if (parts.length != 5) {
            log.warn("Ungültige Anzahl Parameter. Erwartet: createUser <firstname> <lastname> <dateOfBirth> <email> <password>");
            log.info("Beispiel: createUser Max Mustermann 1990-05-21 max.mustermann@example.com geheim123");
            return;
        }

        String firstname = parts[0];
        String lastname = parts[1];
        String dateOfBirthStr = parts[2];
        String email = parts[3];
        String password = parts[4];

        try {
            // Parse date of birth
            LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr, DateTimeFormatter.ISO_LOCAL_DATE);

            // Generate salt and hash password
            byte[] salt = PasswordHandler.generateSalt();
            byte[] hash = PasswordHandler.hashPassword(password, salt);

            // Encode salt and hash to Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            // Create user object
            User user = new User();
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setDateOfBirth(dateOfBirth);
            user.setEmail(email);
            user.setPasswordSalt(saltBase64);
            user.setPasswordHash(hashBase64);

            // Save user to database
            Database.saveUser(user);
            log.info("Benutzer erfolgreich erstellt: " + firstname + " " + lastname + " (" + email + ")");

        } catch (DateTimeParseException e) {
            log.warn("Ungültiges Datumsformat. Verwenden Sie das Format YYYY-MM-DD (z.B. 1990-05-21)");
        } catch (NoSuchAlgorithmException e) {
            log.error("Fehler beim Hashen des Passworts: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Fehler beim Erstellen des Benutzers: " + e.getMessage(), e);
        }
    }

    /**
     * Quits the application
     */
    private void quit() {
        log.info("Programm wird beendet...");
        System.exit(0);
    }

    /**
     * Executes a command
     * @param commandName name of the command to execute
     * @return true if command was found and executed, false otherwise
     */
    public boolean executeCommand(String commandName) {
        Runnable command = commands.get(commandName);
        if (command != null) {
            command.run();
            return true;
        }
        return false;
    }

    /**
     * Shows error message for unknown commands
     */
    public void showUnknownCommandError() {
        log.warn("Unbekannter Befehl!");
        log.info("Verfügbare Befehle: help, listBooks [limit], list, importBooks <FILE_PATH>, quit");
    }
}