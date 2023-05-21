package pt.up.fe.cpd2223.server.userQueue;

import com.sun.source.tree.Tree;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class RankedUserQueue extends AbstractUserQueue {

    private final TreeMap<Long, List<QueueUser>> userBins;
    private final long binSize;

    private  ReentrantLock lock = new ReentrantLock();
    private Condition sufficientPlayers = lock.newCondition();

    public RankedUserQueue(int gameGroupSize) {
        super(gameGroupSize);
        this.userBins = new TreeMap<>();
        this.binSize = 5; //binSize;
        /*
        this.users = new TreeMap<>((quser1, quser2) -> {
            if (quser1.user().id() == quser2.user().id()) return 0;

            return quser1.user().elo() > quser2.user().elo() ? 1 : -1;
        });
        */
    }

    @Override
    public boolean addPlayer(QueueUser player) {
        lock.lock();

        try {
            // TODO: add breaking conditions if needed
            // if (<cenas>) return false;

            long binIndex = player.user().elo() / binSize;
            boolean userAdded = userBins.computeIfAbsent(binIndex, k -> new ArrayList<>()).add(player);

            if (userBins.get(binIndex).size() >= this.gameGroupSize)
                sufficientPlayers.signal();

            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<QueueUser> nextUserGroup() {
        lock.lock();
        try {
            while (userBins.isEmpty()) {
                sufficientPlayers.await();
            }

            for (long bin : userBins.keySet()) {
                if (userBins.get(bin).size() >= gameGroupSize) {
                    return getUsersFromBin(bin);
                }
            }

            // If no single bin has enough users, match from adjacent bins.
            Map.Entry<Long, List<QueueUser>> firstBin = userBins.firstEntry();
            List<QueueUser> group = new ArrayList<>(firstBin.getValue());
            userBins.remove(firstBin.getKey());
            while (group.size() < gameGroupSize && !userBins.isEmpty()) {
                Map.Entry<Long, List<QueueUser>> nextBin = userBins.firstEntry();
                while (!nextBin.getValue().isEmpty() && group.size() < gameGroupSize) {
                    group.add(nextBin.getValue().remove(nextBin.getValue().size()-1));
                }
                if (nextBin.getValue().isEmpty()) {
                    userBins.remove(nextBin.getKey());
                }
            }
            return group.size() >= gameGroupSize ? group : null;
        } catch (InterruptedException e) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    private List<QueueUser> getUsersFromBin(long bin) {
        List<QueueUser> group = new ArrayList<>();

        // TODO: If bin doesn't have enough people

        while (group.size() < gameGroupSize)
            group.add(userBins.get(bin).remove(userBins.get(bin).size() - 1));

        if (userBins.get(bin).isEmpty())
            userBins.remove(bin);

        return group;
    }

    @Override
    public QueueUser getForId(long userId) {
        lock.lock();
        try {
            for (var bin : userBins.entrySet()) {
                for (QueueUser user : bin.getValue()) {
                    if (user.user().id() == userId)
                        return user;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
}
