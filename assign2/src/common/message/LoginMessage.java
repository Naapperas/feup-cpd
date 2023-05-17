package common.message;

public class LoginMessage extends AuthMessage {
    public LoginMessage(String username, String password) {
        super(username, password);
    }

    @Override
    public MessageType type() {
        return MessageType.AUTH_LOGIN;
    }

}
