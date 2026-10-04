package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Machine en cours de réglage (onglet « Rouleaux » de la Table du croupier) :
 * une copie de celle du joueur, modifiée rouleau par rouleau, enregistrée
 * seulement quand elle compte exactement {@link Symbol#MACHINE_SIZE} rouleaux.
 */
public class MachineDraft {

    private final PlayerProfile profile;
    private final List<Symbol>  reels;

    public MachineDraft(PlayerProfile profile) {
        this.profile = profile;
        this.reels   = new ArrayList<>(profile.getMachine());
    }

    /** @return les rouleaux de la machine, dans l'ordre où ils ont été placés. */
    public List<Symbol> getReels() { return Collections.unmodifiableList(reels); }

    /** @return {@code true} si {@code symbol} est dans la machine. */
    public boolean contains(Symbol symbol) { return reels.contains(symbol); }

    /** @return le nombre de rouleaux placés. */
    public int size() { return reels.size(); }

    /** @return {@code true} si la machine a exactement {@link Symbol#MACHINE_SIZE} rouleaux. */
    public boolean isComplete() { return reels.size() == Symbol.MACHINE_SIZE; }

    /** @return pourquoi {@code symbol} ne peut pas entrer dans la machine, ou {@code null} s'il le peut. */
    public String addProblem(Symbol symbol) {
        if (!profile.ownsReel(symbol)) return "Rouleau pas encore acheté";
        if (reels.contains(symbol)) return "Ce rouleau est déjà dans la machine";
        if (reels.size() >= Symbol.MACHINE_SIZE) return "La machine a déjà " + Symbol.MACHINE_SIZE + " rouleaux";
        return null;
    }

    /** Place {@code symbol} dans la machine. @return {@code false} si ce n'est pas possible (voir {@link #addProblem}) */
    public boolean add(Symbol symbol) {
        if (addProblem(symbol) != null) return false;
        reels.add(symbol);
        return true;
    }

    /** Retire {@code symbol} de la machine. @return {@code false} s'il n'y était pas */
    public boolean remove(Symbol symbol) { return reels.remove(symbol); }

    /** Remet les 11 rouleaux classiques. */
    public void resetToClassic() {
        reels.clear();
        reels.addAll(Symbol.classicReels());
    }

    /** Retire tous les rouleaux. */
    public void clear() { reels.clear(); }

    /** Enregistre la machine si elle est complète. @return {@code false} sinon */
    public boolean save() {
        if (!isComplete()) return false;
        profile.setMachine(reels);
        return true;
    }
}
