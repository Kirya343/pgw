package org.kirya343.main.model;

public class ModelsSettings {

    // Параметры моделей
    public enum SearchParamType { 
        // Общие параметры
        ID,
        NAME,

        ALL, // Найти все элементы

        // User
        EMAIL,
        SUB,
        OAUTH2,
    }
}
