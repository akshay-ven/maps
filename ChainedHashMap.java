package maps;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ChainedHashMap<K, V> extends AbstractIterableMap<K, V> {
    private int size;
    private int userInitialChainCap;
    private int userInitialChainCount;
    private double userResizingFactor;
    private static final int DEFAULT_INITIAL_CHAIN_COUNT = 10;
    private static final int DEFAULT_INITIAL_CHAIN_CAPACITY = 10;
    private static final double DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD = 1.0;
    AbstractIterableMap<K, V>[] chains;

    public ChainedHashMap() {
        this(DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD, DEFAULT_INITIAL_CHAIN_COUNT, DEFAULT_INITIAL_CHAIN_CAPACITY);
    }

    public ChainedHashMap(double resizingLoadFactorThreshold, int initialChainCount, int chainInitialCapacity) {
        size = 0;
        this.userInitialChainCount = initialChainCount;
        this.chains = createArrayOfChains(initialChainCount);
        this.userInitialChainCap = chainInitialCapacity;
        this.userResizingFactor = resizingLoadFactorThreshold;
        for (int i = 0; i < initialChainCount; i++) {
            chains[i] = createChain(chainInitialCapacity);
        }
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code AbstractIterableMap<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     * @see ArrayMap createArrayOfEntries method for more background on why we need this method
     */
    @SuppressWarnings("unchecked")
    private AbstractIterableMap<K, V>[] createArrayOfChains(int arraySize) {
        return (AbstractIterableMap<K, V>[]) new AbstractIterableMap[arraySize];
    }

    protected AbstractIterableMap<K, V> createChain(int initialSize) {
        return new ArrayMap<>(initialSize);
    }

    @Override
    public V get(Object key) {
        if (size == 0) {
            return null;
        } else {
            int hashedKey = chainHashedKey(key);
            return chains[hashedKey].get(key);
        }
    }

    @Override
    public V put(K key, V value) {
        int hashedKey = chainHashedKey(key);
        V oldVal = null;
        if (chains[hashedKey] != null) {
            oldVal = chains[hashedKey].get(key);
        } else {
            chains[hashedKey] = createChain(userInitialChainCap);
        }
        int oldSize = chains[hashedKey].size();
        chains[hashedKey].put(key, value);
        if (oldSize < chains[hashedKey].size()) {
            size++;
        }
        if ((double) size / (double) chains.length > userResizingFactor) {
            chains = resizeAndRehash();
        }
        return oldVal;
    }

    @Override
    public V remove(Object key) {
        if (size == 0) {
            return null;
        } else {
            int hashedKey = chainHashedKey(key);
            if (containsKey(key)) {
                size--;
            }
            return chains[hashedKey].remove(key);
        }
    }

    @Override
    public void clear() {
        Arrays.fill(chains, null);
        size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        int hashedKey = chainHashedKey(key);
        if (chains[hashedKey] != null) {
            return chains[hashedKey].containsKey(key);
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        return size;
    }

    private int chainHashedKey(Object key) {
        if (key != null) {
            return Math.abs(key.hashCode()) % chains.length;
        } else {
            return 0;
        }
    }

    private AbstractIterableMap<K, V>[] resizeAndRehash() {
        AbstractIterableMap<K, V>[] newChain = createArrayOfChains(2 * chains.length);
        for (Map.Entry<K, V> entry : this) {
            int newHashedKey = Math.abs(entry.getKey().hashCode());
            if (newChain[newHashedKey % newChain.length] == null) {
                newChain[newHashedKey % newChain.length] = createChain(userInitialChainCount);
            }
            newChain[newHashedKey % newChain.length].put(entry.getKey(), entry.getValue());
        }
        return newChain;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new ChainedHashMapIterator<>(this.chains);
    }
    
    private static class ChainedHashMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private AbstractIterableMap<K, V>[] chains;
        private int currIndex;
        private Iterator<Map.Entry<K, V>> arrayMapIterator;

        public ChainedHashMapIterator(AbstractIterableMap<K, V>[] chains) {
            this.chains = chains;
            currIndex = 0;
            for (int i = currIndex; i < chains.length; i++) {
                if (chains[i] != null) {
                    arrayMapIterator = chains[i].iterator();
                    break;
                }
            }
        }

        @Override
        public boolean hasNext() {
            for (int i = currIndex; i < chains.length; i++) {
                if (arrayMapIterator != null && arrayMapIterator.hasNext()) {
                    return true;
                } else if (currIndex == chains.length - 1) {
                    return false;
                }
                currIndex++;
                if (chains[currIndex] != null) {
                    arrayMapIterator = chains[currIndex].iterator();
                }
            }
            return false;
        }

        @Override
        public Entry<K, V> next() {
            if (arrayMapIterator != null && arrayMapIterator.hasNext()) {
                return arrayMapIterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
