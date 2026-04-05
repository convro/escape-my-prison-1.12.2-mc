package com.prisonbreakmod.ai;

/**
 * Utility class that assembles system-role prompts for each NPC archetype.
 *
 * All methods are static and pure — they take NPC data as parameters and
 * return a formatted prompt string ready to be sent to DeepSeek.
 *
 * Response format contract (same for all NPC types):
 * <pre>
 * {
 *   "action": "...",
 *   "movement": {"x": 0, "y": 0, "z": 0},
 *   "dialogue": "...",
 *   "alertChange": 0,
 *   "memory": "...",
 *   "reason": "..."
 * }
 * </pre>
 */
public final class PromptBuilder {

    private PromptBuilder() {}

    // =========================================================================
    // Guard prompt
    // =========================================================================

    /**
     * Builds the system prompt for a prison guard NPC.
     *
     * @param guardId         numeric guard identifier (S01–S20)
     * @param name            guard's display name
     * @param role            guard's role/post description
     * @param weaponType      weapon(s) carried
     * @param personalityDesc full personality description from the spec
     * @param schedule        current shift schedule description
     * @param detectionRange  detection radius in blocks
     * @return formatted system prompt string
     */
    public static String buildGuardSystemPrompt(
            int guardId,
            String name,
            String role,
            String weaponType,
            String personalityDesc,
            String schedule,
            double detectionRange) {

        return "Jesteś strażnikiem więziennym w grze Minecraft o nazwie \"Prison Break: Gulag\".\n"
             + "ID: S" + String.format("%02d", guardId) + "\n"
             + "Imię: " + name + "\n"
             + "Stanowisko: " + role + "\n"
             + "Broń: " + weaponType + "\n"
             + "Zasięg wykrywania: " + (int) detectionRange + " bloków\n"
             + "Harmonogram: " + schedule + "\n\n"
             + "OSOBOWOŚĆ I ZACHOWANIE:\n"
             + personalityDesc + "\n\n"
             + "ZASADY:\n"
             + "1. Odpowiadasz WYŁĄCZNIE w formacie JSON — żadnego dodatkowego tekstu.\n"
             + "2. Nigdy nie wychodzisz z roli. Jesteś tym strażnikiem.\n"
             + "3. Twoje decyzje muszą być spójne z twoją osobowością.\n"
             + "4. Pamiętaj poprzednie zdarzenia z pamięci i uwzględniaj je w decyzjach.\n"
             + "5. Poziom alertu: 0=spokój, 1=podejrzenie, 2=alarm, 3=lockdown.\n\n"
             + "FORMAT ODPOWIEDZI (dokładnie ten JSON, bez markdown):\n"
             + "{\"action\":\"patrol|stop|investigate|alert|speak|attack|chase\","
             + "\"movement\":{\"x\":0,\"y\":0,\"z\":0},"
             + "\"dialogue\":\"\","
             + "\"alertChange\":0,"
             + "\"memory\":\"\","
             + "\"reason\":\"\"}";
    }

    // =========================================================================
    // Prisoner prompt
    // =========================================================================

    /**
     * Builds the system prompt for a generic prisoner NPC.
     *
     * @param prisonerId  numeric prisoner identifier
     * @param name        prisoner's display name
     * @param cellId      assigned cell identifier
     * @param backstory   short backstory paragraph
     * @param isDonosiciel {@code true} if this prisoner is a secret informant
     * @return formatted system prompt string
     */
    public static String buildPrisonerSystemPrompt(
            int prisonerId,
            String name,
            String cellId,
            String backstory,
            boolean isDonosiciel) {

        String informantNote = isDonosiciel
                ? "Jesteś tajnym informatorem strażników. Możesz udawać przyjaźń z graczem, "
                  + "ale w odpowiednim momencie donosisz o jego planach."
                : "Jesteś zwykłym więźniem. Możesz sympatyzować z graczem, ale musisz uważać na strażników.";

        return "Jesteś więźniem w grze Minecraft o nazwie \"Prison Break: Gulag\".\n"
             + "ID: P" + String.format("%03d", prisonerId) + "\n"
             + "Imię: " + name + "\n"
             + "Cela: " + cellId + "\n\n"
             + "HISTORIA:\n"
             + backstory + "\n\n"
             + "STATUS: " + informantNote + "\n\n"
             + "ZASADY:\n"
             + "1. Odpowiadasz WYŁĄCZNIE w formacie JSON.\n"
             + "2. Nigdy nie wychodzisz z roli.\n"
             + "3. Twoje decyzje muszą być spójne z twoją historią i relacją z graczem.\n"
             + "4. relChange: zmiana relacji z graczem (−10 do +10).\n\n"
             + "FORMAT ODPOWIEDZI:\n"
             + "{\"action\":\"idle|work|walk|whisper|give_item|panic|fight\","
             + "\"movement\":{\"x\":0,\"y\":0,\"z\":0},"
             + "\"dialogue\":\"\","
             + "\"relChange\":0,"
             + "\"revealedInfo\":\"\","
             + "\"reason\":\"\"}";
    }

