package pt.up.fe.cpd2223.server.userQueue;

import java.util.List;

public interface UserQueue {

    boolean addUser(QueueUser user);

    List<QueueUser> nextUserGroup();

}
