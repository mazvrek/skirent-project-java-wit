# SkiRent – System Wypożyczalni Nart

**SkiRent** to desktopowa aplikacja do zarządzania wypożyczalnią nart, napisana w języku Java z interfejsem graficznym opartym na Swing. System umożliwia kompleksową obsługę wypożyczalni: zarządzanie sprzętem narciarskim, klientami oraz procesem wypożyczeń i zwrotów. Dane są przechowywane lokalnie w binarnym pliku `.dat`, a interfejs obsługuje dwa języki: polski i angielski.

---

## Wymagania

| Zależność | Minimalna wersja |
|-----------|-----------------|
| Java JDK | 17+ |
| Maven | 3.6+ |

---

## Uruchomienie

### Budowanie projektu

```bash
mvn package
```

### Uruchomienie aplikacji

```bash
java -jar target/skirent-1.0-SNAPSHOT.jar
```

### Uruchomienie testów

```bash
mvn test
```

---

## Struktura projektu

```
skirent/
├── pom.xml # Konfiguracja Maven
└── src/
├── main/
│ ├── java/pl/skirent/
│ │ ├── model/ # Encje danych
│ │ │ ├── SkiType.java # Typ narty
│ │ │ ├── Ski.java # Narty
│ │ │ ├── Customer.java # Klient
│ │ │ └── Rental.java # Wypożyczenie
│ │ ├── config/
│ │ │ └── AppConfig.java # Stałe konfiguracyjne
│ │ ├── repository/ # Warstwa dostępu do danych
│ │ │ ├── Repository.java # Generyczny interfejs CRUD
│ │ │ ├── SkiTypeRepository.java
│ │ │ ├── SkiRepository.java
│ │ │ ├── CustomerRepository.java
│ │ │ └── RentalRepository.java
│ │ ├── service/ # Logika biznesowa
│ │ │ ├── RentalService.java # Obsługa wypożyczeń
│ │ │ └── ReportService.java # Generowanie raportów
│ │ ├── io/
│ │ │ └── FileIOService.java # Binarny zapis/odczyt danych
│ │ ├── i18n/
│ │ │ └── I18n.java # Obsługa internacjonalizacji
│ │ └── ui/ # Interfejs graficzny (Swing)
│ │ ├── MainFrame.java # Główne okno aplikacji
│ │ ├── SkiTypePanel.java # Panel typów nart
│ │ ├── SkiPanel.java # Panel nart
│ │ ├── CustomerPanel.java # Panel klientów
│ │ ├── RentalPanel.java # Panel wypożyczeń
│ │ └── ReportPanel.java # Panel raportów
│ └── resources/
│ ├── messages_pl.properties # Tłumaczenia PL
│ └── messages_en.properties # Tłumaczenia EN
└── test/
└── java/pl/skirent/test/ # Testy JUnit 5
├── SkiTypeRepositoryTest.java
├── RentalServiceTest.java
├── ReportServiceTest.java
└── FileIOServiceTest.java
```

---

## Architektura

Aplikacja zbudowana jest w oparciu o warstwową architekturę:

### Model
Encje danych reprezentujące domenę biznesową. Wypożyczenie (`Rental`) obsługuje statusy:

| Status | Opis |
|------------|-------------------------------------------|
| `ACTIVE` | Narty aktualnie wypożyczone |
| `RETURNED` | Narty zwrócone w terminie |
| `LATE` | Narty zwrócone lub przetrzymane po terminie |

### Repository
Warstwa dostępu do danych oparta na wzorcu repozytorium. Generyczny interfejs `Repository<T>` definiuje standardowe operacje CRUD, a implementacje (np. `SkiRepository`) przechowują dane **in-memory** w kolekcjach Java.

```java
public interface Repository<T> {
void add(T entity);
Optional<T> findById(String id);
List<T> findAll();
void update(T entity);
void delete(String id);
}
```

### Service
Warstwa logiki biznesowej:
- **`RentalService`** – tworzenie wypożyczeń z walidacją kolizji dat (sprawdzanie, czy narty są dostępne w podanym przedziale czasowym), obsługa zwrotów (zmiana statusu, dodawanie uwag) oraz automatyczne wykrywanie wypożyczeń przeterminowanych.
- **`ReportService`** – generowanie raportów: narty dostępne, aktualnie wypożyczone, przetrzymane po terminie.

### IO
`FileIOService` realizuje trwałe przechowywanie danych:
- Zapis i odczyt binarny przez `DataOutputStream` / `DataInputStream`
- Operacje wykonywane **asynchronicznie** w dedykowanym wątku z puli (`ExecutorService`) skonfigurowanej przez `AppConfig`

### UI
Interfejs graficzny zbudowany przy użyciu **Java Swing**:
- `MainFrame` – główne okno z menu i zakładkami (`JTabbedPane`)
- Każdy panel (`SkiTypePanel`, `SkiPanel`, `CustomerPanel`, `RentalPanel`, `ReportPanel`) odpowiada jednej domenie
- Dynamiczna zmiana języka PL ↔ EN realizowana przez klasę `I18n` wykorzystującą `ResourceBundle` – bez konieczności restartu aplikacji

---

## Funkcjonalności

