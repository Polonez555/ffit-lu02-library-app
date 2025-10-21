package ch.bzz;

import ch.bzz.model.User;
import ch.bzz.util.PasswordHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

/**
 * Test class for User functionality
 */
public class UserTest {
    
    @BeforeEach
    void setUp() {
        // Database is initialized automatically via static initialization
        // No explicit init() method needed
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after tests if needed
    }
    
    @Test
    void testUserEntityCreation() {
        // Test User entity creation with all fields
        User user = new User();
        user.setFirstname("Max");
        user.setLastname("Mustermann");
        user.setDateOfBirth(LocalDate.of(1990, 5, 21));
        user.setEmail("max.mustermann@example.com");
        user.setPasswordHash("hashedPassword");
        user.setPasswordSalt("saltValue");
        
        assertEquals("Max", user.getFirstname());
        assertEquals("Mustermann", user.getLastname());
        assertEquals(LocalDate.of(1990, 5, 21), user.getDateOfBirth());
        assertEquals("max.mustermann@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals("saltValue", user.getPasswordSalt());
    }
    
    @Test
    void testPasswordHashing() throws Exception {
        // Test password hashing functionality
        String password = "geheim123";
        byte[] salt = PasswordHandler.generateSalt();
        byte[] hash = PasswordHandler.hashPassword(password, salt);
        
        assertNotNull(salt);
        assertNotNull(hash);
        assertEquals(16, salt.length); // Salt should be 16 bytes
        assertEquals(32, hash.length); // SHA-256 hash should be 32 bytes
        
        // Test that same password with same salt produces same hash
        byte[] hash2 = PasswordHandler.hashPassword(password, salt);
        assertArrayEquals(hash, hash2);
        
        // Test that different passwords produce different hashes
        byte[] differentHash = PasswordHandler.hashPassword("differentPassword", salt);
        assertFalse(java.util.Arrays.equals(hash, differentHash));
    }
    
    @Test
    void testBase64Encoding() throws Exception {
        // Test Base64 encoding of salt and hash
        String password = "geheim123";
        byte[] salt = PasswordHandler.generateSalt();
        byte[] hash = PasswordHandler.hashPassword(password, salt);
        
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        
        assertNotNull(saltBase64);
        assertNotNull(hashBase64);
        assertTrue(saltBase64.length() > 0);
        assertTrue(hashBase64.length() > 0);
        
        // Test that we can decode back to original values
        byte[] decodedSalt = Base64.getDecoder().decode(saltBase64);
        byte[] decodedHash = Base64.getDecoder().decode(hashBase64);
        
        assertArrayEquals(salt, decodedSalt);
        assertArrayEquals(hash, decodedHash);
    }
    
    @Test
    void testSaveAndRetrieveUser() throws Exception {
        // Test saving and retrieving a user from the database
        String password = "geheim123";
        byte[] salt = PasswordHandler.generateSalt();
        byte[] hash = PasswordHandler.hashPassword(password, salt);
        
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        
        // Use unique email with timestamp to avoid conflicts
        String uniqueEmail = "test.user." + System.currentTimeMillis() + "@example.com";
        
        User user = new User();
        user.setFirstname("Test");
        user.setLastname("User");
        user.setDateOfBirth(LocalDate.of(1995, 3, 15));
        user.setEmail(uniqueEmail);
        user.setPasswordSalt(saltBase64);
        user.setPasswordHash(hashBase64);
        
        // Save user
        Database.saveUser(user);
        assertNotNull(user.getId()); // ID should be generated
        
        // Retrieve all users and verify our user is there
        List<User> users = Database.getAllUsers();
        assertTrue(users.size() > 0);
        
        // Find our user in the list
        User retrievedUser = users.stream()
            .filter(u -> uniqueEmail.equals(u.getEmail()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(retrievedUser);
        assertEquals("Test", retrievedUser.getFirstname());
        assertEquals("User", retrievedUser.getLastname());
        assertEquals(LocalDate.of(1995, 3, 15), retrievedUser.getDateOfBirth());
        assertEquals(uniqueEmail, retrievedUser.getEmail());
        assertEquals(saltBase64, retrievedUser.getPasswordSalt());
        assertEquals(hashBase64, retrievedUser.getPasswordHash());
    }
    
    @Test
    void testCreateUserCommand() {
        // Test the createUser command functionality
        CommandHandler commandHandler = new CommandHandler();
        
        // Use unique email with timestamp to avoid conflicts
        String uniqueEmail = "john.doe." + System.currentTimeMillis() + "@example.com";
        
        // Test valid user creation
        String userInfo = "John Doe 1985-12-10 " + uniqueEmail + " secret456";
        assertDoesNotThrow(() -> commandHandler.createUser(userInfo));
        
        // Verify user was created by checking the database
        List<User> users = Database.getAllUsers();
        User createdUser = users.stream()
            .filter(u -> uniqueEmail.equals(u.getEmail()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(createdUser);
        assertEquals("John", createdUser.getFirstname());
        assertEquals("Doe", createdUser.getLastname());
        assertEquals(LocalDate.of(1985, 12, 10), createdUser.getDateOfBirth());
        assertEquals(uniqueEmail, createdUser.getEmail());
        assertNotNull(createdUser.getPasswordHash());
        assertNotNull(createdUser.getPasswordSalt());
    }
    
    @Test
    void testCreateUserCommandWithInvalidData() {
        CommandHandler commandHandler = new CommandHandler();
        
        // Test with insufficient parameters
        assertDoesNotThrow(() -> commandHandler.createUser("John Doe"));
        
        // Test with invalid date format
        assertDoesNotThrow(() -> commandHandler.createUser("John Doe invalid-date john@example.com password"));
        
        // Test with null input
        assertDoesNotThrow(() -> commandHandler.createUser(null));
        
        // Test with empty input
        assertDoesNotThrow(() -> commandHandler.createUser(""));
    }
}