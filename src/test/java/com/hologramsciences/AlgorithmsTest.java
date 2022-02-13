package com.hologramsciences;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class AlgorithmsTest {

    @Test
    public void testCartesianProduct1() {
        final List<List<String>> listOfLists = Arrays.asList(
                Arrays.asList("A", "B"),
                Arrays.asList("K", "L")
        );

        assertEquals(
                Algorithms.cartesianProductForLists(listOfLists),

                Arrays.asList(
                        Arrays.asList("A", "K"),
                        Arrays.asList("A", "L"),
                        Arrays.asList("B", "K"),
                        Arrays.asList("B", "L")
                )
        );
    }

    @Test
    public void testCartesianProduct2() {
        final List<List<Integer>> listOfLists = Arrays.asList(
                Arrays.asList(1, 2),
                Arrays.asList(3, 4),
                Arrays.asList(5, 6, 7)
        );

        assertEquals(
                Algorithms.cartesianProductForLists(listOfLists),

                Arrays.asList(
                        Arrays.asList(1, 3, 5),
                        Arrays.asList(1, 3, 6),
                        Arrays.asList(1, 3, 7),

                        Arrays.asList(1, 4, 5),
                        Arrays.asList(1, 4, 6),
                        Arrays.asList(1, 4, 7),

                        Arrays.asList(2, 3, 5),
                        Arrays.asList(2, 3, 6),
                        Arrays.asList(2, 3, 7),

                        Arrays.asList(2, 4, 5),
                        Arrays.asList(2, 4, 6),
                        Arrays.asList(2, 4, 7)
                )
        );
    }


    @Test
    public void testWaysToMakeChange200Cents() {
        assertEquals(2728L, Algorithms.countNumWaysMakeChange(200));
        System.out.println(Algorithms.callCount);
    }

    @Test
    public void testWaysToMakeChange20000Cents() {
        assertEquals(4371565890901L, Algorithms.countNumWaysMakeChange(20000));
        System.out.println(Algorithms.callCount);
    }
}

