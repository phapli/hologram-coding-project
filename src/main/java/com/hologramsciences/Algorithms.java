package com.hologramsciences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Algorithms {
    /**
     * Compute the cartesian product of a list of lists of any type T
     * the result is a list of lists of type T, where each element comes
     * each successive element of the each list.
     * <p>
     * https://en.wikipedia.org/wiki/Cartesian_product
     * <p>
     * For this problem order matters.
     * <p>
     * Example:
     * <p>
     * listOfLists = Arrays.asList(
     * Arrays.asList("A", "B"),
     * Arrays.asList("K", "L")
     * )
     * <p>
     * returns:
     * <p>
     * Arrays.asList(
     * Arrays.asList("A", "K"),
     * Arrays.asList("A", "L"),
     * Arrays.asList("B", "K"),
     * Arrays.asList("B", "L")
     * )
     */
    public static final <T> List<List<T>> cartesianProductForLists(final List<List<T>> listOfLists) {
        List<List<T>> result = new ArrayList<>();
        pickNextEle(listOfLists, 0, result, null);
        return result;
    }

    private static <T> void pickNextEle(final List<List<T>> listOfLists, int curListIndex, List<List<T>> products, List<T> tempProduct) {
        if (curListIndex >= listOfLists.size()) {
            if (tempProduct != null) products.add(tempProduct);
            return;
        }
        for (T t : listOfLists.get(curListIndex)) {
            List<T> newProducts = tempProduct != null ? new ArrayList<>(tempProduct) : new ArrayList<>();
            newProducts.add(t);
            pickNextEle(listOfLists, curListIndex + 1, products, newProducts);
        }
    }

    private static final int[] COIN_DENOMINATIONS = new int[]{100, 50, 25, 10, 5, 1};

    /**
     * In the United States there are six coins:
     * 1¢ 5¢ 10¢ 25¢ 50¢ 100¢
     * Assuming you have an unlimited supply of each coin,
     * implement a method which returns the number of distinct ways to make totalCents
     */
    public static final long countNumWaysMakeChange(final int totalCents) {
        // TODO Implement me

        Map<String, Long> cache = new HashMap<>();
        return pickNextDenomination(totalCents, 0, cache);
    }

    public static int callCount = 0;

    private static long pickNextDenomination(int remainCents, int denominationIndex, Map<String, Long> cache) {
        callCount++;
        if (remainCents == 0) {
            return 1;
        }
        if (denominationIndex == COIN_DENOMINATIONS.length - 1) {
            if (remainCents % COIN_DENOMINATIONS[denominationIndex] == 0)
                return 1;
            else
                return 0;
        }

        String key = String.format("%s-%s", remainCents, denominationIndex);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        int maxCount = remainCents / COIN_DENOMINATIONS[denominationIndex];
        long ways = 0;
        for (int i = 0; i <= maxCount; i++) {
            ways += pickNextDenomination(remainCents - i * COIN_DENOMINATIONS[denominationIndex],
                    denominationIndex + 1, cache);
        }
        cache.put(key, ways);
        return ways;
    }
}
