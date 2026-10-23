# Meble Mobile

Natywna aplikacja Android dla backendu Spring znajdującego się w katalogu nadrzędnym.

## Funkcje

- logowanie i rejestracja przez JWT,
- zachowanie sesji w pamięci aplikacji,
- lista produktów i ich najważniejsze parametry,
- lista aktywnych zamówień,
- szczegóły zamówienia,
- obsługa błędów sieci i wygaśnięcia sesji,
- jasny i ciemny motyw systemowy.

## Uruchomienie

1. Uruchom PostgreSQL i backend z profilem `dev`:

   ```powershell
   ..\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

2. Otwórz folder `android-app` w Android Studio.
3. Uruchom aplikację na emulatorze.

Emulator Android łączy się z hostem przez `http://10.0.2.2:8080/`. Adres jest ustawiony
jako `API_BASE_URL` w `app/build.gradle.kts`. Dla fizycznego telefonu ustaw w tym miejscu
adres IP komputera w sieci lokalnej i zezwól zaporze na połączenia do portu 8080.

Profil `dev` backendu tworzy konta testowe:

- `admin` / `admin1234`
- `user` / `user12345`

## Budowanie z terminala

```powershell
.\gradlew.bat assembleDebug
```

Gotowy plik APK znajdzie się w `app/build/outputs/apk/debug/`.
