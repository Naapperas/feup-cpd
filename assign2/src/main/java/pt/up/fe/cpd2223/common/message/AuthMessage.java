package pt.up.fe.cpd2223.common.message;

public abstract class AuthMessage extends Message {

    private final String username;
    private final String password;

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    protected AuthMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String payload(){
        return "%s%s%s".formatted(this.username, Message.payloadDataSeparator(), this.password);
    }
}
