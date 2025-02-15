# PrimitiveHashMap

## Disclaimer
This library is generated by Windsurf AI. It passes all tests, but is not guaranteed to work in all cases. I'll be changing it over time. Use it at your own risk.

## Introduction
A high-performance, memory-efficient hash map implementation for primitive `long` keys and values. This implementation uses off-heap memory allocation to minimize garbage collection pressure, making it ideal for high-throughput scenarios where memory efficiency is crucial.

## Features

- **Off-heap Memory**: Uses `ByteBuffer.allocateDirect()` for memory allocation outside the Java heap
- **Primitive Types**: Specialized for `long` keys and values to avoid boxing/unboxing overhead
- **Linear Probing**: Implements open addressing with linear probing for collision resolution
- **Auto-resizing**: Automatically resizes when the load factor exceeds 0.75
- **Memory Efficient**: Minimizes memory overhead by storing primitives directly
- **GC Friendly**: Reduces garbage collection pressure by using off-heap memory

## Usage

### Basic Operations

```java
// Create a new map with default initial capacity (16)
try (PrimitiveHashMap map = new PrimitiveHashMap()) {
    // Insert key-value pairs
    map.put(1L, 100L);
    map.put(2L, 200L);

    // Retrieve values
    long value = map.get(1L);  // Returns 100L

    // Check if key exists
    boolean exists = map.containsKey(1L);  // Returns true

    // Remove a key-value pair
    map.remove(1L);

    // Get current size
    int size = map.size();

    // Clear all entries
    map.clear();
}
```

### Custom Initialization

```java
// Create a map with custom initial capacity
try (PrimitiveHashMap map = new PrimitiveHashMap(1024)) {
    // Use the map...
}

// Create a map with custom empty marker (default is 0L)
try (PrimitiveHashMap map = new PrimitiveHashMap(16, -1L)) {
    // Use the map...
}
```

## Implementation Details

### Memory Layout
- Each entry uses 16 bytes (8 bytes for key + 8 bytes for value)
- Uses direct ByteBuffer for off-heap memory allocation
- Memory is allocated in power-of-two sizes for efficient indexing

### Collision Resolution
- Uses open addressing with linear probing
- When a collision occurs, the implementation linearly probes subsequent slots until an empty slot is found
- During removal, the implementation continues searching past empty slots to handle keys that might have been pushed further due to collisions

### Resizing
- Automatically resizes when load factor exceeds 0.75
- New capacity is always a power of two
- During resize, all existing entries are rehashed into the new buffer

### Performance Characteristics
- **Put Operation**: O(1) average case, O(n) worst case
- **Get Operation**: O(1) average case, O(n) worst case
- **Remove Operation**: O(1) average case, O(n) worst case
- **Memory Usage**: 16 bytes per entry + buffer overhead

## Best Practices

1. **Resource Management**
   - Always use try-with-resources or explicitly call `close()` to properly release off-heap memory
   - The map implements `AutoCloseable` for automatic resource management

2. **Capacity Planning**
   - Initialize with an appropriate capacity to minimize resizing
   - Consider the load factor (0.75) when planning capacity

3. **Empty Key Handling**
   - By default, 0L is used as the empty marker
   - Avoid using the empty marker value as a key
   - Custom empty markers can be specified during construction

## Limitations

1. Only supports `long` keys and values
2. Keys cannot equal the empty marker value (default 0L)
3. Not thread-safe; external synchronization is required for concurrent access

## Example Use Cases

1. **High-Performance Caching**
   ```java
   try (PrimitiveHashMap cache = new PrimitiveHashMap(1024)) {
       // Use as a high-performance cache for primitive values
       cache.put(userId, timestamp);
   }
   ```

2. **Memory-Efficient Counting**
   ```java
   try (PrimitiveHashMap counter = new PrimitiveHashMap()) {
       // Use as a counter for large datasets
       counter.put(eventId, counter.getOrDefault(eventId, 0L) + 1);
   }
   ```

## Building and Testing

```bash
# Build the project
mvn clean install

# Run tests
mvn test
```

## Requirements

- Java 8 or higher
- Maven for building (if using build scripts)

## License

This project is licensed under the MIT License - see the LICENSE file for details.
