package pt.up.fe.cpd2223.server.userQueue;

import java.util.List;

public interface UserQueue {

    boolean addPlayer(QueueUser user);

    List<QueueUser> nextUserGroup();

    QueueUser getForId(long userId);

}
