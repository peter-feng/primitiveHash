package com.example;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.NoSuchElementException;

public class PrimitiveHashMapTest {

    @Test
    public void testBasicOperations() {
        try (PrimitiveHashMap map = new PrimitiveHashMap()) {
            // Test put and get
            map.put(1L, 100L);
            map.put(2L, 200L);
            map.put(3L, 300L);

            assertEquals(100L, map.get(1L));
            assertEquals(200L, map.get(2L));
            assertEquals(300L, map.get(3L));
            assertEquals(3, map.size());

            // Test containsKey
            assertTrue(map.containsKey(1L));
            assertFalse(map.containsKey(4L));

            // Test remove
            map.remove(2L);
            assertEquals(2, map.size());
            assertFalse(map.containsKey(2L));

            // Test clear
            map.clear();
            assertEquals(0, map.size());
            assertFalse(map.containsKey(1L));
        }
    }

    @Test
    public void testResize() {
        try (PrimitiveHashMap map = new PrimitiveHashMap(4)) {
            // Fill the map beyond its initial capacity
            for (long i = 1; i <= 100; i++) {
                map.put(i, i * 10);
            }

            // Verify all entries after resize
            for (long i = 1; i <= 100; i++) {
                assertEquals(i * 10, map.get(i));
            }
            assertEquals(100, map.size());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKeyNotAllowed() {
        try (PrimitiveHashMap map = new PrimitiveHashMap()) {
            map.put(0L, 100L);  // Should throw exception as 0 is the default empty marker
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNonExistentKey() {
        try (PrimitiveHashMap map = new PrimitiveHashMap()) {
            map.get(999L);  // Should throw exception
        }
    }

    @Test
    public void testHighLoadAndCollisions() {
        try (PrimitiveHashMap map = new PrimitiveHashMap(16)) {  // Start with small capacity
            // Insert values that will definitely cause collisions
            // Using multiples of 16 plus 1 to avoid empty marker (0)
            int numEntries = 1000;
            
            // First verify we can insert all entries
            for (long i = 0; i < numEntries; i++) {
                map.put(i * 16 + 1, i);  // Keys that will collide but avoid 0
            }
            assertEquals("Initial size should match insertions", numEntries, map.size());

            // Verify all entries are accessible and have correct values
            for (long i = 0; i < numEntries; i++) {
                assertTrue("Key should exist: " + (i * 16 + 1), map.containsKey(i * 16 + 1));
                assertEquals("Value should match for key: " + (i * 16 + 1), i, map.get(i * 16 + 1));
            }

            // Remove even numbered entries
            int expectedRemaining = numEntries;
            for (long i = 0; i < numEntries; i += 2) {
                map.remove(i * 16 + 1);
                expectedRemaining--;
                assertEquals("Size should match after removing " + i, expectedRemaining, map.size());
            }

            // Verify odd numbered entries are still present with correct values
            for (long i = 1; i < numEntries; i += 2) {
                assertTrue("Key should exist after removals: " + (i * 16 + 1), map.containsKey(i * 16 + 1));
                assertEquals("Value should match after removals: " + (i * 16 + 1), i, map.get(i * 16 + 1));
            }

            // Verify even numbered entries are gone
            for (long i = 0; i < numEntries; i += 2) {
                assertFalse("Key should not exist after removal: " + (i * 16 + 1), map.containsKey(i * 16 + 1));
            }

            assertEquals("Final size should be half of original", numEntries / 2, map.size());
        }
    }
}
