# Etap 1: Budowanie aplikacji
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiowanie pom.xml i pobranie zaleznosci
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Kopiowanie kodu zrodlowego i budowanie paczki JAR
COPY src/ src/
RUN mvn clean package -DskipTests

# Etap 2: Obraz uruchomieniowy
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Folder na przesylane zdjecia
RUN mkdir -p uploads/products

# Skopiowanie zbudowanego JAR-a
COPY --from=build /app/target/*.jar app.jar

# Render dynamicznie przypisuje port przez zmienna PORT (np. 10000)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
