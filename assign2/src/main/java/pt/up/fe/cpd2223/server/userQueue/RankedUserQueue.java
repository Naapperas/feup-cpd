package pt.up.fe.cpd2223.server.userQueue;

import java.util.*;
import java.util.stream.IntStream;

public class RankedUserQueue extends AbstractUserQueue {

    private final TreeMap<QueueUser, QueueUser> users; // TODO: should this be a queue?

    public RankedUserQueue(int gameGroupSize) {
        super(gameGroupSize);
        this.users = new TreeMap<>((quser1, quser2) -> {
            if (quser1.user().id() == quser2.user().id()) return 0;

            return quser1.user().elo() > quser2.user().elo() ? 1 : -1;
        });
    }

    @Override
    public boolean addPlayer(QueueUser player) {
//        try {
//            this.lock.lock();
//
//            boolean userAppended = false;
//
//            if (this.users.containsKey(player)) {
//                // this situation might happen when we disconnect, in which case we should update the entry with the new channel
//
//
//
//                int userPosition = userPositionInQueueOpt.getAsInt();
//
//                this.users.set(userPosition, player);
//            } else {
//                userAppended = this.users.offer(player);
//            }
//
//            if (this.users.size() >= this.gameGroupSize) this.condition.signal();
//
//            return userAppended;
//        } finally {
//            this.lock.unlock();
//        }
        return false;
    }

    @Override
    public List<QueueUser> nextUserGroup() {
//        try {
//            this.lock.lock();
//
//            while (this.users.size() < this.gameGroupSize) {
//                this.condition.await();
//            }
//
//            var selectedUsers = this.users.stream().limit(this.gameGroupSize).toList();
//
//            if (selectedUsers.stream().anyMatch((quser) -> !quser.channel().isConnected())) return null;
//
//            this.users.removeAll(selectedUsers);
//
//            return selectedUsers;
//        } catch (InterruptedException e) {
//            return null;
//        } finally {
//            this.lock.unlock();
//        }
        return null;
    }

    @Override
    public QueueUser getForId(long userId) {
        return null;
    }
}
