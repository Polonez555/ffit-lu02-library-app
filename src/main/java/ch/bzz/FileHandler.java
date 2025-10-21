package ch.bzz;

import ch.bzz.model.Book;
import java.io.File;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileHandler class handles all file operations
 * Following the SRP principle by separating file handling concerns
 */
public class FileHandler {
    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    /**
     * Reads books from a TSV file and returns a List of Book objects
     * @param filePath path to the TSV file
     * @return List of Book objects parsed from the file
     */
    public static List<Book> readBooksFromTSV(String filePath) {
        List<Book> books = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            // Skip header line if present
            if (fileScanner.hasNextLine()) {
                String headerLine = fileScanner.nextLine();
                // Check if it's actually a header (contains "id", "isbn", etc.)
                if (!headerLine.toLowerCase().contains("id") || !headerLine.toLowerCase().contains("isbn")) {
                    // If not a header, parse it as data
                    String[] values = headerLine.split("\t");
                    if (values.length >= 5) {
                        try {
                            int id = Integer.parseInt(values[0].trim());
                            String isbn = values[1].trim();
                            String title = values[2].trim();
                            String author = values[3].trim();
                            int year = Integer.parseInt(values[4].trim());
                            books.add(new Book(id, isbn, title, author, year));
                        } catch (NumberFormatException e) {
                            log.warn("Error parsing line: " + headerLine, e);
                        }
                    }
                }
            }
            
            // Parse remaining lines
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] values = line.split("\t");
                if (values.length >= 5) {
                    try {
                        int id = Integer.parseInt(values[0].trim());
                        String isbn = values[1].trim();
                        String title = values[2].trim();
                        String author = values[3].trim();
                        int year = Integer.parseInt(values[4].trim());
                        books.add(new Book(id, isbn, title, author, year));
                    } catch (NumberFormatException e) {
                        log.warn("Error parsing line: " + line, e);
                    }
                } else {
                    log.warn("Invalid line format (expected 5 columns): " + line);
                }
            }
        } catch (java.io.FileNotFoundException e) {
            log.error("File not found: " + filePath, e);
        } catch (Exception e) {
            log.error("Error reading file: " + e.getMessage(), e);
        }
        
        return books;
    }
}