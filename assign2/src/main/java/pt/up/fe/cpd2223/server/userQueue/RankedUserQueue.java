package pt.up.fe.cpd2223.server.userQueue;

import java.util.List;

public class RankedUserQueue extends AbstractUserQueue {
    public RankedUserQueue(int gameGroupSize) {
        super(gameGroupSize);
    }

    @Override
    public boolean addUser(QueueUser user) {
        return false;
    }

    @Override
    public List<QueueUser> nextUserGroup() {
        return null;
    }
}
