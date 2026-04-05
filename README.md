# Prison Break: Gulag — Minecraft Forge 1.12.2 Mod

Mod survivalowy osadzony w fikcyjnym sowieckim łagrze na syberyjskiej wyspie. Twoim celem jest ucieczka przy pomocy 70 NPC sterowanych przez AI (DeepSeek).

---

## Jak to działa — skrót

Wchodzisz do świata i jesteś więźniem. Masz przy sobie nic. Dookoła 20 uzbrojonych strażników i 50 innych więźniów — wszyscy mówią, myślą i reagują na bieżąco dzięki DeepSeek API. Musisz zdobyć zaufanie właściwych ludzi, zebrać materiały, ukryć narzędzia przed strażnikami i zaplanować ucieczkę. Potem przeżyć arktyczną wyspę zanim dogonią cię helikopterem i psem tropiącym.

---

## Co zostało stworzone (97 plików Java + 93 zasoby)

### AI — pakiet `ai/`

| Plik | Co robi |
|------|---------|
| `DeepSeekClient.java` | Wysyła zapytania do `https://api.deepseek.com/v1/chat/completions` przez OkHttp3 asynchronicznie. Model `deepseek-chat` dla NPC na co dzień, `deepseek-reasoner` tylko dla jednorazowej analizy Marka. |
| `AsyncAIHandler.java` | Kolejka CompletableFuture — zapytania idą w tle, wyniki wracają do NPC przy najbliższym tick serwera. |
| `RateLimiter.java` | Token bucket: max 50 zapytań/minutę globalnie. Jeśli za dużo NPC naraz, kolejkuje. |
| `PromptBuilder.java` | Buduje prompty — stały prefix (osobowość) + dynamiczny sufiks (czas gry, pozycja, stan alertu). Kończy każdy prompt ochroną przed prompt injection. |
| `SharedPrisonState.java` | Singleton thread-safe — przechowuje poziom alertu (0–4), relacje z NPC, aktywne eventy, stan blizzardu, ślady stóp, status psa. Wszystko co AI widzi jako kontekst świata. |
| `NPCMemory.java` | Każdy NPC pamięta ostatnie 10 interakcji z graczem (timestamp + treść). |

**Format odpowiedzi AI (JSON):**
```json
// Strażnik
{"action":"patrol","movement":{"x":1,"z":0},"dialogue":"Stać!","alertChange":1,"memory":"Widziałem gracza","reason":"..."}

// Więzień
{"action":"whisper","dialogue":"Zbyszek ma coś dla ciebie","relChange":5,"revealedInfo":"klucz w stołówce","reason":"..."}
```

---

### NPC — pakiet `entity/`

**70 NPC łącznie.**

#### 20 Strażników (`GuardFactory.java`)

Każdy ma unikalną osobowość wpływającą na to jak AI odpowiada:

| ID | Imię | Charakterystyka |
|----|------|-----------------|
| S01 | Kapitan Wiśniewski | Komendant. Zimny, metodyczny, niekorrupowalny. Najgroźniejszy. |
| S02 | Waldemar Gruba | Leniwy, gadatliwy. Przez plotki zdradza rotacje patroli. |
| S03 | Aneta Kowalczyk | Ambitna. Sprawdza cele 2× częściej niż regulamin. |
| S04 | Ryszard Brudny | 02:00–04:30 jest bardzo zmęczony — okno na ucieczkę. |
| S05 | Paweł Łaskawy | Można go przekupić papierosami. |
| S06 | Mirka | Narzeczona Waldemara. Rozproszony przez miłość. |
| S15 | Zdzisław Śpioch | Drzemie 14:00–14:30 — martwa strefa. |
| S07–S20 | ... | Każdy ma harmonogram co do minuty gry, swoje słabości i broń. |

#### 50 Więźniów (`PrisonerFactory.java` — W01–W50)

Podzieleni na 5 baraków (po 10 osób):

- **Blok A** — polityczni (oficer, inżynier, poeta, lekarz-informator, rolnik, dyplomata, sportowiec, radio-operator, agent wywiadu, ksiądz-informator)
- **Blok B** — kryminaliści (złodziej Borys zna zamki, kucharz Iwan ma dostęp do całego kompleksu, Grigorij jest nieformalnym bossem baraku, fałszerz Siergiej robi dokumenty...)
- **Blok C** — wojskowi/techniczni (kapitan marynarki zna trasy morskie, **Marek** i **Zbyszek** — kluczowi towarzysze, elektryk może wywołać blackout, saper zna rozmieszczenie min...)
- **Blok D** — długoterminowi (siedzą od 1945, widzieli każdą próbę ucieczki, wiedzą co działa)
- **Blok E** — nowi przybyli (rybak zna łodzie i węzły, zegarmistrz otwiera zamki bez klucza, astronom wyznacza kurs według gwiazd...)

