/**
 * Created by emilhornlund on 2017-03-04
 */

import java.util.NoSuchElementException;

public class LinkedQueue<T> implements IQueue<T> {
    private class Node {
        T element;
        Node next;

        Node(T element, Node next) {
            this.element = element;
            this.next = next;
        }

        Node(T element) {
            this(element, null);
        }
    }

    private Node head;
    private Node tail;

    public LinkedQueue() {
        this.head = this.tail = null;
    }

    public void enqueue(T element) {
        if (this.head == null) {
            this.head = new Node(element);
            this.tail = this.head;
        } else {
            this.tail.next = new Node(element, this.tail);
            this.tail = this.tail.next;
        }
    }

    public T dequeue() {
        if (this.isEmpty())
            throw new NoSuchElementException("Queue is empty");
        T element = this.head.element;
        if (this.head == this.tail) {
            this.head = this.tail = null;
        } else {
            this.head = this.head.next;
        }
        return element;
    }

    public T peek() {
        if (this.isEmpty())
            throw new NoSuchElementException("Queue is empty");
        return this.head.element;
    }

    public boolean isEmpty() {
        return this.head == null;
    }

    public void removeAllElements() {
        while (head != null) {
            head = head.next;
        }
        tail = null;
    }
}
