/**
 * Created by emilhornlund on 2017-03-04
 */
public interface IQueue<T> {
    void enqueue(T element);
    T dequeue();
    T peek();
    boolean isEmpty();
}
