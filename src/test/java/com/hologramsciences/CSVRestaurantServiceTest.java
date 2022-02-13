package com.hologramsciences;

import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;

public class CSVRestaurantServiceTest {
    private static CSVRestaurantService csvRestaurantService;

    static {
        try {
            csvRestaurantService = new CSVRestaurantService();
        } catch (IOException e) {
            csvRestaurantService = null;
            e.printStackTrace();
        }
    }

    @Test
    public void getAllRestaurantsIsNotEmpty() {
        assertTrue(csvRestaurantService.getAllRestaurants().size() > 0);
    }

    @Test
    public void getOpenRestaurantsReturnsResults() {
        assertTrue(
                csvRestaurantService.getOpenRestaurants(DayOfWeek.MONDAY, LocalTime.NOON).size() > 0
        );
    }

    @Test
    public void getOpenRestaurantsReturnsResultsIncludesThai() {
        final List<Restaurant> openResults = csvRestaurantService.getOpenRestaurants(DayOfWeek.SATURDAY, LocalTime.of(0, 20));
        final List<String> restaurantNames = openResults.stream().map(Restaurant::getName).collect(Collectors.toList());
        assertTrue(restaurantNames.contains("Thai Stick Restaurant"));
    }

    @Test
    public void getOpenRestaurantsLateSaturdayIntoSunday() {
        final List<Restaurant> openResults = csvRestaurantService.getOpenRestaurants(DayOfWeek.SUNDAY, LocalTime.of(0, 20));
        final Set<String> restaurantNames = openResults.stream().map(Restaurant::getName).collect(Collectors.toSet());
        final Set<String> expectedNames = new HashSet<>(Arrays.asList("The Cheesecake Factory", "Sudachi", "Thai Stick Restaurant", "Sabella & La Torre", "Marrakech Moroccan Restaurant", "Tim's Nighttime Banana Stand"));
        assertTrue(restaurantNames.equals(expectedNames));
    }

    @Test
    public void getOpenRestaurantsForLocalDateTimeReturnsResults() {
        final List<Restaurant> lateNightResults = csvRestaurantService.getOpenRestaurantsForLocalDateTime(
                LocalDateTime.of(LocalDate.of(2020, 1, 15), LocalTime.MIDNIGHT)
        );

        final List<String> lateNightRestaurantNames = lateNightResults.stream().map(Restaurant::getName).collect(Collectors.toList());
        final List<String> expectedNames = Arrays.asList("Sudachi", "Thai Stick Restaurant", "Marrakech Moroccan Restaurant", "Tim's Nighttime Banana Stand");

        assertTrue((new HashSet<>(lateNightRestaurantNames)).containsAll(expectedNames));
    }

    @Test
    public void getOpenRestaurantsForLocalDateTimeReturnsResultsReallyLate() {
        final List<Restaurant> lateNightResults = csvRestaurantService.getOpenRestaurantsForLocalDateTime(
                LocalDateTime.of(LocalDate.of(2020, 1, 15), LocalTime.of(2, 0))
        );

        final List<String> lightNightRestaurantNames = lateNightResults.stream().map(Restaurant::getName).collect(Collectors.toList());
        final List<String> expectedNames = Arrays.asList("Tim's Nighttime Banana Stand");

        assertTrue((new HashSet<>(lightNightRestaurantNames)).containsAll(expectedNames));
    }


    @Test
    public void allRestaurantsHaveGoodOpenHours() {
        assertTrue(
                csvRestaurantService.getAllRestaurants().size() > 0 &&
                        csvRestaurantService.getAllRestaurants().stream().allMatch(r -> r.getOpenHoursMap().size() > 0
                                && r.getOpenHoursMap().values().stream().allMatch(oh -> !oh.getStartTime().equals(oh.getEndTime()))
                        )
        );
    }
}
