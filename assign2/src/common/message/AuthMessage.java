package common.message;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AuthMessage implements Message {

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
