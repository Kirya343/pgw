package org.workswap.main.controller.components;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.News;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.repository.ListingRepository;
import org.workswap.datasource.main.repository.NewsRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MiscController {

    private final ListingRepository listingRepository;
    private final NewsRepository newsRepository;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String getSitemap(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();

        List<Listing> listings = listingRepository.findListByActiveTrueAndTestModeFalse();

        List<User> profiles = listings.stream()
                                    .map(Listing::getAuthor)
                                    .collect(Collectors.toList());

        List<News> news = newsRepository.findByPublishedTrue();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        sb.append("<url>\n");
        sb.append("<loc>").append(baseUrl).append("/</loc>\n");
        sb.append("<changefreq>daily</changefreq>\n");
        sb.append("<priority>1.0</priority>\n");
        sb.append("</url>\n");

        // Инфо
        sb.append("<url>\n");
        sb.append("<loc>").append(baseUrl).append("/info").append("/</loc>\n");
        sb.append("<changefreq>daily</changefreq>\n");
        sb.append("<priority>1.0</priority>\n");
        sb.append("</url>\n");

        // Условия пользования
        sb.append("<url>\n");
        sb.append("<loc>").append(baseUrl).append("/privacy-policy").append("/</loc>\n");
        sb.append("<changefreq>monthly</changefreq>\n");
        sb.append("<priority>0.3</priority>\n");
        sb.append("</url>\n");

        // Политика конфиденциальности
        sb.append("<url>\n");
        sb.append("<loc>").append(baseUrl).append("/terms").append("/</loc>\n");
        sb.append("<changefreq>monthly</changefreq>\n");
        sb.append("<priority>0.3</priority>\n");
        sb.append("</url>\n");

        // Новости
        sb.append("<url>\n");
        sb.append("<loc>").append(baseUrl).append("/news").append("/</loc>\n");
        sb.append("<changefreq>daily</changefreq>\n");
        sb.append("<priority>0.8</priority>\n");
        sb.append("</url>\n");

        for (News newsItem : news) {
            sb.append("<url>\n");
            sb.append("<loc>").append(baseUrl).append("/news/")
              .append(newsItem.getId()).append("</loc>\n");
            sb.append("<changefreq>weekly</changefreq>\n");
            sb.append("<priority>0.8</priority>\n");
            sb.append("</url>\n");
        }

        for (Listing listing : listings) {
            sb.append("<url>\n");
            sb.append("<loc>").append(baseUrl).append("/listing/")
              .append(listing.getId()).append("</loc>\n");
            sb.append("<changefreq>weekly</changefreq>\n");
            sb.append("<priority>0.8</priority>\n");
            sb.append("</url>\n");
        }

        for (User profile : profiles) {
            sb.append("<url>\n");
            sb.append("<loc>").append(baseUrl).append("/profile/")
              .append(profile.getId()).append("</loc>\n");
            sb.append("<changefreq>weekly</changefreq>\n");
            sb.append("<priority>0.8</priority>\n");
            sb.append("</url>\n");
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    @GetMapping(value = "/robots.txt", produces = "text/plain")
    @ResponseBody
    public Resource getRobotsTxt() {
        return new FileSystemResource("config/robots.txt");
    }
}