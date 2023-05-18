package pt.up.fe.cpd2223.server.userQueue;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class NormalUserQueue extends AbstractUserQueue {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = this.lock.newCondition();

    private final LinkedList<QueueUser> users; // TODO: should this be a queue?

    public NormalUserQueue(int gameGroupSize) {
        super(gameGroupSize);
        this.users = new LinkedList<>();
    }

    @Override
    public boolean addUser(QueueUser user) {

        try {
            this.lock.lock();

            boolean userAppended = false;

            OptionalInt userPositionInQueueOpt = IntStream.range(0, users.size())
                    .filter(i -> users.get(i).user().id() == user.user().id())
                    .findFirst();

            if (userPositionInQueueOpt.isPresent()) {
                // this situation might happen when we disconnect, in which case we should update the entry with the new channel

                int userPosition = userPositionInQueueOpt.getAsInt();

                this.users.add(userPosition, user);
                this.users.remove(userPosition  + 1);

            } else {
                userAppended = this.users.offer(user);
            }

            if (this.users.size() >= this.gameGroupSize) this.condition.signal();

            return userAppended;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public List<QueueUser> nextUserGroup() {
        try {
            this.lock.lock();

            while (this.users.size() < this.gameGroupSize) {
                this.condition.await();
            }

            var selectedUsers = this.users.stream().limit(this.gameGroupSize).toList();

            if (selectedUsers.stream().anyMatch((quser) -> !quser.channel().isConnected())) return null;

            this.users.removeAll(selectedUsers);

            return selectedUsers;
        } catch (InterruptedException e) {
            return null;
        } finally {
            this.lock.unlock();
        }
    }
}
