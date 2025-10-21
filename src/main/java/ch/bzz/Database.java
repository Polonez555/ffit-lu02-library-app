package ch.bzz;

import ch.bzz.model.Book;
import ch.bzz.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database class handles all database connections and operations using JPA/Hibernate
 * Following the DRY principle by centralizing database access
 */
public class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private static final Properties config = loadDatabaseConfig();
    private static final EntityManagerFactory emf = createEntityManagerFactory();

    /**
     * Loads database configuration from config.properties file
     * @return Properties object containing database configuration
     */
    private static Properties loadDatabaseConfig() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            log.error("Error loading config.properties: " + e.getMessage(), e);
            log.error("Make sure config.properties exists in the root directory.");
            System.exit(1);
        }
        return properties;
    }

    /**
     * Creates EntityManagerFactory with configuration from config.properties
     * @return EntityManagerFactory configured for the application
     */
    private static EntityManagerFactory createEntityManagerFactory() {
        Properties jpaProperties = new Properties();
        
        // Map config.properties to JPA properties
        String dbUrl = config.getProperty("DB_URL");
        String dbUser = config.getProperty("DB_USER");
        String dbPassword = config.getProperty("DB_PASSWORD");
        
        // Also check for JPA-style properties
        if (config.getProperty("jakarta.persistence.jdbc.url") != null) {
            dbUrl = config.getProperty("jakarta.persistence.jdbc.url");
        }
        if (config.getProperty("jakarta.persistence.jdbc.user") != null) {
            dbUser = config.getProperty("jakarta.persistence.jdbc.user");
        }
        if (config.getProperty("jakarta.persistence.jdbc.password") != null) {
            dbPassword = config.getProperty("jakarta.persistence.jdbc.password");
        }
        
        jpaProperties.setProperty("jakarta.persistence.jdbc.url", dbUrl);
        jpaProperties.setProperty("jakarta.persistence.jdbc.user", dbUser);
        jpaProperties.setProperty("jakarta.persistence.jdbc.password", dbPassword);
        
        return Persistence.createEntityManagerFactory("localPU", jpaProperties);
    }

    /**
     * Gets the EntityManagerFactory
     * @return EntityManagerFactory instance
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    /**
     * Fetches all books from the database
     * @return List of Book objects from the database
     */
    public static List<Book> getAllBooks() {
        try (EntityManager em = emf.createEntityManager()) {
            var query = em.createQuery("SELECT b FROM Book b ORDER BY id", Book.class);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error fetching books from database: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetches books from the database with a limit
     * @param limit maximum number of books to return, 0 or negative for no limit
     * @return List of Book objects from the database
     */
    public static List<Book> getAllBooks(int limit) {
        try (EntityManager em = emf.createEntityManager()) {
            var query = em.createQuery("SELECT b FROM Book b ORDER BY id", Book.class);
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error fetching books from database: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Saves a list of books to the database, updating existing entries with same id
     * @param books list of books to save
     */
    public static void saveBooks(List<Book> books) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                for (Book book : books) {
                    em.merge(book); // merge handles both insert and update
                }
                em.getTransaction().commit();
                log.info(books.size() + " Bücher erfolgreich importiert/aktualisiert.");
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                log.error("Error during saving of books to the database:", e);
            }
        } catch (Exception e) {
            log.error("Error saving books to database: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches all users from the database
     * @return List of User objects from the database
     */
    public static List<User> getAllUsers() {
        try (EntityManager em = emf.createEntityManager()) {
            var query = em.createQuery("SELECT u FROM User u ORDER BY id", User.class);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error fetching users from database: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Saves a user to the database
     * @param user the user to save
     */
    public static void saveUser(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                em.merge(user); // merge handles both insert and update
                em.getTransaction().commit();
                log.info("Benutzer erfolgreich gespeichert: " + user.getEmail());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                log.error("Error during saving of user to the database:", e);
                throw e; // Re-throw to allow caller to handle
            }
        } catch (Exception e) {
            log.error("Error saving user to database: " + e.getMessage(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    /**
     * Closes the EntityManagerFactory when the application shuts down
     */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}