package kamel.capstone.nosqlnode.data.observer;

public interface Subject<T> {
    void subscribe(Subscriber<T> subscriber);
    void unsubscribe(Subscriber<T> subscriber);
    void notifySubscribers();
}