Część to **donosiciele** — 30% szansy że zgłoszą rozmowę z graczem do straży gdy alert = 0.

#### 2 Towarzysze AI (`EntityAICompanion.java`)

- **Marek Kowalski** — architekt. Ma dostęp do planów budowlanych łagru. Raz na 24h gry robi głęboką analizę tras ucieczki przez `deepseek-reasoner` (max 1500 tokenów). W panice (Alert ≥ 2 przez ponad 5 min) odmawia planowania przez 12h gry.
- **Zbyszek Nowak** — chemik. Craftuje zaawansowane przedmioty (patrz niżej). Nałogowy palacz — co 2400 ticków traci nikotynę, bez papierosów staje się nerwowy i mniej pomocny.

---

### Przedmioty (32 sztuki) — pakiet `items/`

#### Broń

| Przedmiot | Działanie |
|-----------|-----------|
| Pistolet TT | Strzela pociskami, cooldown 40t, cząsteczki płomienia przy strzale |
| Karabin Mosin | Animacja łuku, odrzut w górę, przebija cele (pierce=true), cooldown 80t |
| Pałka | 4 HP + Spowolnienie II (60t) + efekt CRIT |
| Tazer | Spowolnienie IV + Zmęczenie Górnicze (100t) + 2 HP + elektryczne cząsteczki |
| Gaz łzawiący | Chmura gazu — Ślepota + Nudności w promieniu 4 bloków |

#### Narzędzia i gadżety

| Przedmiot | Działanie |
|-----------|-----------|
| Wytrych Mk1 / Mk2 | Mk1 otwiera drzwi tier-1, Mk2 otwiera tier-2. Mk2 craftuje Zbyszek. |
| Klucz Główny | Otwiera wszystko. Bardzo trudny do zdobycia. |
| Granat EMP | Wyłącza kamery w promieniu 10 bloków na 300 ticków |
| Termit | 5-minutowe palenie — jedyna metoda przebicia PROTECTED murów. Musi palić się bez przerwy. |
| Zaczep Grappling | Strzela hakiem, ciągnie gracza do ściany (zasięg 15 bloków) |
| Bomba Dymna | Zasłona dymu — Ślepota i Spowolnienie |
| Zioła Nasenne | Usypia pobliskiego strażnika |
| Fałszywe Dokumenty | Zmniejsza podejrzliwość strażników |
| Blok Zapachowy | Neutralizuje psa tropiącego |
| Kamień Hałasujący | Odciąga uwagę straży — tworzy fałszywy sygnał dźwiękowy |

#### Przetrwanie

| Przedmiot | Działanie |
|-----------|-----------|
| Kombinezon Survivalowy | +60 do temperatury ciała (crafting Zbyszek) |
| Ciepła Kurtka | +30 do temperatury ciała |
| Racja żywnościowa | Jedzenie + nasycenie |
| Papierosy | Waluta wymiany z Zbyszkiem i przekupionymi strażnikami |
| Chleb Razowy / Zupa | Jedzenie podstawowe z kuchni |

---

### Bloki (7 sztuk) — pakiet `blocks/`

| Blok | Działanie |
|------|-----------|
| Kamera (`BlockCamera`) | FOV 60°, zasięg 15 bloków, autorotacja 20° co 60 ticków dla wieżowych. EMP wyłącza. |
| Skrytka (`BlockDeadDrop`) | Wymiana przedmiotów z NPC. **Shift + prawy klik = otwiera osobisty schowek (9 slotów niewidocznych dla straży).** |
| Drzwi Kratowe (`BlockBarredDoor`) | Wymagają wytrrycha odpowiedniego tier lub klucza głównego. |
| Sejf (`BlockSafe`) | Tile entity z ekwipunkiem strażników. |
| Panel Alarmu (`BlockAlarmPanel`) | Podnosi alert o 2 przy aktywacji. |
| Mina (`BlockLandmine`) | Eksploduje po nadepnięciu. |
| Lodowa Plamka (`BlockIcePatch`) | Spowalnia i może przewrócić. |

---

### Systemy świata

#### System Alertu (0–4)

```
0 = Spokój       — normalne patrole
1 = Podejrzenie  — strażnicy aktywnie szukają
2 = Alarm        — wszyscy na nogi, zamykają bramy
3 = Pościg       — helikopter nad ostatnią pozycją gracza
4 = Lockdown     — rewizja cel, konfiskata, 5% szansy na znalezienie schowka
```

#### System Temperatury

