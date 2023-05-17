package pt.up.fe.cpd2223.server.repository;


import pt.up.fe.cpd2223.common.model.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;

public class UserRepository {

    private final Collection<User> users;

    private Lock lock;

    public UserRepository() {
        this.users = loadUsers();
    }

    public Collection<User> getUsers() {
        return this.users;
    }

    private Collection<User> loadUsers() {

        try (var lines = Files.lines(Path.of("users.db"));) {
            var users = new ArrayList<User>();

            for (Iterator<String> it = lines.iterator(); it.hasNext(); ) {
                var userLine = it.next();
                var userParts = userLine.split(":");

                if (userParts.length != 4)
                    continue;

                try {
                    var id = Integer.parseInt(userParts[0]);
                    var username = userParts[1];
                    var password = userParts[2];
                    var elo = Integer.parseInt(userParts[3]);

                    users.add(new User(id, username, password, elo));
                } catch (Exception ignored) {
                    // this is supposed to serve as a "continue"
                }
            }

            return users;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public long nextUserId() {
        return this.getUsers().stream().max(Comparator.comparingLong(User::id)).map(user -> user.id() + 1L).orElse(1L);
    }

    public User findByUsername(String username) {
        return this.getUsers().stream().filter(user -> user.username().equals(username)).findFirst().orElse(null);
    }

    public void saveUsers() {
        try {
            Files.write(Path.of("users.db"), this.getUsers().stream().map(User::toString).toList());
        } catch (Exception ignored) {
            System.err.println("Error saving users to file");
        }
    }
}
