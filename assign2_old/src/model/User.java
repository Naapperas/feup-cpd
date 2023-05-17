package model;

public record User(long id, String username, String password, long elo) {
    public User {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        if (elo < 0) {
            throw new IllegalArgumentException("Elo cannot be negative");
        }
    }
}
