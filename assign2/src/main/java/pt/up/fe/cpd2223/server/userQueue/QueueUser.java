package pt.up.fe.cpd2223.server.userQueue;

import pt.up.fe.cpd2223.common.model.User;

import java.nio.channels.SocketChannel;

public record QueueUser(User user, SocketChannel channel) {
}
