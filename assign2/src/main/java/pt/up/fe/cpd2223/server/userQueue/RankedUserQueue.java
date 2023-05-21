package pt.up.fe.cpd2223.server.userQueue;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class RankedUserQueue extends AbstractUserQueue {

    private final TreeMap<Long, List<QueueUser>> userBins;
    private final long binSize;

    private ReentrantLock lock = new ReentrantLock();
    private Condition sufficientPlayers = lock.newCondition();

    public RankedUserQueue(int gameGroupSize) {
        super(gameGroupSize);
        this.userBins = new TreeMap<>();
        this.binSize = 15;
    }

    @Override
    public boolean addPlayer(QueueUser player) {
        lock.lock();

        try {
            // TODO: add breaking conditions if needed
            // if (<cenas>) return false;

            long binIndex = player.user().elo() / binSize;

            var bin = userBins.computeIfAbsent(binIndex, k -> new ArrayList<>());

            boolean userAdded = false;

            OptionalInt userPositionInQueueOpt = IntStream.range(0, bin.size())
                    .filter(i -> bin.get(i).user().id() == player.user().id())
                    .findFirst();

            if (userPositionInQueueOpt.isPresent()) {
                // this situation might happen when we disconnect, in which case we should update the entry with the new channel

                int userPosition = userPositionInQueueOpt.getAsInt();

                bin.set(userPosition, player);
            } else {
                userAdded = bin.add(player);
            }

            if (userBins.get(binIndex).size() >= this.gameGroupSize)
                sufficientPlayers.signal();

            return userAdded;
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
                    group.add(nextBin.getValue().remove(nextBin.getValue().size() - 1));
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

        // We will never have problems with exhausting the given bin because this function is called only if we have enough users in the bin.
        while (group.size() < gameGroupSize)
            group.add(userBins.get(bin).remove(userBins.get(bin).size() - 1));

        if (userBins.get(bin).isEmpty())
            userBins.remove(bin);

        return group;
    }

    @Override
    public QueueUser getForId(long userId) {
        for (var bin : userBins.entrySet()) {
            for (QueueUser user : bin.getValue()) {
                if (user.user().id() == userId)
                    return user;
            }
        }

        return null;
    }
}
