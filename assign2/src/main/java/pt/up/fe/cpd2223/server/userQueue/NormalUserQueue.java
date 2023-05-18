package pt.up.fe.cpd2223.server.userQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class NormalUserQueue extends AbstractUserQueue {

    private final LinkedList<QueueUser> users; // TODO: should this be a queue?

    public NormalUserQueue(int gameGroupSize) {
        super(gameGroupSize);
        this.users = new LinkedList<>();
    }

    @Override
    public boolean addPlayer(QueueUser player) {
        try {
            this.lock.lock();

            boolean userAppended = false;

            OptionalInt userPositionInQueueOpt = IntStream.range(0, users.size())
                    .filter(i -> users.get(i).user().id() == player.user().id())
                    .findFirst();

            if (userPositionInQueueOpt.isPresent()) {
                // this situation might happen when we disconnect, in which case we should update the entry with the new channel

                int userPosition = userPositionInQueueOpt.getAsInt();

                this.users.set(userPosition, player);
            } else {
                userAppended = this.users.offer(player);
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

            var selectedUsers = this.users.stream().filter((queueUser -> queueUser.channel().isConnected())).limit(this.gameGroupSize).toList();

            if (selectedUsers.size() < this.gameGroupSize) return null;

            this.users.removeAll(selectedUsers);

            return selectedUsers;
        } catch (InterruptedException e) {
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public QueueUser getForId(long userId) {
        return this.users.stream().filter((queueUser -> queueUser.user().id() == userId)).findFirst().orElse(null);
    }
}
