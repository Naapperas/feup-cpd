package pt.up.fe.cpd2223.common.model;

public class User {

    private long userId, userElo;
    private String userName, userPassword;

    public User(long id, String username, String password, long elo) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        if (elo < 0) {
            throw new IllegalArgumentException("Elo cannot be negative");
        }

        this.userId = id;
        this.userElo = elo;
        this.userName = username;
        this.userPassword = password;
    }

    public long id() {
        return this.userId;
    }

    public long elo() {
        return userElo;
    }
    public void setElo(long newElo) {
        this.userElo = newElo;
    }

    public String username() {
        return this.userName;
    }

    public String password() {
        return this.userPassword;
    }

    public String toString() {
        return "%d:%s:%s:%d".formatted(userId, userName, userPassword, userElo);
    }
}
