package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.i18n.Lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deck en cours de construction (onglet « Deck » de la Table du croupier) : une copie
 * du deck du joueur, modifiée carte par carte, puis enregistrée dans son
 * {@link PlayerProfile} une fois complète ({@link PlayerProfile#DECK_SIZE}
 * cartes exactement).
 */
public class DeckDraft {

    private final PlayerProfile        profile;
    private final Map<String, Integer> copies = new LinkedHashMap<>();

    /** Commence avec le deck actuel du joueur. */
    public DeckDraft(PlayerProfile profile) {
        this.profile = profile;
        copies.putAll(profile.getDeck());
    }

    /** @return le nombre d'exemplaires de la carte {@code id} dans le deck. */
    public int getCopies(String id) { return copies.getOrDefault(id, 0); }

    /** @return les cartes du deck et leur nombre d'exemplaires, par id. */
    public Map<String, Integer> getCopies() { return Collections.unmodifiableMap(copies); }

    /** @return le nombre de cartes du deck. */
    public int size() { return copies.values().stream().mapToInt(Integer::intValue).sum(); }

    /** @return {@code true} si le deck fait exactement {@link PlayerProfile#DECK_SIZE} cartes. */
    public boolean isComplete() { return size() == PlayerProfile.DECK_SIZE; }

    /** @return les exemplaires de la carte {@code id} qu'on peut mettre dans le deck (possédés, et au plus 3). */
    public int getMaxCopies(String id) { return Math.min(PlayerProfile.MAX_COPIES, profile.getOwnedCopies(id)); }

    /** @return pourquoi la carte {@code id} ne peut pas être ajoutée, ou {@code null} si elle peut l'être. */
    public String addProblem(String id) {
        if (size() >= PlayerProfile.DECK_SIZE) return Lang.f("Le deck est plein ({0} cartes)",
            PlayerProfile.DECK_SIZE);
        if (getCopies(id) >= PlayerProfile.MAX_COPIES) return Lang.f("Pas plus de {0} exemplaires",
            PlayerProfile.MAX_COPIES);
        if (getCopies(id) >= profile.getOwnedCopies(id)) return Lang.t("Tu n'en as pas d'autre exemplaire");
        return null;
    }

    /** Ajoute un exemplaire de la carte {@code id}. @return {@code true} s'il a été ajouté */
    public boolean add(String id) {
        if (addProblem(id) != null) return false;
        copies.merge(id, 1, Integer::sum);
        return true;
    }

    /** Retire un exemplaire de la carte {@code id}. @return {@code true} s'il y en avait un */
    public boolean remove(String id) {
        int count = getCopies(id);
        if (count <= 0) return false;
        if (count == 1) copies.remove(id);
        else copies.put(id, count - 1);
        return true;
    }

    /** Vide le deck. */
    public void clear() { copies.clear(); }

    /**
     * Remet le deck de départ. Une carte fusionnée (voir {@link PlayerProfile#upgradeCard(String)})
     * y est remplacée par sa version « + », tant qu'il y en a.
     */
    public void resetToStarter() {
        copies.clear();
        profile.getStarterDeck().forEach((id, count) -> {
            int kept = Math.min(count, getMaxCopies(id));
            if (kept > 0) copies.merge(id, kept, Integer::sum);
            String plus = Card.upgradedId(id);
            int upgraded = Math.min(count - kept, getMaxCopies(plus) - getCopies(plus));
            if (upgraded > 0) copies.merge(plus, upgraded, Integer::sum);
        });
    }

    /**
     * Après la fusion de la carte {@code id} : ses exemplaires qui ne sont plus
     * possédés quittent le deck, et sa version « + » en prend la place, tant
     * qu'il y en a. Le deck peut ne plus être complet.
     */
    public void afterUpgrade(String id) {
        int removed = getCopies(id) - getMaxCopies(id);
        if (removed <= 0) return;
        for (int i = 0; i < removed; i++) remove(id);
        String plus = Card.upgradedId(id);
        int added = Math.min(removed, getMaxCopies(plus) - getCopies(plus));
        if (added > 0) copies.merge(plus, added, Integer::sum);
    }

    /**
     * Enregistre le deck dans le profil du joueur.
     *
     * @return {@code true} s'il a été enregistré, {@code false} s'il n'est pas complet
     */
    public boolean save() {
        if (profile.deckProblem(copies) != null) return false;
        profile.setDeck(copies);
        return true;
    }
}
