package pt.up.fe.cpd2223.server.service;

import pt.up.fe.cpd2223.common.model.User;
import pt.up.fe.cpd2223.server.repository.UserRepository;

public final class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository repository) {
        this.userRepository = repository;
    }

    public User login(String username, String password) {
        var user = this.userRepository.findByUsername(username);

        if (user == null)
            return null;

        if (!user.password().equals(password)) // TODO: change to encoded passwords
            return null;

        return user;
    }

    public User register(String username, String password) {

        var user = this.userRepository.findByUsername(username);

        if (user != null)
            return null;

        var id = this.userRepository.nextUserId();

        user = new User(id, username, password, 1000);

        this.userRepository.getUsers().add(user);
        this.userRepository.saveUsers();

        return user;
    }
}
