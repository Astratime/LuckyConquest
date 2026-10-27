package fr.astratime.lucky.entities;

import java.util.function.DoubleConsumer;

/**
 * La cagnotte du Jeu bonus (choix d'Astra, 2026-10-27) : les gains misés
 * ({@link SpinEconomy.Stake}) s'y accumulent. Au prochain Jeu bonus, chaque
 * alignement paie sa valeur plus toute la cagnotte, puis elle repart de 0.
 * <ul>
 *   <li>Exploration : une cagnotte par lieu, partagée par ses donjons et gardée
 *       d'une partie à l'autre (voir {@link fr.astratime.lucky.progress.PlayerProfile#getPot}) ;</li>
 *   <li>Entraînement : sa propre cagnotte, gardée aussi ;</li>
 *   <li>Tour des épreuves : une cagnotte par ascension d'un chapitre, partagée
 *       par ses combats et effacée à la fin du chapitre.</li>
 * </ul>
 */
public final class BonusPot {

    private double amount;
    /** Prévenu à chaque changement (enregistrement du profil), ou {@code null}. */
    private final DoubleConsumer onChange;

    /** Une cagnotte vide, qui n'est enregistrée nulle part. */
    public BonusPot() { this(0, null); }

    /**
     * @param amount   montant de départ
     * @param onChange prévenu du nouveau montant à chaque changement, ou {@code null}
     */
    public BonusPot(double amount, DoubleConsumer onChange) {
        this.amount   = Math.max(0, amount);
        this.onChange = onChange;
    }

    /** @return ce que contient la cagnotte. */
    public double getAmount() { return amount; }

    /** Ajoute {@code gains} (une Mise) à la cagnotte. */
    public void add(double gains) {
        if (gains <= 0) return;
        amount = BigNumber.add(amount, gains);
        changed();
    }

    /** @return tout ce que contenait la cagnotte, qui repart de 0 (Jeu bonus, fin d'un chapitre de la Tour). */
    public double take() {
        double taken = amount;
        amount = 0;
        if (taken > 0) changed();
        return taken;
    }

    private void changed() {
        if (onChange != null) onChange.accept(amount);
    }
}