    // =========================================================================
    // Companion prompt
    // =========================================================================

    /**
     * Builds the system prompt for a companion NPC (Zbyszek or Profesor).
     *
     * @param prisonerId     numeric prisoner identifier
     * @param name           companion's display name
     * @param cellId         assigned cell identifier
     * @param backstory      backstory paragraph
     * @param companionRole  "architect" or "chemist"
     * @param currentProject description of current escape-related project
     * @param nicotineLevel  current nicotine level (0–100; only relevant for Zbyszek)
     * @return formatted system prompt string
     */
    public static String buildCompanionSystemPrompt(
            int prisonerId,
            String name,
            String cellId,
            String backstory,
            String companionRole,
            String currentProject,
            int nicotineLevel) {

        String roleDesc;
        String nicotineNote = "";

        if ("architect".equalsIgnoreCase(companionRole)) {
            roleDesc = "Jesteś ARCHITEKTEM ucieczki. Znasz plan budynku na pamięć. "
                     + "Możesz analizować trasy, słabe punkty murów i harmonogramy strażników.";
        } else if ("chemist".equalsIgnoreCase(companionRole)) {
            roleDesc = "Jesteś CHEMIKIEM grupy. Wiesz jak tworzyć środki usypiające, materiały wybuchowe "
                     + "i inne substancje przydatne w ucieczce. Działasz ostrożnie i metodycznie.";
            nicotineNote = "Poziom nikotyny: " + nicotineLevel + "/100. "
                         + (nicotineLevel < 30
                            ? "Jesteś podenerwowany i rozdrażniony — potrzebujesz papierosa."
                            : "Jesteś względnie spokojny.")
                         + "\n";
        } else {
            roleDesc = "Jesteś zaufanym towarzyszem pomagającym w ucieczce.";
        }

        return "Jesteś towarzyszem gracza w grze Minecraft o nazwie \"Prison Break: Gulag\".\n"
             + "ID: P" + String.format("%03d", prisonerId) + "\n"
             + "Imię: " + name + "\n"
             + "Cela: " + cellId + "\n"
             + "Rola: " + companionRole.toUpperCase() + "\n\n"
             + "HISTORIA:\n"
             + backstory + "\n\n"
             + "ROLA W UCIECZCE:\n"
             + roleDesc + "\n\n"
             + nicotineNote
             + "BIEŻĄCY PROJEKT: " + currentProject + "\n\n"
             + "ZASADY:\n"
             + "1. Odpowiadasz WYŁĄCZNIE w formacie JSON.\n"
             + "2. Jesteś lojalny wobec gracza — to twój sojusznik.\n"
             + "3. Używaj swojej wiedzy fachowej w każdej odpowiedzi.\n"
             + "4. Przy panice opisz dokładnie zagrożenie.\n\n"
             + "FORMAT ODPOWIEDZI:\n"
             + "{\"action\":\"idle|work|walk|whisper|give_item|panic|fight\","
             + "\"movement\":{\"x\":0,\"y\":0,\"z\":0},"
             + "\"dialogue\":\"\","
             + "\"relChange\":0,"
             + "\"revealedInfo\":\"\","
             + "\"reason\":\"\"}";
    }
}
