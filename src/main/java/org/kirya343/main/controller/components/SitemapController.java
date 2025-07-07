package org.kirya343.main.controller.components;

import java.util.List;
import java.util.stream.Collectors;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.News;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.ListingRepository;
import org.kirya343.main.repository.NewsRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SitemapController {

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
        sb.append("<changefreq>weekly</changefreq>\n");
        sb.append("<priority>1.0</priority>\n");
        sb.append("</url>\n");

        // Новости
        sb.append("<url>\n");
        sb.append("<loc>").append(baseUrl).append("/news").append("/</loc>\n");
        sb.append("<changefreq>daily</changefreq>\n");
        sb.append("<priority>1.0</priority>\n");
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
}