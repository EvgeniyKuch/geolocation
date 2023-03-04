package ru.otus.geolocation.service.util;

import org.springframework.stereotype.Service;
import ru.otus.geolocation.dto.ShopPointDTO;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитарные методы для парсинга.
 *
 * @author Evgeny Kuchumov
 * @version 0.1
 * @since
 */
@Service
public class WorktimeParser {

    private final Pattern week = Pattern.compile("((\\d{1,2}(:\\d{1,2})?-\\d{1,2}(:\\d{1,2})?|-),){6}(\\d{1,2}(:\\d{1,2})?-\\d{1,2}(:\\d{1,2})?|-)");

    private final Pattern day = Pattern.compile("((\\d{1,2})(:(\\d{1,2}))?-(\\d{1,2})(:(\\d{1,2}))?|-)");

    public void parseWorkTime(ShopPointDTO shopPointDTO, String workTimeRaw) {
        if (workTimeRaw.isEmpty()) {
            return;
        }
        workTimeRaw = clearWorkTime(workTimeRaw);
        if (week.matcher(workTimeRaw).matches()) {
            String[] workTimeWeek = workTimeRaw.split(",");
            if (workTimeWeek.length == 7) {
                String workTimeToday = workTimeWeek[LocalDate.now().getDayOfWeek().getValue() - 1];
                Matcher m = day.matcher(workTimeToday);
                if (m.matches()) {
                    if ("-".equals(workTimeToday)) {
                        shopPointDTO.setHaveCorrectWorkTime(true);
                        shopPointDTO.setHoliday(true);
                    } else {
                        int hourStart = Integer.parseInt(m.group(2));
                        int minuteStart = m.group(3) != null ? Integer.parseInt(m.group(4)) : 0;
                        int hourEnd = Integer.parseInt(m.group(5));
                        int minuteEnd = m.group(6) != null ? Integer.parseInt(m.group(7)) : 0;
                        hourEnd = hourEnd == 0 && minuteEnd == 0 ? 24 : hourEnd;
                        if (validTime(hourStart, minuteStart, hourEnd, minuteEnd)) {
                            shopPointDTO.setHaveCorrectWorkTime(true);
                            shopPointDTO.setHourStart(hourStart);
                            shopPointDTO.setMinuteStart(minuteStart);
                            shopPointDTO.setHourEnd(hourEnd);
                            shopPointDTO.setMinuteEnd(minuteEnd);
                        }
                    }
                }
            }
        }
    }

    public boolean correctWorkTime(String workTimeRaw) {
        workTimeRaw = clearWorkTime(workTimeRaw);
        if (week.matcher(workTimeRaw).matches()) {
            String[] workTimeWeek = workTimeRaw.split(",");
            if (workTimeWeek.length == 7) {
                int countCorrect = 0;
                for (String currentDay : workTimeWeek) {
                    Matcher m = day.matcher(currentDay);
                    if (m.matches()) {
                        if ("-".equals(currentDay)) {
                            countCorrect++;
                        } else {
                            int hourStart = Integer.parseInt(m.group(2));
                            int minuteStart = m.group(3) != null ? Integer.parseInt(m.group(4)) : 0;
                            int hourEnd = Integer.parseInt(m.group(5));
                            int minuteEnd = m.group(6) != null ? Integer.parseInt(m.group(7)) : 0;
                            hourEnd = hourEnd == 0 && minuteEnd == 0 ? 24 : hourEnd;
                            if (validTime(hourStart, minuteStart, hourEnd, minuteEnd)) {
                                countCorrect++;
                            }
                        }
                    }
                }
                return countCorrect == 7;
            }
        }
        return false;
    }

    private String clearWorkTime(String workTimeRaw) {
        return workTimeRaw.replaceAll("[\\u00A0\\s]", "") // пробелы, переносы строк, табуляции, неразрывные пробелы
                .toLowerCase()
                .replace("–", "-") // короткое тире меняем на минус
                .replace("—", "-") // длинное тире меняем на минус
                .trim();
    }

    private boolean validTime(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        return hourStart >= 0 && hourStart <= 23 && minuteStart >= 0 && minuteStart <= 59
                && hourEnd >= 0 && hourEnd <= 24 && minuteEnd >= 0 && minuteEnd <= 59;
    }
}
