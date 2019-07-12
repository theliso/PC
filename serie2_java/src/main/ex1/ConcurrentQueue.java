package main.ex1;

import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentQueue<T> {

    private final Node<T> dummy = new Node<>(null, null);
    private final AtomicReference<Node<T>> head = new AtomicReference<>(dummy);
    private final AtomicReference<Node<T>> tail = new AtomicReference<>(dummy);

    public boolean put(T item){
        Node<T> newNode = new Node<>(item, null);
        while (true) {
            Node<T> curTail = tail.get();
            Node<T> tailNext = curTail.next.get();
            if (curTail == tail.get()) {
                if (tailNext != null) {
                    // Queue in intermediate state, advance tail
                    tail.compareAndSet(curTail, tailNext);
                } else {
                    // In quiescent state, try inserting new node
                    if (curTail.next.compareAndSet(null, newNode)) {
                        // Insertion succeeded, try advancing tail
                        tail.compareAndSet(curTail, newNode);
                        return true;
                    }
                }
            }
        }
    }

    public T tryTake(){
        Node<T> firstNode;
        do{
            firstNode = head.get().next.get();
            if(firstNode == null){
                return null;
            }
        } while(!head.get().next.compareAndSet(firstNode, firstNode.next.get()));
        return firstNode.item;
    }

    public boolean isEmpty(){
        return head.get() == tail.get();
    }


}
