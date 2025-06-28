package org.kirya343.main.controller.advice;

import org.kirya343.main.exceptions.UserNotRegisteredException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

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
}