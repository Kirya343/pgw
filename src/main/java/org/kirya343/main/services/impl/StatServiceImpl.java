package org.kirya343.main.services.impl;

import lombok.RequiredArgsConstructor;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.ListingRepository;
import org.kirya343.main.repository.ResumeRepository;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.components.StatService;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ResumeRepository resumeRepository;

    @Override
    public int getTotalViews(User user) {
        return user.getListings().stream()
                .mapToInt(Listing::getViews)
                .sum();
    }

    @Override
    public int getTotalResponses(User user) {
        // Заглушка — можно заменить логикой по сообщениям или откликам
        return 0;
    }

    @Override
    public int getCompletedDeals(User user) {
        // Заглушка — если нет поля "сделка завершена", можно симулировать
        return 0;
    }

    @Override
    public double getAverageRating(User user) {
        List<Listing> listings = user.getListings();
        if (listings.isEmpty()) return 0.0;
        return listings.stream()
                .mapToDouble(Listing::getRating)
                .average()
                .orElse(0.0);
    }
    @Override
    public int getMonthlyViews(User user) {
        // Получаем текущую дату
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Считаем просмотры только за текущий месяц
        return user.getListings().stream()
                .filter(listing -> {
                    // Проверяем, что дата создания объявления соответствует текущему месяцу и году
                    LocalDate createdAt = listing.getCreatedAt().toLocalDate();
                    return createdAt.getMonthValue() == currentMonth && createdAt.getYear() == currentYear;
                })
                .mapToInt(Listing::getViews)
                .sum();
    }
    @Override
    public int getMonthlyResponses(User user) {
        // Получаем текущую дату
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Заглушка — логика подсчета откликов для текущего месяца (например, можно использовать сообщения или другие сущности)
        return user.getListings().stream()
                .filter(listing -> {
                    // Проверяем, что дата создания объявления соответствует текущему месяцу и году
                    LocalDate createdAt = listing.getCreatedAt().toLocalDate();
                    return createdAt.getMonthValue() == currentMonth && createdAt.getYear() == currentYear;
                })
                .mapToInt(listing -> 1) // Условие — тут может быть другая логика для откликов
                .sum();
    }
    @Override
    public int getMonthlyDeals(User user) {
        // Получаем текущую дату
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Заглушка — проверка завершенных сделок для текущего месяца
        return user.getListings().stream()
                .filter(listing -> {
                    // Проверяем, что дата создания объявления соответствует текущему месяцу и году
                    LocalDate createdAt = listing.getCreatedAt().toLocalDate();
                    return createdAt.getMonthValue() == currentMonth && createdAt.getYear() == currentYear;
                })
                .mapToInt(listing -> listing.getRating() > 4.0 ? 1 : 0) // Пример: если рейтинг больше 4, считаем сделку завершенной
                .sum();
    }

    public Map<String, Object> getSiteStats(Locale locale) {
        Map<String, Object> stats = new HashMap<>();

        // Получаем реальные данные из репозиториев
        long usersCount = userRepository.count();
        long resumesCount = resumeRepository.countByPublishedTrue();
        long activeListingsCount = listingRepository.findByActiveTrue().size();
        long totalViews = listingRepository.findAll().stream()
                .mapToInt(Listing::getViews)
                .sum();

        // Форматируем числа в зависимости от локали
        NumberFormat numberFormat = NumberFormat.getInstance(locale);

        stats.put("usersCount", numberFormat.format(usersCount));
        stats.put("listingsCount", numberFormat.format(activeListingsCount));
        stats.put("viewsCount", numberFormat.format(totalViews));
        stats.put("resumesCount", numberFormat.format(resumesCount));

        // Пока используем фиктивные данные для сделок
        stats.put("dealsCount", "2,000+");

        return stats;
    }
}
