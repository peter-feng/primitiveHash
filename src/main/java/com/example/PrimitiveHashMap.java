package com.example;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * A primitive long-to-long hash map implementation that uses off-heap memory.
 * This implementation uses open addressing with linear probing for collision resolution.
 * The map stores key-value pairs in direct ByteBuffer to avoid GC pressure.
 */
public class PrimitiveHashMap implements AutoCloseable {
    private static final int ENTRY_SIZE = 16; // 8 bytes for key + 8 bytes for value
    private static final long EMPTY_KEY = 0L;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private ByteBuffer buffer;
    private int capacity;
    private int size;
    private final long emptyMarker;

    public PrimitiveHashMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public PrimitiveHashMap(int initialCapacity) {
        this(initialCapacity, EMPTY_KEY);
    }

    public PrimitiveHashMap(int initialCapacity, long emptyMarker) {
        this.capacity = nextPowerOfTwo(initialCapacity);
        this.emptyMarker = emptyMarker;
        this.size = 0;
        allocateBuffer(this.capacity);
        clear();
    }

    private void allocateBuffer(int capacity) {
        if (buffer != null) {
            buffer.clear();
        }
        // Allocate direct buffer (off-heap)
        buffer = ByteBuffer.allocateDirect(capacity * ENTRY_SIZE);
    }

    public void put(long key, long value) {
        if (key == emptyMarker) {
            throw new IllegalArgumentException("Key cannot be empty marker value");
        }

        if (size >= capacity * LOAD_FACTOR) {
            resize();
        }

        int index = findSlot(key);
        boolean isNewKey = getKeyAt(index) == emptyMarker;
        putEntry(index, key, value);
        if (isNewKey) {
            size++;
        }
    }

    public long get(long key) {
        if (key == emptyMarker) {
            throw new IllegalArgumentException("Key cannot be empty marker value");
        }

        int index = findSlot(key);
        long storedKey = getKeyAt(index);
        if (storedKey == emptyMarker) {
            throw new NoSuchElementException("Key not found: " + key);
        }
        return getValueAt(index);
    }

    public boolean containsKey(long key) {
        if (key == emptyMarker) return false;
        int index = findSlot(key);
        return getKeyAt(index) == key;
    }

    public void remove(long key) {
        if (key == emptyMarker) return;

        int index = findSlotForRemoval(key);
        if (index >= 0) {
            putEntry(index, emptyMarker, 0L);
            size--;
            verifyAndGetSize();  // Verify size is correct after removal
        }
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            putEntry(i, emptyMarker, 0L);
        }
        size = 0;
    }

    public int size() {
        return size;
    }

    private int findSlot(long key) {
        int index = hash(key) & (capacity - 1);
        int startIndex = index;
        
        do {
            long currentKey = getKeyAt(index);
            if (currentKey == emptyMarker || currentKey == key) {
                return index;
            }
            
            index = (index + 1) & (capacity - 1);
        } while (index != startIndex);
        
        // We've wrapped around - map is full
        throw new IllegalStateException("Map is full");
    }

    private int findSlotForRemoval(long key) {
        int index = hash(key) & (capacity - 1);
        int startIndex = index;
        boolean foundEmpty = false;
        
        do {
            long currentKey = getKeyAt(index);
            if (currentKey == key) {
                return index;
            }
            if (currentKey == emptyMarker) {
                foundEmpty = true;
            }
            
            index = (index + 1) & (capacity - 1);
        } while (index != startIndex);
        
        // We've checked all slots and haven't found the key
        return -1;
    }

    private void resize() {
        int oldCapacity = capacity;
        ByteBuffer oldBuffer = buffer;
        int oldSize = size;

        capacity *= 2;
        allocateBuffer(capacity);
        size = 0;

        // Copy all non-empty entries to the new buffer
        for (int i = 0; i < oldCapacity; i++) {
            long key = getKeyAt(oldBuffer, i);
            if (key != emptyMarker) {
                long value = getValueAt(oldBuffer, i);
                int newIndex = findSlot(key);
                putEntry(newIndex, key, value);
                size++;
            }
        }

        if (size != oldSize) {
            throw new IllegalStateException(
                String.format("Size mismatch after resize: expected %d, got %d", oldSize, size));
        }
    }

    private static int hash(long key) {
        key = (key ^ (key >>> 32));
        return (int) key;
    }

    private void putEntry(int index, long key, long value) {
        int position = index * ENTRY_SIZE;
        buffer.putLong(position, key);
        buffer.putLong(position + 8, value);
    }

    private long getKeyAt(int index) {
        return buffer.getLong(index * ENTRY_SIZE);
    }

    private long getValueAt(int index) {
        return buffer.getLong(index * ENTRY_SIZE + 8);
    }

    private long getKeyAt(ByteBuffer buf, int index) {
        return buf.getLong(index * ENTRY_SIZE);
    }

    private long getValueAt(ByteBuffer buf, int index) {
        return buf.getLong(index * ENTRY_SIZE + 8);
    }

    private int verifyAndGetSize() {
        int actualSize = 0;
        for (int i = 0; i < capacity; i++) {
            if (getKeyAt(i) != emptyMarker) {
                actualSize++;
            }
        }
        if (actualSize != size) {
            System.err.println("Size mismatch: tracked=" + size + ", actual=" + actualSize);
        }
        return actualSize;
    }

    private static int nextPowerOfTwo(int value) {
        int highestOneBit = Integer.highestOneBit(value);
        return value > highestOneBit ? highestOneBit << 1 : highestOneBit;
    }

    @Override
    public void close() {
        // Help hint to the GC that the direct buffer can be freed
        buffer = null;
    }
}
