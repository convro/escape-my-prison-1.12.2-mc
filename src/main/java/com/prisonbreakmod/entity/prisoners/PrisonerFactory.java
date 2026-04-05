package com.prisonbreakmod.entity.prisoners;

import com.prisonbreakmod.entity.EntityPrisoner;
import net.minecraft.world.World;

/**
 * Creates all 50 named prisoners (W01-W50) for the gulag population.
 * Each prisoner has a unique backstory, cell assignment, and informant flag.
 */
public class PrisonerFactory {

    /**
     * Creates a specific prisoner by their 1-based index (1–50).
     *
     * @param world    server world
     * @param index    prisoner number 1-50
     * @return configured {@link EntityPrisoner} or null if index out of range
     */
    public static EntityPrisoner create(World world, int index) {
        switch (index) {
            // ----------------------------------------------------------------
            // Blok A — Polityczni (01-10)
            // ----------------------------------------------------------------
            case 1:
                return new EntityPrisoner(world, 1,
                        "Aleksander Nowak", "A-01",
                        "Były oficer Armii Czerwonej skazany za 'szpiegostwo' w 1952 r. " +
                        "Zna rozkład straży i ma kontakty w kuchni. Wierzy w możliwość ucieczki przez tunel.",
                        false);
            case 2:
                return new EntityPrisoner(world, 2,
                        "Józef Wiśniewski", "A-02",
                        "Inżynier kolejowy. Trafił tu za sabotaż — odmówił naprawy lokomotyw wysyłanych na front. " +
                        "Zna mechanikę i może naprawić sprzęt. Szuka okazji do wykradzenia dokumentów.",
                        false);
            case 3:
                return new EntityPrisoner(world, 3,
                        "Tadeusz Kowalski", "A-03",
                        "Poeta i profesor literatury. Skazany za 'antyradziecki wiersz'. " +
                        "Notuje w sekretnym dzienniku schowanym pod podłogą. Boi się donosicieli.",
                        false);
            case 4:
                return new EntityPrisoner(world, 4,
                        "Stefan Jankowski", "A-04",
                        "Lekarz weterynarii. Potrafi leczyć rany i rozróżnia zioła lecznicze od trujących. " +
                        "Dostarcza leki strażnikom w zamian za przywileje — informator z przymusu.",
                        true);
            case 5:
                return new EntityPrisoner(world, 5,
                        "Henryk Zając", "A-05",
                        "Rolnik skazany za 'kułactwo'. Zna kraj, śnieżne szlaki i miejsca schronienia w tajdze. " +
                        "Gotów pomóc za obietnicę powrotu do rodziny.",
                        false);
            case 6:
                return new EntityPrisoner(world, 6,
                        "Mieczysław Dąbrowski", "A-06",
                        "Były dyplomata. Mówi po rosyjsku z perfekcyjnym akcentem, zna protokół strażniczy. " +
                        "Może sfałszować dokumenty przejazdu.",
                        false);
            case 7:
                return new EntityPrisoner(world, 7,
                        "Ryszard Grabowski", "A-07",
                        "Sportowiec — skoczek narciarski. Trafił tu za odmowę startu pod sowiecką flagą. " +
                        "Doskonała kondycja, zna techniki wspinaczki i ruchu w śniegu.",
                        false);
            case 8:
                return new EntityPrisoner(world, 8,
                        "Kazimierz Piotrowski", "A-08",
                        "Radio-operator floty. Może zbudować prosty nadajnik z odpadków. " +
                        "Bardzo ostrożny — stracił kolegę przez donos. Nie ufa nikomu przez pierwsze 3 tygodnie.",
                        false);
            case 9:
                return new EntityPrisoner(world, 9,
                        "Leon Wróbel", "A-09",
                        "Tajny agent polskiego wywiadu. Był infiltrował sowiecki garnizon gdy go złapali. " +
                        "Zna kilka tożsamości i techniki kamuflażu. Podejrzliwy wobec wszystkich.",
                        false);
            case 10:
                return new EntityPrisoner(world, 10,
                        "Władysław Nowicki", "A-10",
                        "Ksiądz katolicki. Służy jako duchowy autorytet dla polskich więźniów. " +
                        "Organizuje tajne nabożeństwa, przez co jest na celowniku komendanta. Informator Stefa.",
                        true);

            // ----------------------------------------------------------------
            // Blok B — Kryminaliści (11-20)
            // ----------------------------------------------------------------
            case 11:
                return new EntityPrisoner(world, 11,
                        "Borys Karpow", "B-01",
                        "Rosyjski złodziej z Leningradu. Zna się na zamkach i sejfach lepiej niż ktokolwiek. " +
                        "Może wykraść klucz strażnika za odpowiednią cenę. Lubi papierosowy handel.",
                        false);
            case 12:
                return new EntityPrisoner(world, 12,
                        "Iwan Siemionow", "B-02",
                        "Były kucharz wojskowy skazany za kradzież z magazynów armii. " +
                        "Pracuje w kuchni łagru i przemyca żywność. Ma dostęp do całego kompleksu.",
                        false);
            case 13:
                return new EntityPrisoner(world, 13,
                        "Nikołaj Fiedorow", "B-03",
                        "Zawodowy przemytnik z granicy fińskiej. Zna tajne trasy przez las. " +
                        "Prowadzi nieformalny rynek wymiany w baraku B. Informator Stefa za tytoń.",
                        true);
            case 14:
                return new EntityPrisoner(world, 14,
                        "Grigorij Morozow", "B-04",
                        "Szef bloku B — faktyczny boss wśród kryminalistów. " +
                        "Kontroluje dystrybucję jedzenia. Może zapewnić ochronę lub utrudnić życie.",
                        false);
            case 15:
                return new EntityPrisoner(world, 15,
                        "Paweł Strogow", "B-05",
                        "Samouk mechanik. Naprawia narzędzia i sprzęt strażników w zamian za racje. " +
                        "Zna słaby punkt w ogrodzeniu — wygiętą kratkę przy stacji meteo.",
                        false);
            case 16:
                return new EntityPrisoner(world, 16,
                        "Dmitrij Koniow", "B-06",
                        "Były bokser i przestępca z Moskwy. Bardzo silny, służy jako ochrona Grigorija. " +
                        "Nie myśli zbyt wiele, ale jest bezgranicznie lojalny wobec bossa.",
                        false);
            case 17:
                return new EntityPrisoner(world, 17,
                        "Siergiej Łukow", "B-07",
                        "Fałszerz dokumentów. Potrafił podrabiać paszporty tak dobrze, że musiał tu trafić. " +
                        "Może pomóc zrobić fałszywe dokumenty za wynagrodzenie.",
                        false);
            case 18:
                return new EntityPrisoner(world, 18,
                        "Fiodor Baskow", "B-08",
                        "Alkoholik i były chemik fabryczny. Destyluje nielegalny samogon. " +
                        "Zna się na chemikaliach i może dostarczyć środki chemiczne.",
                        false);
            case 19:
                return new EntityPrisoner(world, 19,
                        "Wiktor Gawriłow", "B-09",
                        "Młody recydywista — trzecia odsiadka. Nienawidzi systemu bardziej niż czegokolwiek. " +
                        "Chętnie dołączy do planu ucieczki, ale ma skłonność do nierozważnych działań.",
                        false);
            case 20:
                return new EntityPrisoner(world, 20,
                        "Anton Mielnikow", "B-10",
                        "Były żołnierz karny. Schwytany za dezercję pod Stalingradem. " +
                        "Ma doświadczenie bojowe, zna wojskowe procedury i może pomóc w sabotażu.",
                        false);

            // ----------------------------------------------------------------
            // Blok C — Wojskowi i techniczni (21-30)
            // ----------------------------------------------------------------
            case 21:
                return new EntityPrisoner(world, 21,
                        "Zygmunt Lewandowski", "C-01",
                        "Kapitan polskiej marynarki wojennej. Orientuje się w mapach morskich i nawigacji. " +
                        "Wie, że po ucieczce z wyspy można dopłynąć do portu rybackiego 40 km na wschód.",
                        false);
            case 22:
                return new EntityPrisoner(world, 22,
                        "Marek Zalewski", "C-02",
                        "Architekt — tu służy jako rysownik planów budowlanych dla nowych baraków. " +
                        "Dostęp do planów całego łagru. KLUCZOWY SOJUSZNIK. Towarzysz AI.",
                        false);
            case 23:
                return new EntityPrisoner(world, 23,
                        "Zbyszek Olszewski", "C-03",
                        "Chemik i farmaceuta. Produkuje leki, narkotyki i materiały wybuchowe na czarnym rynku. " +
                        "Nałogowy palacz — bez papierosów staje się nerwowy. KLUCZOWY SOJUSZNIK. Towarzysz AI.",
                        false);
            case 24:
                return new EntityPrisoner(world, 24,
                        "Andrzej Mazurek", "C-04",
                        "Elektryk z Warszawy. Zna instalację elektryczną łagru jak własną kieszeń. " +
                        "Może wywołać blackout. Współpracuje z Markiem przy planowaniu.",
                        false);
            case 25:
                return new EntityPrisoner(world, 25,
                        "Tomasz Wojciechowski", "C-05",
                        "Pilot myśliwski — zestrzelony nad Finlandią. Zna terytorium Syberii z lotu ptaka. " +
                        "Może wskazać trasy lądowe i orientacyjne punkty do granicy.",
                        false);
            case 26:
                return new EntityPrisoner(world, 26,
                        "Krzysztof Szymański", "C-06",
                        "Saperski podoficerski — zna się na minach i pułapkach. " +
                        "Wie, gdzie są miny wokół obozu i jak je ominąć. Cierpi na traumę powojenną.",
                        false);
            case 27:
                return new EntityPrisoner(world, 27,
                        "Franciszek Kaczmarek", "C-07",
                        "Geograf i topograf. Sporządza potajemne mapy terenu za pomocą patyczków i śniegu. " +
                        "Może narysować mapę bezpiecznej drogi do granicy.",
                        false);
            case 28:
                return new EntityPrisoner(world, 28,
                        "Grzegorz Pawlak", "C-08",
                        "Mechanik samochodowy z Łodzi. Może uruchomić zahibernowaną ciężarówkę strażników. " +
                        "Lubi zagrywki z szansą — gotowy ryzykować jeśli wygrana jest warta.",
                        false);
            case 29:
                return new EntityPrisoner(world, 29,
                        "Stanisław Michalak", "C-09",
                        "Snajper wojskowy. Ma niezwykłą zdolność skupienia i cierpliwość. " +
                        "Pomaga obserwować ruchy straży przez szczeliny w dachu baraku.",
                        false);
            case 30:
                return new EntityPrisoner(world, 30,
                        "Władimir Pietrow", "C-10",
                        "Radziecki oficer oskarżony o korupcję. Zna procedury straży od środka. " +
                        "Ma resentyment do komendanta. Może dostarczyć informacji o rotacjach warty.",
                        false);

            // ----------------------------------------------------------------
            // Blok D — Starzy więźniowie, długoterminowi (31-40)
            // ----------------------------------------------------------------
            case 31:
                return new EntityPrisoner(world, 31,
                        "Bolesław Wierzbicki", "D-01",
                        "Siedzi tu od 1945 roku. Widział dziesiątki prób ucieczki, wie co się kończyło sukcesem. " +
                        "Ostrzeże przed pułapkami psychologicznymi komendanta.",
                        false);
            case 32:
                return new EntityPrisoner(world, 32,
                        "Czesław Rutkowski", "D-02",
                        "Były tłumacz polsko-rosyjski. Pracuje jako tłumacz dla komendanta. " +
                        "Ma dostęp do gabinetu. Informator z przymusu, ale wewnętrznie po naszej stronie.",
                        true);
            case 33:
                return new EntityPrisoner(world, 33,
                        "Eugeniusz Kucharski", "D-03",
                        "Stary zek — obozowy filozof. Zna prawa i zwyczaje nieformalnego kodeksu więźniów. " +
                        "Może pośredniczyć w konfliktach między więźniami różnych narodowości.",
                        false);
            case 34:
                return new EntityPrisoner(world, 34,
                        "Feliks Malinowski", "D-04",
                        "Złamany człowiek — doniósł na 5 osób żeby przeżyć. Teraz żałuje. " +
                        "Może dostarczyć informacji o poprzednich donosach, ale jego relacja zaczyna się od 0.",
                        true);
            case 35:
                return new EntityPrisoner(world, 35,
                        "Ignacy Sobiecki", "D-05",
                        "Były burmistrz małego miasta. Dobry organizator, umie skoordynować wielu ludzi. " +
                        "Może pomóc przy dywersji wymagającej synchronizacji działań.",
                        false);
            case 36:
                return new EntityPrisoner(world, 36,
                        "Józef Krakowski", "D-06",
                        "Tkacz i krawiec. Umie szyć i farbować ubrania — może zamienić mundur strażnika. " +
                        "Jest spokojny i zrównoważony. Jeden z niewielu, którzy lubią tu komendanta.",
                        false);
            case 37:
                return new EntityPrisoner(world, 37,
                        "Karol Wolski", "D-07",
                        "Dentysta. Leczy zęby strażnikom, przez co ma dużo swobody poruszania się. " +
                        "Ma małe metalowe narzędzia, które mogą posłużyć jako wytrych.",
                        false);
            case 38:
                return new EntityPrisoner(world, 38,
                        "Leszek Górski", "D-08",
                        "Trener pływania. Marzenie: uciec przez zamarzające morze na przełomie pór roku. " +
                        "Zna się na prądach i temperaturze wody.",
                        false);
            case 39:
                return new EntityPrisoner(world, 39,
                        "Marian Czajkowski", "D-09",
                        "Bibliotekarz. Dostęp do nielicznej biblioteki obozowej, gdzie chowa kontrabandę. " +
                        "Może udostępnić ukryte materiały, jeśli relacja jest wystarczająca.",
                        false);
            case 40:
                return new EntityPrisoner(world, 40,
                        "Narcyz Wieczorek", "D-10",
                        "Aktor teatralny. Mistrz kamuflażu i wcielania się w role. " +
                        "Może 'zagrać' rolę strażnika przekonująco dla nowych więźniów. Informator Mirki.",
                        true);

            // ----------------------------------------------------------------
            // Blok E — Nowi przybyli (41-50)
            // ----------------------------------------------------------------
            case 41:
                return new EntityPrisoner(world, 41,
                        "Olaf Brandt", "E-01",
                        "Niemiec — jeniec wojenny, który nie chciał wracać do NRD. " +
                        "Mówi po polsku łamaną polszczyzną. Ma zegarek z kompasem ukryty w bucie.",
                        false);
            case 42:
                return new EntityPrisoner(world, 42,
                        "Piotr Jabłoński", "E-02",
                        "Student medycyny. Trafił tu w wieku 19 lat za ulotkę. " +
                        "Najgorzej znosi zimno — choruje. Ale ma ostry umysł i pamięta każdy szczegół.",
                        false);
            case 43:
                return new EntityPrisoner(world, 43,
                        "Rafał Kwiatkowski", "E-03",
                        "Syn lokalnego dygnitarza — wpadł przez przypadek w obławę. " +
                        "Ojciec szuka go od miesięcy. Zdesperowany, gotowy na wszystko żeby wrócić.",
                        false);
            case 44:
                return new EntityPrisoner(world, 44,
                        "Sławomir Adamski", "E-04",
                        "Rybak z Gdańska. Zna się na łodziach, siatkach i węzłach marynarskich. " +
                        "Twierdzi, że widział z łodzi rybackiej kuter wojskowy przy północnym brzegu.",
                        false);
            case 45:
                return new EntityPrisoner(world, 45,
                        "Tymoteusz Kubiak", "E-05",
                        "Wiejski kowale i ślusarz. Może wykuć proste narzędzia z odpadów metalu. " +
                        "Bardzo silny, nie boi się konfrontacji fizycznej. Lubi cichy szacunek.",
                        false);
            case 46:
                return new EntityPrisoner(world, 46,
                        "Urban Niezgoda", "E-06",
                        "Handlarz z Krakowa. Perfekcyjny w negocjacjach i bazarowym handlu. " +
                        "Organizuje nieformalny rynek wymiany między barakami. Zawsze ma co sprzedać.",
                        false);
            case 47:
                return new EntityPrisoner(world, 47,
                        "Viktor Haluszczuk", "E-07",
                        "Ukraiński nacjonalista, banderowiec. Nienawidzi Sowietów jeszcze bardziej niż Polacy. " +
                        "Zna lasy Syberii — uciekał kiedyś przez trzy tygodnie zanim go złapali.",
                        false);
            case 48:
                return new EntityPrisoner(world, 48,
                        "Walenty Szewczyk", "E-08",
                        "Zegarmistrz z Poznania. Niezwykła precyzja manualną — otwiera zamki bez klucza. " +
                        "Płakał przez pierwsze dwa tygodnie. Teraz skupiony jak laser.",
                        false);
            case 49:
                return new EntityPrisoner(world, 49,
                        "Xawery Biały", "E-09",
                        "Astronom amator. Orientuje się według gwiazd i może wyznaczyć kurs ucieczki nocą. " +
                        "Mówi zbyt głośno przez co strażnicy go lubią — bywa nieświadomie dekonspirujący.",
                        false);
            case 50:
                return new EntityPrisoner(world, 50,
                        "Zenon Czarny", "E-10",
                        "Najmłodszy więzień — 16 lat, syn skazanego. Sierotą z łagru. " +
                        "Wie wszystko o łagrze bo tu dorastał. Szybki, cichy, niewidzialny dla straży. " +
                        "Niezwykle lojalny jeśli okazać mu opiekę.",
                        false);

            default:
                return null;
        }
    }

    /**
     * Creates all 50 prisoners and returns them as an array.
     * Null entries indicate creation failure.
     */
    public static EntityPrisoner[] createAll(World world) {
        EntityPrisoner[] result = new EntityPrisoner[50];
        for (int i = 0; i < 50; i++) {
            result[i] = create(world, i + 1);
        }
        return result;
    }
}
