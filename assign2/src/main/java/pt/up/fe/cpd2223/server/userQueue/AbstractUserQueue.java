package pt.up.fe.cpd2223.server.userQueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractUserQueue implements UserQueue{

    protected final Lock lock = new ReentrantLock();
    protected final Condition condition = this.lock.newCondition();

    protected final int gameGroupSize;

    protected AbstractUserQueue(int gameGroupSize) {
        this.gameGroupSize = gameGroupSize;
    }
}
