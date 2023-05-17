package pt.up.fe.cpd2223.server;

import pt.up.fe.cpd2223.common.message.Message;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {

    private final Queue<Message> messageQueue;

    public MessageQueue() {
        this.messageQueue = new LinkedList<>();
    }

    public synchronized boolean enqueueMessage(Message m) {
        this.notifyAll();
        return this.messageQueue.offer(m);
    }

    public synchronized Message pollMessage(long timeout) {

        if (this.messageQueue.size() == 0) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this.messageQueue.poll();
    }

}
