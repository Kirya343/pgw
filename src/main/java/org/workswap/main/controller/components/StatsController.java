package org.workswap.main.controller.components;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.workswap.datasource.stats.model.StatSnapshot;
import org.workswap.datasource.stats.model.StatSnapshot.IntervalType;
import org.workswap.datasource.stats.repository.StatsRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsRepository statsRepository;

    @GetMapping("/views")
    public ResponseEntity<List<Map<String, Object>>> getViewsStats(
            @RequestParam Long listingId,
            @RequestParam IntervalType interval,
            @RequestParam(required = false, defaultValue = "7") int days
    ) {
        LocalDateTime fromTime = LocalDateTime.now().minusDays(days);

        List<StatSnapshot> stats = statsRepository.findByListingIdAndIntervaTypeAndTimeAfter(
                listingId,
                interval,
                fromTime
        );

        List<Map<String, Object>> chartData = stats.stream()
                .sorted(Comparator.comparing(StatSnapshot::getTime))
                .map(s -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("x", s.getTime().toString());   // Или форматировать: s.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    point.put("y", s.getViews());             // Здесь можно вернуть и rating, и favorites — если нужно
                    return point;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(chartData);
    }
}