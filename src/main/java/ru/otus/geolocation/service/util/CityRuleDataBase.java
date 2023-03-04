package ru.otus.geolocation.service.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.otus.geolocation.domain.CityRule;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CityRuleDataBase {

    /**
     * Хранит CityRule в отсортированном по убыванию длины строки названия города состоянии во избежания конфликтов при
     * распознавании городов с одинаковой подстрокой (например, Орск-Магнитогорск). Для добавления регулярок в этот кэш
     * добавить в таблицу city_rule регулярки и выполнить POST запрос с пустым телом на "/api/update-cities-rules".
     * Дублей по полю rule быть не должно.
     */
    private final List<CityRule> cityRuleList;

    private final Map<String, String> ruleCityMap;

}
