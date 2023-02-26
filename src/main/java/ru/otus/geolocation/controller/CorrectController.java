package ru.otus.geolocation.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.geolocation.service.CorrectService;

/**
 * Проверка корректности базы.
 */
@Controller
@AllArgsConstructor
public class CorrectController {

    private final CorrectService correctService;

    /**
     * Посмотреть дубликаты по координатам
     */
    @GetMapping("api/duplicate")
    public String getDuplicates(Model model) {
        model.addAttribute("duplicates", correctService.findDuplicates());
        return "listDuplicate";
    }

    /**
     * Посмотреть неопределившиеся координаты (широта или долгота меньше 0)
     */
    @GetMapping("api/incorrect")
    public String getIncorrect(Model model) {
        model.addAttribute("incorrects", correctService.findIncorrect());
        return "listIncorrect";
    }

    /**
     * Посмотреть все точки продаж
     */
    @GetMapping("api/all")
    public String getAll(Model model) {
        model.addAttribute("shopPoints", correctService.findAll());
        return "listAll";
    }

    /**
     * Посмотреть города, которые есть в ShopPoint, но нет в CityRule.
     * Чтобы знать, для каких городов добавить регулярки.
     */
    @GetMapping("api/get-new-cities")
    public String getNewCitiesForRegexp(Model model) {
        model.addAttribute("cities", correctService.getNewCitiesForRegexp());
        return "listNewCitiesForRegexp";
    }
}