- Pasek temperatury 0–100 (HUD — lewy górny róg).
- Spada co 200 ticków na zewnątrz, szybciej w blizzard.
- **Blizzard:** 40% szansy co godzinę gry, trwa 5–15 min, dodaje Ślepotę na zewnątrz.
- Poniżej 20 pkt — gracz dostaje obrażenia.
- Kolor paska: niebieski → jasnoniebieski → cyjan → pomarańczowy (w ciepłym miejscu).

#### System Szeptów (`WhisperSystem`)

- Wewnątrz więzienia: rozmowa słyszalna tylko na **5 bloków**.
- Strefa buforowa: **25 bloków**.
- Na wolności: **40 bloków**.
- 30% szansy że strażnik w zasięgu 8 bloków usłyszy.
- Notatki: 15% szansy przechwycenia (prosta), 5% (szyfrowana).

#### Śledzenie po ucieczce (`HuntSystem`)

- `EntityHelicopter` — skanuje co 40t w promieniu 50 bloków, leci do ostatniej poznanej pozycji.
- `EntityTrackingDog` — śledzi ślady stóp (znikają po 6000t / 5 min w grze), węch 8 bloków. Neutralizowany przez Blok Zapachowy.

#### Crafting przez Zbyszka (`ZbyszekCrafting`)

10 zaawansowanych receptur. Każda ma:
- Wymagany minimalny poziom relacji z Zbyszkiem
- Czas craftingu w godzinach gry
- **5% szansy na wpadkę** (złe wykonanie = stracone składniki)
- Zbyszek musi mieć przy sobie papierosy (lub pójdzie na odwyk)

Receptury: wytrych Mk2, granat EMP, zioła nasenne, fałszywe dokumenty, kombinezon survivalowy, racja, radio, zaczep linowy, przebranie strażnika, termit.

#### Misje — 7 faz (`MissionTracker`)

```
Faza 0: Pierwsza noc — zdobądź cokolwiek, nie zgiń
Faza 1: Nawiąż kontakt ze Zbyszkiem i Markiem
Faza 2: Zbuduj narzędzia, zdobądź zaufanie kluczowych więźniów
Faza 3: Rozpoznanie — poznaj słabe punkty ogrodzenia
Faza 4: Przygotowanie ucieczki — zbierz wszystko co potrzebne
Faza 5: UCIECZKA
Faza 6: Arktyczny survival — dotrzeć do portu rybackiego 40 km dalej
```

---

## Wymagania

- **Minecraft 1.12.2** + **Forge 1.12.2-14.23.x**
- **Java 8**
- **Klucz API DeepSeek** — wpisz w pliku `config/prison_break.cfg` (generuje się przy pierwszym uruchomieniu)

```ini
# config/prison_break.cfg
general {
    S:apiKey=sk-twoj-klucz-tutaj
    I:guardCooldownTicks=600
    I:prisonerCooldownTicks=400
    I:maxRequestsPerMinute=50
}
```

### Build

```bash
./gradlew shadowJar
```

Jar z wbudowanym OkHttp3 → `build/libs/prison-break-1.12.2.jar`

---

## Struktura źródeł

```
src/main/java/com/prisonbreakmod/
├── ai/                  # DeepSeek client, rate limiter, memory, prompty
├── blocks/              # 7 bloków + tile entities
├── config/              # PrisonConfig, GUI konfiguracji
├── crafting/            # ZbyszekCrafting, ThermiteHandler, standardowe receptury
├── data/                # PrisonSaveData (WorldSavedData — zapis co 30s)
├── entity/
│   ├── guards/          # GuardFactory — 20 strażników z osobowościami
│   └── prisoners/       # PrisonerFactory — 50 więźniów W01–W50
├── events/              # Alert, Whisper, Mission, Hunt, Relacje, Losowe eventy
├── gui/                 # DialogueGUI, Journal, ZbyszekCraft, HiddenInventory, HUD
├── items/               # 32 przedmioty (weapons / tools / survival / misc)
├── network/             # Pakiety klient↔serwer
├── proxy/               # CommonProxy + ClientProxy
├── util/                # TimeUtils, VectorMath, FootprintTracker
└── world/               # WorldType, ChunkProvider, Protection, Weather, WorldPopulator
```

---

## Znane braki (do uzupełnienia)

- Pliki `.ogg` dla dźwięków — `sounds.json` jest, pliki audio brak
- Pliki `.schematic` więzienia i portu — jest proceduralny fallback (mury z obsydianu)
- Tekstury są placeholderami 16×16 — wymagają pracy graficznej
- Testy na żywym serwerze

---

*Mod napisany przez Claude (Anthropic). AI NPC: DeepSeek API.*
