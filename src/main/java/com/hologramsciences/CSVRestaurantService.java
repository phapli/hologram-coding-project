package com.hologramsciences;

import io.atlassian.fugue.Option;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVRestaurantService {
    private final List<Restaurant> restaurantList;

    /**
     * TODO: Implement Me
     * <p>
     * From the CSVRecord which represents a single line from src/main/resources/rest_hours.csv
     * Write a parser to read the line and create an instance of the Restaurant class (Optionally, using the Option class)
     * <p>
     * Example Line:
     * <p>
     * "Burger Bar","Mon,Tue,Wed,Thu,Sun|11:00-22:00;Fri,Sat|11:00-0:00"
     * <p>
     * '|'   separates the list of applicable days from the hours span
     * ';'   separates groups of (list of applicable days, hours span)
     * <p>
     * So the above line would be parsed as:
     * <p>
     * Map<DayOfWeek, OpenHours> m = new HashMap<>();
     * m.put(MONDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(TUESDAY,   new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(WEDNESDAY, new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(THURSDAY,  new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * m.put(SUNDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(22, 0)));
     * <p>
     * m.put(FRIDAY,    new OpenHours(LocalTime.of(11, 0), LocalTime.of(0, 0)));
     * m.put(SATURDAY,  new OpenHours(LocalTime.of(11, 0), LocalTime.of(0, 0)));
     * <p>
     * Option.some(new Restaurant("Burger Bar", m))
     * <p>
     * This method returns Option.some(parsedRestaurant),
     * IF the String name, and Map<DayOfWeek, OpenHours> openHours is found in the CSV,
     * - assume if both columns are in the CSV then they are both parsable.
     * AND if all values in openHours have !startTime.equals(endTime)
     * <p>
     * This method returns Option.none() when any of the OpenHours for a given restaurant have the same startTime and endDate
     * <p>
     * <p>
     * NOTE, the getDayOfWeek method should be helpful, and the LocalTime should be parsable by LocalDate.parse
     */
    public static Option<Restaurant> parse(final CSVRecord r) {
        if (r.size() != 2) {
            return Option.none();
        }
        String name = r.get(0);
        Map<DayOfWeek, Restaurant.OpenHours> openHoursMap = parseOpenHour(r.get(1));
        if (openHoursMap.isEmpty()) {
            return Option.none();
        }
        return Option.some(new Restaurant(name, openHoursMap));
    }

    /**
     * TODO: Implement me, This is a useful helper method
     */
    public static Map<DayOfWeek, Restaurant.OpenHours> parseOpenHour(final String openHoursString) {
        Map<DayOfWeek, Restaurant.OpenHours> openHoursMap = new HashMap<>();
        String scheduleString = openHoursString.trim();
        if (openHoursString.isEmpty()) {
            return openHoursMap;
        }
        String[] schedules = scheduleString.split(";");
        for (String schedule : schedules) {
            String[] dayHourPair = schedule.split("\\|");
            String dayString = dayHourPair[0].trim();
            String[] days = dayString.split(",");
            String hourString = dayHourPair[1].trim();
            String[] hourPair = hourString.split("-");
            LocalTime startTime = LocalTime.parse(hourPair[0].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(hourPair[1].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            if (startTime.equals(endTime)) {
                continue;
            }
            Restaurant.OpenHours openHours = new Restaurant.OpenHours(startTime, endTime);
            for (String day : days) {
                openHoursMap.put(getDayOfWeek(day.trim()).get(), openHours);
            }
        }
        return openHoursMap;
    }

    public CSVRestaurantService() throws IOException {
        this.restaurantList = ResourceLoader.parseOptionCSV("rest_hours.csv", CSVRestaurantService::parse);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantList;
    }

    /**
     * TODO: Implement me
     * <p>
     * A restaurant is considered open when the OpenHours for the dayOfWeek has:
     * <p>
     * startTime < localTime   && localTime < endTime
     * <p>
     * If the open hours are 16:00-20:00  Then
     * <p>
     * 15:59 open = false
     * 16:00 open = false
     * 16:01 open = true
     * 20:00 open = false
     * <p>
     * <p>
     * If the startTime endTime spans midnight, then consider an endTime up until 5:00 to be part of same DayOfWeek as the startTime
     * <p>
     * SATURDAY, OpenHours are: 20:00-04:00    SUNDAY, OpenHours are: 10:00-14:00
     * <p>
     * (SATURDAY, 03:00) => open = false
     * (SUNDAY, 03:00)   => open = true
     * (SUNDAY, 05:00)   => open = false
     */
    public List<Restaurant> getOpenRestaurants(final DayOfWeek dayOfWeek, final LocalTime localTime) {
        return restaurantList.stream()
                .filter(restaurant -> {
                    if (restaurant.getOpenHoursMap().containsKey(dayOfWeek)) {
                        Restaurant.OpenHours thisDayHours = restaurant.getOpenHoursMap().get(dayOfWeek);
                        if (thisDayHours.getStartTime().isBefore(thisDayHours.getEndTime())) {
                            // Case 10:00-14:00:
                            // - if localTime in middle -> true
                            // - if localTime after endTime -> false
                            // - if localTime before startTime -> check previous day
                            if (thisDayHours.getStartTime().isBefore(localTime)
                                    && thisDayHours.getEndTime().isAfter(localTime)) {
                                return true;
                            } else if (thisDayHours.getStartTime().isAfter(localTime)) {
                                Restaurant.OpenHours prevDayHours = restaurant.getOpenHoursMap().get(dayOfWeek.minus(1));
                                return prevDayHours != null
                                        && prevDayHours.getStartTime().isAfter(prevDayHours.getEndTime())
                                        && prevDayHours.getEndTime().isAfter(localTime);
                            } else {
                                return false;
                            }
                        } else {
                            // Case 20:00-04:00:
                            // - if localTime after endTime -> true
                            // - else -> check previous day
                            if (thisDayHours.getStartTime().isBefore(localTime)) {
                                return true;
                            } else {
                                Restaurant.OpenHours prevDayHours = restaurant.getOpenHoursMap().get(dayOfWeek.minus(1));
                                return prevDayHours != null
                                        && prevDayHours.getStartTime().isAfter(prevDayHours.getEndTime())
                                        && prevDayHours.getEndTime().isAfter(localTime);
                            }
                        }
                    } else {
                        // check previous day
                        Restaurant.OpenHours prevDayHours = restaurant.getOpenHoursMap().get(dayOfWeek.minus(1));
                        return prevDayHours != null
                                && prevDayHours.getStartTime().isAfter(prevDayHours.getEndTime())
                                && prevDayHours.getEndTime().isAfter(localTime);
                    }
                }).collect(Collectors.toList());
    }

    public List<Restaurant> getOpenRestaurantsForLocalDateTime(final LocalDateTime localDateTime) {
        return getOpenRestaurants(localDateTime.getDayOfWeek(), localDateTime.toLocalTime());
    }

    public static Option<DayOfWeek> getDayOfWeek(final String s) {

        if (s.equals("Mon")) {
            return Option.some(DayOfWeek.MONDAY);
        } else if (s.equals("Tue")) {
            return Option.some(DayOfWeek.TUESDAY);
        } else if (s.equals("Wed")) {
            return Option.some(DayOfWeek.WEDNESDAY);
        } else if (s.equals("Thu")) {
            return Option.some(DayOfWeek.THURSDAY);
        } else if (s.equals("Fri")) {
            return Option.some(DayOfWeek.FRIDAY);
        } else if (s.equals("Sat")) {
            return Option.some(DayOfWeek.SATURDAY);
        } else if (s.equals("Sun")) {
            return Option.some(DayOfWeek.SUNDAY);
        } else {
            return Option.none();
        }
    }

    public static <S, T> Function<S, Stream<T>> toStreamFunc(final Function<S, Option<T>> function) {
        return s -> function.apply(s).fold(() -> Stream.empty(), t -> Stream.of(t));
    }

    /**
     * NOTE: Useful for generating the data.sql file in src/main/resources/
     */
    public static void main(final String[] args) throws IOException {
        final CSVRestaurantService csvRestaurantService = new CSVRestaurantService();

        csvRestaurantService.getAllRestaurants().forEach(restaurant -> {

            final String name = restaurant.getName().replaceAll("'", "''");

            System.out.println("INSERT INTO restaurants (name) values ('" + name + "');");

            restaurant.getOpenHoursMap().entrySet().forEach(entry -> {
                final DayOfWeek dayOfWeek = entry.getKey();
                final LocalTime startTime = entry.getValue().getStartTime();
                final LocalTime endTime = entry.getValue().getEndTime();

                System.out.println("INSERT INTO open_hours (restaurant_id, day_of_week, start_time_minute_of_day, end_time_minute_of_day) select id, '" + dayOfWeek.toString() + "', " + startTime.get(ChronoField.MINUTE_OF_DAY) + ", " + endTime.get(ChronoField.MINUTE_OF_DAY) + " from restaurants where name = '" + name + "';");

            });
        });
    }
}
