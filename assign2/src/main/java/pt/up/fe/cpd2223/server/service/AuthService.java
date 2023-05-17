package pt.up.fe.cpd2223.server.service;

import pt.up.fe.cpd2223.common.model.User;
import pt.up.fe.cpd2223.server.repository.UserRepository;

public final class AuthService {

    private static final UserRepository userRepository = new UserRepository();

    public static User login(String username, String password) {
        var user = userRepository.findByUsername(username);

        if (user == null)
            return null;

        if (!user.password().equals(password)) // TODO: change to encoded passwords
            return null;

        return user;
    }

    public static User register(String username, String password) {

        var user = userRepository.findByUsername(username);

        if (user != null)
            return null;

        var id = userRepository.nextUserId();

        user = new User(id, username, password, 1000);

        userRepository.getUsers().add(user);

        userRepository.saveUsers();

        return user;
    }
}
