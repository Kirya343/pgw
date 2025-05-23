# Этап сборки
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем только pom.xml для скачивания зависимостей
COPY pom.xml .

# Скачиваем зависимости (кэшируем этот шаг)
RUN mvn dependency:go-offline

# Копируем остальные исходники
COPY src ./src

# Собираем jar-файл
RUN mvn clean package -DskipTests

# Этап запуска
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Копируем jar-файл из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Указываем порт, который слушает Spring Boot
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
