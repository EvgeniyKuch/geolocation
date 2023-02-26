package ru.otus.geolocation.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.geolocation.domain.CityRule;
import ru.otus.geolocation.dto.AnswerShop;
import ru.otus.geolocation.service.QueryService;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class GeoController {

    private final QueryService queryService;

    /**
     * Магазины по транскрипции города.
     * enum AnswerStatus {
     * ZERO, // нет точек продаж в насел. пункте, пустой список
     * ONLY, // только одна точка продаж, единственный магазин в списке
     * MANY // больше 1 точки продажи, пустой список
     * }
     */
    @GetMapping("/api/shops-by-city-utterance")
    public AnswerShop getShopsByCityUtterance(@RequestParam("utterance") String utterance) {
        return queryService.getShopsByCityUtterance(utterance);
    }

    /**
     * Ближайшие магазины к адресу utterance в городе rule.
     * enum AnswerStatus {
     * ZERO, // нет точек продаж в насел. пункте, пустой список
     * MANY // больше 1 точки продажи, в сиске ближайшие магазины к адресу utterance в городе rule.
     * }
     */
    @GetMapping("/api/shops-by-rule-and-utterance")
    public AnswerShop getShopsByRuleAndUtterance(@RequestParam("rule") String rule, @RequestParam("utterance") String utterance) {
        return queryService.getByRuleAndAddress(rule, utterance);
    }

    /**
     * Обновить регулярки в кэше
     * @return массив CityRule в отсортированном по убыванию длины строки названия города состоянии
     */
    @PostMapping("/api/update-cities-rules")
    public List<CityRule> updateCitiesRules() {
        return queryService.updateCityRules();
    }

}
