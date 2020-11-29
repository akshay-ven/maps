package maps;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;


/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ArrayMap<K, V> extends AbstractIterableMap<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private int size;
    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    SimpleEntry<K, V>[] entries;

    // You may add extra fields or helper methods though!

    public ArrayMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public ArrayMap(int initialCapacity) {
        this.entries = this.createArrayOfEntries(initialCapacity);
        size = 0;
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code Entry<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     */
    @SuppressWarnings("unchecked")
    private SimpleEntry<K, V>[] createArrayOfEntries(int arraySize) {
        /*
        It turns out that creating arrays of generic objects in Java is complicated due to something
        known as "type erasure."

        We've given you this helper method to help simplify this part of your assignment. Use this
        helper method as appropriate when implementing the rest of this class.

        You are not required to understand how this method works, what type erasure is, or how
        arrays and generics interact.
        */
        return (SimpleEntry<K, V>[]) (new SimpleEntry[arraySize]);
    }

    @Override
    public V get(Object key) {
        for (int i = 0; i < size; i++) {
            if (java.util.Objects.equals(entries[i].getKey(), key)) {
                return entries[i].getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        if (size == entries.length) {
            entries = resize();
        }
        if (containsKey(key)) {
            for (int i = 0; i < size; i++) { // if Key exists
                if (Objects.equals(entries[i].getKey(), key)) {
                    V oldVal = entries[i].getValue();
                    entries[i].setValue(value);
                    return oldVal;
                }
            }
        } else {
            entries[size] = new SimpleEntry<>(key, value);
            size++;
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        for (int i = 0; i < size; i++) { // AK05
            if (Objects.equals(entries[i].getKey(), key)) {
                V removedV = entries[i].getValue();
                entries[i] = entries[size - 1];
                entries[size - 1] = null;
                size--;
                return removedV;
            }
        }
        return null;
    }


    @Override
    public void clear() {
        Arrays.fill(entries, null);
        size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        for (int i = 0; i < size; i++) {
            if (java.util.Objects.equals(entries[i].getKey(), key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    private SimpleEntry<K, V>[] resize() {
        SimpleEntry<K, V>[] newArrayMap = createArrayOfEntries(2 * size);
        for (int i = 0; i < size; i++) {
            newArrayMap[i] = entries[i];
        }
        return newArrayMap;
    }

    // You may NOT create any new temporary data structures inside of your iterators.
    // We want our iterators to be efficient, and having to copy the contents of our
    // map to some other data structure at any point is suboptimal.
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: you won't need to change this method (unless you add more constructor parameters)
        return new ArrayMapIterator<>(this.entries);
    }

    private static class ArrayMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private final SimpleEntry<K, V>[] entries;
        // You may add more fields and constructor parameters
        private int currIndex;

        public ArrayMapIterator(SimpleEntry<K, V>[] entries) {
            this.entries = entries;
            currIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return ((entries.length - 1 >= currIndex) && (entries[currIndex] != null));
        }

        @Override
        public Map.Entry<K, V> next() {
            if (this.hasNext()) {
                Map.Entry<K, V> entry = entries[currIndex];
                currIndex++;
                return entry;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
