package pt.up.fe.cpd2223.common.model;

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

    public String toString() {
        return "%d:%s:%s:%d".formatted(id, username, password, elo);
    }
}
