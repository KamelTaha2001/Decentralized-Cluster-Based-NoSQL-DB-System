package kamel.capstone.nosqlnode.data.observer;

import kamel.capstone.nosqlnode.data.model.Collection;

import java.util.LinkedList;
import java.util.List;

public class CollectionsSubject implements Subject<Collection> {
    private final List<Subscriber<Collection>> collectionSubscribers = new LinkedList<>();
    @Override
    public void subscribe(Subscriber<Collection> subscriber) {
        collectionSubscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(Subscriber<Collection> subscriber) {
        collectionSubscribers.remove(subscriber);
    }

    @Override
    public void notifySubscribers() {
        collectionSubscribers.forEach(Subscriber::update);
    }
}
