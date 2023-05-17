package server;

import common.message.Message;

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

    public synchronized Message pollMessage() {

        if (this.messageQueue.size() == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this.messageQueue.poll();
    }

}
