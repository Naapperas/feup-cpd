package pt.up.fe.cpd2223.server.userQueue;

public abstract class AbstractUserQueue implements UserQueue{

    protected final int gameGroupSize;

    protected AbstractUserQueue(int gameGroupSize) {
        this.gameGroupSize = gameGroupSize;
    }
}
