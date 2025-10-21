package ch.bzz;

import ch.bzz.model.Book;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JavalinMain REST API endpoints
 */
public class JavalinMainTest {
    
    private Javalin app;
    
    @BeforeEach
    void setUp() {
        // Create Javalin app with the same configuration as JavalinMain
        app = Javalin.create();
        app.get("/books", JavalinMain::getBooks);
    }
    
    @AfterEach
    void tearDown() {
        if (app != null) {
            app.stop();
        }
    }
    
    @Test
    void testGetBooksEndpoint() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/books");
            assertEquals(200, response.code());
            assertEquals("application/json", response.header("Content-Type"));
            assertNotNull(response.body().string());
        });
    }
    
    @Test
    void testGetBooksWithLimitParameter() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/books?limit=2");
            assertEquals(200, response.code());
            assertEquals("application/json", response.header("Content-Type"));
            
            String responseBody = response.body().string();
            assertNotNull(responseBody);
            assertTrue(responseBody.startsWith("["));
            assertTrue(responseBody.endsWith("]"));
        });
    }
    
    @Test
    void testGetBooksWithInvalidLimitParameter() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/books?limit=invalid");
            assertEquals(400, response.code());
            assertEquals("application/json", response.header("Content-Type"));
            
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("Invalid limit parameter"));
        });
    }
    
    @Test
    void testGetBooksWithZeroLimit() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/books?limit=0");
            assertEquals(200, response.code());
            assertEquals("application/json", response.header("Content-Type"));
        });
    }
    
    @Test
    void testGetBooksWithNegativeLimit() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/books?limit=-1");
            assertEquals(200, response.code());
            assertEquals("application/json", response.header("Content-Type"));
        });
    }
}