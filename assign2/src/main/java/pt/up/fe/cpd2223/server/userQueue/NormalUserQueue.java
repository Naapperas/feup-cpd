package pt.up.fe.cpd2223.server.userQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NormalUserQueue extends AbstractUserQueue {

    private final Lock lock = new ReentrantLock();

    private final Queue<QueueUser> users;

    public NormalUserQueue(int gameGroupSize) {
        super(gameGroupSize);
        this.users = new LinkedList<>();
    }

    @Override
    public boolean addUser(QueueUser user) {

        try {
            this.lock.lock();

            boolean userAppended = this.users.offer(user);

            if (this.users.size() >= this.gameGroupSize) this.notify();

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
                this.wait();
            }

            var selectedUsers = this.users.stream().limit(this.gameGroupSize).toList();

            this.users.removeAll(selectedUsers);

            return selectedUsers;
        } catch (InterruptedException e) {
            return null;
        } finally {
            this.lock.unlock();
        }
    }
}
