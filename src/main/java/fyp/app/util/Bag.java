package fyp.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bag <T> {
    private List<T> bag;

    public Bag(){
        bag = new ArrayList<T>();
    }

    public Bag(T[] arr){
        bag = new ArrayList<T>();
        for(T a: arr) bag.add(a);
    }

    public Bag(List<T> arr) {
        bag = arr;
    }

    public void add (T a) {
        this.bag.add(a);
    }

    public T randomPop () {
        Random rnd = new Random();
        int index = rnd.nextInt(this.bag.size());
        return bag.remove(index);
    }

    public List<T> getBag() {
        return bag;
    }

    public void setBag(ArrayList<T> bag) {
        this.bag = bag;
    }

    public boolean isEmpty(){
        return bag.isEmpty();
    }
}