- **Zarządzanie typami nart** – dodawanie, edycja, usuwanie typów (np. zjazdowe, biegowe, skiturowe)
- **Zarządzanie nartami** – CRUD dla sprzętu narciarskiego (marka, model, wiązania, długość)
- **Zarządzanie klientami** – rejestracja klientów z numerem dokumentu
- **Wypożyczenia** – tworzenie z walidacją dostępności nart w zadanym przedziale dat, usuwanie rezerwacji
- **Zwroty** – zmiana statusu wypożyczenia z możliwością dodania uwag
- **Raporty** – narty dostępne / aktualnie wypożyczone / przeterminowane
- **Trwały zapis danych** – binarny plik `skirent_data.dat` (zapis i odczyt przy starcie/zamknięciu aplikacji)
- **Dwujęzyczny interfejs** – polska i angielska wersja językowa, przełączana z poziomu menu

---

## Model danych

### SkiType – Typ narty

| Pole | Typ | Opis |
|-------------|--------|-------------------------------|
| `id` | String | Unikalny identyfikator (UUID) |
| `name` | String | Nazwa typu (np. Zjazdowe) |
| `description` | String | Opis typu |

### Ski – Narty

| Pole | Typ | Opis |
|-------------|--------|---------------------------------------|
| `id` | String | Unikalny identyfikator (UUID) |
| `skiTypeId` | String | Referencja do typu narty |
| `brand` | String | Marka (np. Rossignol, Salomon) |
| `model` | String | Model |
| `bindings` | String | Typ wiązań |
| `length` | double | Długość nart w centymetrach |

### Customer – Klient

| Pole | Typ | Opis |
|------------------|--------|-------------------------------|
| `id` | String | Unikalny identyfikator (UUID) |
| `firstName` | String | Imię |
| `lastName` | String | Nazwisko |
| `documentNumber` | String | Numer dokumentu tożsamości |
| `description` | String | Dodatkowe uwagi |

### Rental – Wypożyczenie

| Pole | Typ | Opis |
|--------------|--------------|-----------------------------------------------|
| `id` | String | Unikalny identyfikator (UUID) |
| `customerId` | String | Referencja do klienta |
| `skiIds` | List\<String\> | Lista identyfikatorów wypożyczonych nart |
| `from` | LocalDate | Data rozpoczęcia wypożyczenia |
| `to` | LocalDate | Data planowanego zwrotu |
| `status` | RentalStatus | Status: `ACTIVE` / `RETURNED` / `LATE` |
| `remarks` | String | Uwagi (np. przy zwrocie) |

---

## Testy

Projekt zawiera **21 testów JUnit 5**, wszystkie zaliczone .

| Klasa testowa | Zakres testów |
|---------------------------|-------------------------------------------------------------------------------|
| `SkiTypeRepositoryTest` | Operacje CRUD repozytorium: dodawanie, wyszukiwanie po ID, aktualizacja, usuwanie, obsługa braku elementu |
| `RentalServiceTest` | Tworzenie wypożyczeń, walidacja kolizji dat (te same narty w nakładającym się terminie), zwroty, zmiana statusu, wykrywanie przeterminowanych |
| `ReportServiceTest` | Poprawność raportów: lista nart dostępnych, aktualnie wypożyczonych, przetrzymanych po terminie |
| `FileIOServiceTest` | Zapis stanu aplikacji do pliku binarnego i jego odczyt – weryfikacja integralności danych po cyklu zapis/odczyt |

### Uruchomienie testów

```bash
mvn test
```

Wyniki w formacie XML dostępne w `target/surefire-reports/`.

---

## Konfiguracja (AppConfig)

Stałe konfiguracyjne zdefiniowane w klasie `AppConfig`:

| Parametr | Wartość | Opis |
|---------------------|----------------------|-----------------------------------------------|
| `THREAD_POOL_SIZE` | `2` | Liczba wątków w puli dla operacji IO |
| `DATA_FILE` | `skirent_data.dat` | Nazwa binarnego pliku danych |
| `DEFAULT_LANGUAGE` | `pl` | Domyślny język interfejsu (polski) |

```java
public class AppConfig {
public static final int THREAD_POOL_SIZE = 2;
public static final String DATA_FILE = "skirent_data.dat";
public static final String DEFAULT_LANGUAGE = "pl";
}
```

---

## Format pliku danych

Dane zapisywane są binarnie przez `DataOutputStream` w pliku `skirent_data.dat`. Plik składa się z czterech sekcji w stałej kolejności:

```
[ SkiTypes ] → [ Skis ] → [ Customers ] → [ Rentals ]
```

Każda sekcja:
1. `writeInt(count)` – liczba rekordów w sekcji
2. Dla każdego rekordu – pola w określonej kolejności przez:
- `writeUTF(String)` – pola tekstowe (id, name, description, …)
- `writeDouble(double)` – pola liczbowe zmiennoprzecinkowe (np. length)
- `writeInt(int)` – pola liczbowe całkowite (np. liczba elementów listy)

Przykład odczytu wypożyczenia:

```java
String id = dis.readUTF();
String customerId = dis.readUTF();
int skiCount = dis.readInt();
List<String> skiIds = new ArrayList<>();
for (int i = 0; i < skiCount; i++) {
skiIds.add(dis.readUTF());
}
String from = dis.readUTF(); // LocalDate.toString()
String to = dis.readUTF();
String status = dis.readUTF(); // RentalStatus.name()
String remarks = dis.readUTF();
```

---

## Technologie

| Technologia | Wersja | Zastosowanie |
|-------------|--------|--------------------------------------|
| Java | 17 | Język implementacji |
| Swing | – | Interfejs graficzny (GUI) |
| Maven | 3.6+ | Budowanie projektu, zarządzanie zależnościami |
| JUnit 5 | 5.x | Testy jednostkowe |
| DataOutputStream / DataInputStream | Java SE | Binarny zapis/odczyt danych |

---
*SkiRent – System Wypożyczalni Nart | Java 17 + Swing + Maven*