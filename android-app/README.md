# Meble Mobile

Natywna aplikacja Android dla backendu Spring znajdującego się w katalogu nadrzędnym.

## Funkcje

- logowanie przez JWT,
- zachowanie sesji w pamięci aplikacji,
- lista produktów i ich najważniejsze parametry, dodawanie, edycja i usuwanie ze zdjęciem,
- lista aktywnych i zrealizowanych zamówień, dodawanie, edycja i usuwanie,
- obsługa błędów sieci i wygaśnięcia sesji,
- jasny i ciemny motyw systemowy,
- **dynamiczny wybór adresu serwera backendu (Emulator, Lokalny PC / Wi-Fi IP, Render Cloud)** bez konieczności przebudowywania aplikacji.

## Wersje aplikacji (Build Variants)

1. **`debug` (Wersja DEV):**
   - Nazwa aplikacji: `Meble Dev`
   - Pakiet: `produkcja.meble.mobile.dev` (może być zainstalowana obok wersji produkcyjnej!)
   - Domyślny adres backendu: `http://10.0.2.2:8080/` (dla Emulatora Android)
   - Na ekranie logowania możesz jednym kliknięciem zmienić adres na IP komputera (dla fizycznego telefonu) lub chmurę.

2. **`release` (Wersja Produkcyjna):**
   - Nazwa aplikacji: `Meble`
   - Pakiet: `produkcja.meble.mobile`
   - Domyślny adres backendu: `https://storeapp-xf39.onrender.com/`

---

## Uruchomienie lokalnego środowiska deweloperskiego

### 1. Uruchomienie backendu w profilu `dev`

Backend automatycznie używa lokalnej bazy PostgreSQL (`localhost:5432/mebledb`) oraz predefiniowanego klucza JWT dla środowiska dev:

```powershell
# Będąc w katalogu głównym projektu:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

> **Wskazówka:** Jeśli Twoja lokalna baza danych PostgreSQL używa innych danych, możesz je przekazać zmiennymi środowiskowymi:
> - `DEV_DB_URL` (domyślnie: `jdbc:postgresql://localhost:5432/mebledb`)
> - `DEV_DB_USERNAME` (domyślnie: `postgres`)
> - `DEV_DB_PASSWORD` (domyślnie: `postgres`)

Profil `dev` backendu tworzy automatycznie konta testowe:
- `admin` / `admin1234`
- `user` / `user12345`

### 2. Uruchomienie aplikacji Android

#### Na Emulatorze Android Studio:
1. Otwórz folder `android-app` w Android Studio.
2. Wybierz wariant `debug` w oknie **Build Variants**.
3. Uruchom aplikację na emulatorze – połączy się z lokalnym backendem przez `http://10.0.2.2:8080/`.

#### Na fizycznym telefonie:
1. Podłącz telefon i komputer do tej samej sieci Wi-Fi.
2. Uruchom aplikację (lub zainstaluj plik APK).
3. Na ekranie logowania kliknij **„Zmień”** przy sekcji *Serwer API backendu* i wpisz lokalny adres IP Twojego komputera, np. `http://192.168.1.50:8080/`.
4. Upewnij się, że zapora sieciowa (Windows Firewall) zezwala na połączenia przychodzące na port `8080`.

---

## Budowanie z terminala

```powershell
# Będąc w katalogu android-app:
.\gradlew.bat assembleDebug
```

Gotowy plik APK znajdzie się w: `app/build/outputs/apk/debug/app-debug.apk`.
