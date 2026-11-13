package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.BonusPot;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.i18n.Lang;

/**
 * Le combat secret contre le Croupier, ouvert depuis la Tour des épreuves quand le
 * Désert de Braise est vidé et le chapitre 6 terminé en mode difficile
 * ({@link fr.astratime.lucky.progress.PlayerProfile#isCroupierOpen()}).
 * Un seul combat, en trois phases (voir {@link EnemyKind#GRAND_CROUPIER}) ;
 * comme dans la Tour, il ne rapporte aucune pièce. Le gagner mène à la vraie fin du jeu.
 */
public class CroupierRun implements CombatRun {

    /** Cagnotte du Jeu bonus du combat, effacée après une défaite. */
    private final BonusPot pot = new BonusPot();

    /** @return la cagnotte du Jeu bonus du combat. */
    public BonusPot getPot() { return pot; }

    @Override
    public EnemyKind getEnemy() { return EnemyKind.GRAND_CROUPIER; }

    @Override
    public int getStage() { return 0; }

    @Override
    public int getStageCount() { return 1; }

    @Override
    public Next win() { return Next.CLEARED; }

    @Override
    public void restart() { pot.take(); }

    @Override
    public String getLabel() { return Lang.t("Le Croupier"); }

    @Override
    public String getEnding() { return Lang.t("La table est à toi. Le Croupier distribue pour la dernière fois."); }
}
