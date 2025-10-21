package ch.bzz;

import ch.bzz.model.Book;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Main class for the Javalin REST API server
 */
public class JavalinMain {
    private static final Logger logger = LoggerFactory.getLogger(JavalinMain.class);
    private static final int PORT = 7070;
    
    public static void main(String[] args) {
        logger.info("Starting Javalin REST API server on port {}", PORT);
        
        Javalin app = Javalin.create().start(PORT);
        
        // GET /books endpoint with optional limit query parameter
        app.get("/books", JavalinMain::getBooks);
        
        logger.info("Javalin server started successfully on http://localhost:{}", PORT);
        
        // Add shutdown hook to properly close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Javalin server...");
            Database.close();
            app.stop();
        }));
    }
    
    /**
     * Handler for GET /books endpoint
     * Supports optional 'limit' query parameter
     */
    static void getBooks(Context ctx) {
        try {
            String limitParam = ctx.queryParam("limit");
            List<Book> books;
            
            if (limitParam != null && !limitParam.isEmpty()) {
                try {
                    int limit = Integer.parseInt(limitParam);
                    books = Database.getAllBooks(limit);
                    logger.info("Retrieved {} books with limit {}", books.size(), limit);
                } catch (NumberFormatException e) {
                    ctx.status(400).json(new ErrorResponse("Invalid limit parameter: must be a number"));
                    return;
                }
            } else {
                books = Database.getAllBooks();
                logger.info("Retrieved {} books without limit", books.size());
            }
            
            ctx.json(books);
        } catch (Exception e) {
            logger.error("Error retrieving books", e);
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        }
    }
    
    /**
     * Simple error response class for JSON serialization
     */
    private static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}