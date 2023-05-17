package repository;

import model.User;

import java.util.ArrayList;
import java.util.Collection;

public class UserRepository {

    private final Collection<User> users;

    public UserRepository() {
        this.users = loadUsers();
    }

    public Collection<User> getUsers() {
        return this.users;
    }

    private Collection<User> loadUsers() {

        var users = new ArrayList<User>();

        return users;
    }
}
