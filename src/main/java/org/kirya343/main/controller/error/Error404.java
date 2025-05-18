package org.kirya343.main.controller.error;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class Error404 implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // Получаем статус ошибки
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Для ошибки 404 возвращаем нашу кастомную страницу
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-pages/404-fallback"; // Путь к вашему HTML-файлу (без расширения)
            }
            
            // Здесь можно добавить обработку других ошибок (500, 403 и т.д.)
        }
        
        // По умолчанию возвращаем общую страницу ошибки
        return "error-pages/404-fallback";
    }

    public String getErrorPath() {
        return "/error";
    }
}
