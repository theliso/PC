package main.ex1;

import java.util.concurrent.atomic.AtomicReference;

public class Node<T> {

    public final T item;
    public final AtomicReference<Node<T>> next;

    public Node(T elem, AtomicReference<Node<T>> next) {
        this.item = elem;
        this.next = next;
    }
}
