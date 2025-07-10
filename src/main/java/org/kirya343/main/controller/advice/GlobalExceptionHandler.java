package org.kirya343.main.controller.advice;

import org.kirya343.main.exceptions.UserNotRegisteredException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        ex.printStackTrace(); // Выведет полную ошибку в консоль
        return "redirect:/error"; // Любая твоя страница
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(Exception ex) {
        System.out.println("Не был найден какой-то ресурс");
        return null;
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/error");  // Переадресация на страницу регистрации
        modelAndView.addObject("errorMessage", ex.getMessage());  // Добавление сообщения ошибки в модель
        return modelAndView;
    }

    @ExceptionHandler(UserNotRegisteredException.class)
    public String handleUserNotRegistered(UserNotRegisteredException ex) {
        return "redirect:/register";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Файл слишком большой. Максимальный размер — 10MB.");
        return "redirect:/secure/account"; // или другая нужная страница
    }

    @ExceptionHandler(MultipartException.class)
    public String handleMultipartException(MultipartException e, RedirectAttributes redirectAttributes) {
        System.out.println("Ошибка Multipart: " + e);
        System.out.println(e.getMessage());
        e.printStackTrace(); 
        redirectAttributes.addFlashAttribute("error", "Ошибка при разборе загружаемых файлов.");
        return "redirect:/secure/error"; // или другая страница с ошибкой
    }
}