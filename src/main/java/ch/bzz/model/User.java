package ch.bzz.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a user in the library system
 * JPA Entity for database synchronization
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "firstname", nullable = false, length = 100)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 100)
    private String lastname;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 500)
    private String passwordHash;

    @Column(name = "password_salt", nullable = false, length = 500)
    private String passwordSalt;

    /**
     * Default constructor required by JPA/Hibernate
     */
    public User() {}

    /**
     * Constructor for User
     * @param id the user ID
     * @param firstname the user's first name
     * @param lastname the user's last name
     * @param dateOfBirth the user's date of birth
     * @param email the user's email address
     * @param passwordHash the hashed password
     * @param passwordSalt the password salt
     */
    public User(Integer id, String firstname, String lastname, LocalDate dateOfBirth, 
                String email, String passwordHash, String passwordSalt) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    @Override
    public String toString() {
        return String.format("%d | %s | %s | %s | %s", 
            id, firstname, lastname, dateOfBirth, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}