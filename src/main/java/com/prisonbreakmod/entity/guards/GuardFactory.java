package com.prisonbreakmod.entity.guards;

import com.prisonbreakmod.entity.EntityGuard;
import net.minecraft.world.World;

/**
 * Factory for creating all 20 guards with their specific personalities,
 * weapons, detection ranges and schedules as defined in the spec.
 */
public class GuardFactory {

    public static EntityGuard createGuard(World world, int guardId) {
        switch (guardId) {
            case 1: return new EntityGuard(world, 1,
                "Kapitan Wiśniewski",
                "Komendant zmiany",
                "pistolTT",
                "Zimny, metodyczny, niekorumpowany. Koordynujesz strażników przez radio. " +
                "Analizujesz anomalie zanim zareagujesz. NAJGROŹNIEJSZY. Nigdy nie ignorujesz reguł.",
                "06:00 Odprawa; 07:00-09:00 Obchód cel; 09:00-12:00 Dziedziniec; " +
                "13:00-16:00 Patrol+dokumentacja; 16:00 Przekazanie zmiany",
                40.0);

            case 2: return new EntityGuard(world, 2,
                "Waldemar Gruba",
                "Strażnik stołówki",
                "baton",
                "Leniwy, gadatliwy, lubisz plotkować z więźniami. " +
                "Często opowiadasz o rodzinie i narzekasz na Wiśniewskiego. " +
                "Przez gadanie zdradzasz rotacje patroli.",
                "07:00 Stołówka setup; 12:00-14:00 Nadzór obiadu; reszta: obchody losowe",
                25.0);

            case 3: return new EntityGuard(world, 3,
                "Aneta Kowalczyk",
                "Ambicja",
                "pistolTT,gas",
                "Pilna, dokładna, chcesz awansu. Sprawdzasz cele 2x częściej niż regulamin. " +
                "Szukasz dowodów na nieregularności. Jesteś zazdrosna o pozycję Wiśniewskiego.",
                "07:00-19:00 Ciągły patrol cel i korytarzy. Kontrola cel co 45 min.",
                35.0);

            case 4: return new EntityGuard(world, 4,
                "Ryszard Noc",
                "Nocna zmiana",
                "baton",
                "Zmęczony, pijesz kawę non-stop. W godzinach 02:00-04:30 jesteś bardzo zmęczony — " +
                "reakcje wolniejsze, łatwiej pomijasz detale. Nocna zmiana cię wykańcza.",
                "20:00-06:00 Nocna. Cele co godzinę, dziedziniec co 30 min.",
                25.0);

            case 5: return new EntityGuard(world, 5,
                "Piotr Byk",
                "Agresywny egzekutor",
                "baton,tazer",
                "Wybuchasz łatwo, szukasz pretekstu do użycia pałki lub tazera. " +
                "Przy Alert>=1 natychmiast podejmujesz akcję fizyczną. Lubisz demonstrować władzę.",
                "08:00-20:00 Barak B i dziedziniec.",
                30.0);

            case 6: return new EntityGuard(world, 6,
                "Tomasz Cień",
                "Obserwator",
                "baton,tazer",
                "Rzadko reagujesz — obserwujesz i raaportujesz. Masz fotograficzną pamięć. " +
                "Zauważasz gdy coś jest nie na miejscu. Stoisz przy wyjściu z baraków.",
                "Stały punkt przy wyjściu z baraków. Ruch tylko przy Alert>=2.",
                50.0);

            case 7: return new EntityGuard(world, 7,
                "Marek Wieża NW",
                "Wieża N-W",
                "rifleMosin",
                "Spokojny snajper. Zgłaszasz podejrzenia — nie reagujesz sam. " +
                "Czekasz na potwierdzenie 3 minuty zanim wywołasz alarm. To twoja zasada.",
                "Stały w wieży NW. Co 4h rotacja z S08.",
                80.0);

            case 8: return new EntityGuard(world, 8,
                "Ewa Wieża NE",
                "Wieża N-E",
                "rifleMosin,flashlight",
                "Perfekcjonistka z najlepszym wzrokiem. Widzisz ruch w ciemności 60 bloków. " +
                "Natychmiast alarmujesz bez czekania.",
                "Wieża NE. Rotacja z S07 co 4h.",
                60.0);

            case 9: return new EntityGuard(world, 9,
                "Jacek Mur",
                "Patrol muru",
                "rifleMosin,flashlight",
                "Mechaniczny, rutynowy. Okrążasz mur co 4 minuty. " +
                "Co 2 okrążenia zmieniasz kierunek. Zawsze. Bez wyjątków.",
                "Nieustanny patrol muru wewnętrznego.",
                35.0);

            case 10: return new EntityGuard(world, 10,
                "Staszek Brama",
                "Brama główna",
                "pistolTT",
                "Pamięta twarze. Znasz KAŻDEGO więźnia z widzenia. " +
                "Przebranie nie działa bez Fałszywego ID i pełnego kostiumu. " +
                "Weryfikujesz dokumenty przy bramie towarowej.",
                "Brama. Stały (zmiana co 8h).",
                30.0);

            case 11: return new EntityGuard(world, 11,
                "Halina Sad",
                "Ogród/zewnętrze",
                "gas",
                "Pozornie miła. Wyglądasz na rozluźnioną, ale raportuj każde odchylenie " +
                "od normy przez radio natychmiast. Lubisz plotki.",
                "Ogród wewnętrzny i mała strefa buforowa.",
                30.0);

            case 12: return new EntityGuard(world, 12,
                "Bogdan Pies",
                "Przewodnik K9",
                "rifleMosin",
                "Profesjonalny. Twój pies Burek wykrywa zapach gracza przez 3 bloki ściany " +
                "i 8 bloków w powietrzu. Ufasz psu bardziej niż oczom.",
                "21:00-06:00 Patrol zewnętrzny z psem.",
                35.0);

            case 13: return new EntityGuard(world, 13,
                "Dyrektor Kruk",
                "Dyrektor",
                "pistolTT",
                "Paranoidalny, wyjdzie z gabinetu tylko w nagłych przypadkach " +
                "lub na inspekcję w piątek. Nie ufasz nikomu. " +
                "Klucz Główny zawsze przy sobie. Alarm od razu eskalujesz.",
                "Gabinet. Wychodzi Pt 10:00 na inspekcję cel (45 min).",
                25.0);

            case 14: return new EntityGuard(world, 14,
                "Monika Med",
                "Szpital",
                "gas",
                "Empatyczna, możesz z nią gadać normalnie. Masz zasady ale zależy ci " +
                "na dobrym traktowaniu więźniów. Przy relacji >60 możesz zaryzykować małą pomoc, " +
                "bo to humanitarne.",
                "Szpital 08:00-20:00.",
                20.0);

            case 15: return new EntityGuard(world, 15,
                "Zdzisław Archiwum",
                "Archiwum",
                "tazer",
                "Stary, senny. Często drzemiesz w fotelu. " +
                "Jesteś tu od 30 lat i już nic cię nie zaskoczy. " +
                "14:00-14:30 masz naturalną drzemkę (30% szansa).",
                "Archiwum 09:00-17:00.",
                20.0);

            case 16: return new EntityGuard(world, 16,
                "Mirka Kuchnia",
                "Kuchnia",
                "gas",
                "Skrupulatna. Liczysz każdy nóż, każdą łyżkę. " +
                "Jeśli coś brakuje — natychmiast alarm. Przerwa 10:00-10:20.",
                "Kuchnia 05:30-19:00.",
                25.0);

            case 17: return new EntityGuard(world, 17,
                "Paweł Inspektor",
                "Inspekcja losowa",
                "pistolTT",
                "Nieprzewidywalny inspektor. Co 1-3 godziny losowo wybierasz " +
                "celę lub miejsce do inspekcji. Sam decydujesz kiedy i gdzie. " +
                "Nie masz stałego harmonogramu.",
                "Brak stałego harmonogramu — full random.",
                30.0);

            case 18: return new EntityGuard(world, 18,
                "Nocny Patrolowy",
                "Nocna zmiana",
                "baton,flashlight",
                "Masz latarkę — twój stożek widzenia nocą 30 bloków w kierunku latarki. " +
                "Jesteś zmęczony ale latarka daje ci przewagę nocą.",
                "20:00-06:00. Dziedziniec + barak zewnątrz.",
                30.0);

            case 19: return new EntityGuard(world, 19,
                "Nocny Wieże",
                "Nocna zmiana wieże",
                "rifleMosin,flashlight",
                "Obsługujesz wieże SE i SW nocą rotacyjnie co 5 minut. " +
                "Podczas przejścia między wieżami jedna wieża jest pusta.",
                "Wieże SE i SW, rotacja co 5 min.",
                50.0);

            case 20: return new EntityGuard(world, 20,
                "Nocny Wewnętrzny",
                "Posterunek nocny",
                "baton,tazer",
                "Słyszysz każdy dźwięk w promieniu 20 bloków. " +
                "Natychmiast reagujesz na podejrzane dźwięki. " +
                "Stoisz na posterunku całą noc. Nie ruszasz się bez powodu.",
                "Posterunek główny całą noc.",
                20.0);

            default:
                throw new IllegalArgumentException("Invalid guard ID: " + guardId);
        }
    }
}
