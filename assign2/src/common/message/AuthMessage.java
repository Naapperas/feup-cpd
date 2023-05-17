package common.message;

public abstract class AuthMessage extends Message {

    private final String username;
    private final String password;

    protected AuthMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String payload(){
        return "%s%s%s".formatted(this.username, Message.payloadDataSeparator(), this.password);
    }
}
