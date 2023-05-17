package pt.up.fe.cpd2223.common.message;

public class RegisterMessage extends AuthMessage {
    public RegisterMessage(String username, String password) {
        super(username, password);
    }

    @Override
    public MessageType type() {
        return MessageType.AUTH_REGISTER;
    }
}
