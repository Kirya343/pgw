package org.kirya343.main.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/register");  // Переадресация на страницу регистрации
        modelAndView.addObject("errorMessage", ex.getMessage());  // Добавление сообщения ошибки в модель
        return modelAndView;
    }
}