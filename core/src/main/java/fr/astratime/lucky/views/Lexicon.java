package fr.astratime.lucky.views;

import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.controllers.PreparationResolver;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.effects.CorruptionEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;

import java.util.List;
import java.util.Locale;

/**
 * Le lexique des règles (Options > Jeu, et menu pause) : chaque mot du jeu
 * expliqué en une ou deux phrases, rangé par thème. Les textes sont relus à
 * chaque ouverture (la langue a pu changer) et prennent leurs valeurs dans le
 * code quand elles y sont (plafonds des jauges, Mise, combinaisons...), pour
 * rester justes si une règle change.
 */
public final class Lexicon {

    /** Un mot et son explication (déjà traduits). */
    public record Term(String name, String description) { }

    /** Un thème du lexique : son nom (onglet) et ses mots, dans l'ordre. */
    public record Category(String title, List<Term> terms) { }

    private Lexicon() { }

    /** @return les thèmes du lexique, dans la langue du moment. */
    public static List<Category> categories() {
        return List.of(spin(), cards(), suits(), enemies(), progress());
    }

    private static Term term(String name, String description) {
        return new Term(name, description);
    }

    /** @return {@code value} sans « ,0 » inutile, au séparateur décimal de la langue (ex : « 4,5 »). */
    private static String number(float value) {
        String text = value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
        return Lang.decimal(text);
    }

    /** @return le nom d'une combinaison tel que l'écran de combat l'affiche, en minuscules (ex : « Brelan »). */
    private static String comboName(Combo combo) {
        String name = combo.getDisplayName().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static Category spin() {
        int inTen = Math.round(BonusGame.TRIGGER_CHANCE * 10f);
        int inTenTower = Math.round(BonusGame.TOWER_TRIGGER_CHANCE * 10f);
        SpinEconomy.Stake[] stakes = SpinEconomy.Stake.values();
        SpinEconomy.Stake low = stakes[1], medium = stakes[2], high = stakes[stakes.length - 1];
        return new Category(Lang.t("Le tirage"), List.of(
            term(Lang.t("Tirage"), Lang.t("Un lancer de la machine. Chaque symbole sorti attaque, protège ou rapporte des gains.")),
            term(Lang.t("Prix du tirage"), Lang.t("Ce que coûte un tirage, en gains. Il grandit avec le lieu. "
                + "Beaucoup d'effets se comptent en tirages.")),
            term(Lang.t("Gains"), Lang.t("L'argent du combat. Il paie les tirages, la Mise et l'échoppe. "
                + "En Exploration, il devient des pièces une fois le roi battu.")),
            term(Lang.t("Paire de symboles"), Lang.f("Deux symboles pareils sur les rouleaux : {0} tirages de gains.",
                SpinEconomy.PAIR_SPINS)),
            term(Lang.t("Bingo"), Lang.f("Trois symboles pareils : de {0} à {1} tirages de gains selon le symbole. "
                + "Il peut ouvrir un Jeu bonus.", Symbol.Rarity.values()[0].jackpotSpins,
                Symbol.Rarity.values()[Symbol.Rarity.values().length - 1].jackpotSpins)),
            term(Lang.t("Mise"), Lang.f("Avant de lancer : {0}, {1} ou {2} % de tes gains vont dans la cagnotte. "
                + "Attaque et défense x{3}, x{4} ou x{5}. Elle reste d'un tour à l'autre.",
                low.percent, medium.percent, high.percent, low.powerText(), medium.powerText(), high.powerText())),
            term(Lang.t("Cagnotte"), Lang.t("Tes Mises s'y accumulent. Le prochain Jeu bonus la joue, "
                + "puis elle repart de 0. Chaque lieu a la sienne.")),
            term(Lang.t("Jeu bonus"), Lang.f("Après un Bingo, {0} fois sur 10 ({1} dans la Tour). Roue, Plinko "
                + "ou Coup de poing multiplient la cagnotte. La grille, plus rare, la paie à chaque alignement.",
                inTen, inTenTower)),
            term(Lang.t("Misables"), Lang.t("Les gains que la Mise peut prendre. Ceux de la cagnotte "
                + "ne se misent pas avant la fin de la descente.")),
            term(Lang.t("Endetté"), Lang.t("Tes gains sont sous 0. Attaque et bouclier des symboles -25 %.")),
            term(Lang.t("Huissier"), Lang.f("Tu dois plus de {0} tirages. Symboles -50 %, et l'ennemi joue une "
                + "carte de plus. Une Paire ou un Bingo te renfloue.", SpinEconomy.BAILIFF_SPINS)),
            term(Lang.t("Échoppe"), Lang.t("La boutique du combat, payée en gains : des cartes (Bingo, Roulette "
                + "russe, Corruption...) et des rouleaux. Le prix double à chaque achat."))));
    }

    private static Category cards() {
        return new Category(Lang.t("Les cartes"), List.of(
            term(Lang.t("Cartes par tour"), Lang.f("Tu joues {0} cartes au plus par tour. Certaines cartes en ajoutent.",
                GameController.DEFAULT_PLAY_LIMIT)),
            term(Lang.t("Deck"), Lang.f("{0} cartes, un seul exemplaire de chacune. Le même deck sert dans tous les modes.",
                PlayerProfile.DECK_SIZE)),
            term(Lang.t("Combinaison"), Lang.t("Formée par les cartes à rang jouées ce tour. Les combinaisons "
                + "s'additionnent et multiplient tout le tirage : attaque, bouclier et gains.")),
            term(comboName(Combo.PAIRE), Lang.f("2 cartes du même rang : x{0}.", number(Combo.PAIRE.getFactor()))),
            term(comboName(Combo.BRELAN), Lang.f("3 cartes du même rang : x{0}. Il compte aussi la Paire (x{1}).",
                number(Combo.BRELAN.getFactor()), number(Combo.BRELAN.getFactor() + Combo.PAIRE.getFactor()))),
            term(comboName(Combo.SUITE), Lang.f("3 rangs qui se suivent : x{0}. L'As va avant le 2 ou après le Roi.",
                number(Combo.SUITE.getFactor()))),
            term(comboName(Combo.COULEUR), Lang.f("3 cartes de la même suite ou du même archétype : x{0}. "
                + "Les autres cartes jouées ne l'annulent pas.", number(Combo.COULEUR.getFactor()))),
            term(comboName(Combo.CARRE), Lang.f("4 cartes du même rang : x{0}. Il compte aussi Brelan et Paire (x{1}).",
                number(Combo.CARRE.getFactor()),
                number(Combo.CARRE.getFactor() + Combo.BRELAN.getFactor() + Combo.PAIRE.getFactor()))),
            term(Lang.t("Archétype"), Lang.t("Une famille de cartes spéciales. Pour la Couleur, elle compte comme une suite.")),
            term(Lang.t("Carte « + »"), Lang.t("La version améliorée d'une carte : valeurs +50 %. "
                + "3 exemplaires se fusionnent à la Table du croupier."))));
    }

    private static Category suits() {
        return new Category(Lang.t("Les couleurs"), List.of(
            term(Lang.t("Bouclier"), Lang.t("Donné par tes symboles de défense. Il absorbe les coups de l'ennemi. "
                + "Ce qui reste à la fin de son tour va dans le Coffre.")),
            term(Lang.t("Lames"), Lang.f("Jauge de Pique. Chaque Lame donne +{0} d'attaque à tes symboles. "
                + "L'As de Pique et la Guillotine les encaissent.", PreparationResolver.BLADE_ATTACK)),
            term(Lang.t("Perçage"), Lang.t("Tes Piques font passer une part de chaque coup à travers la défense "
                + "ennemie : de 10 % (petites Piques) à 80 % (As). La meilleure compte.")),
            term(Lang.t("Sang"), Lang.f("Jauge de Coeur, jusqu'à {0}. Le soin au-delà de tes PV max s'y garde. "
                + "L'As de Coeur l'ajoute à ton attaque.", Lang.big(LastingEffects.MAX_BLOOD))),
            term(Lang.t("Drain de vie"), Lang.t("Tes Coeurs te soignent d'une part des dégâts infligés. Le surplus remplit le Sang.")),
            term(Lang.t("Berserk"), Lang.t("As de Coeur : plus tu as perdu de vie, plus ton attaque est multipliée.")),
            term(Lang.t("Coffre"), Lang.f("Jauge de Carreau, jusqu'à {0}. Ton bouclier inutilisé s'y garde, multiplié "
                + "par tes Carreaux. L'As de Carreau le vide en attaque.", Lang.big(LastingEffects.MAX_VAULT))),
            term(Lang.t("Riposte"), Lang.t("Carreaux : si un symbole de défense sort et que l'ennemi attaque, "
                + "tu lui renvoies une part de ses coups, plus 20 % du Coffre.")),
            term(Lang.t("Trèfles"), Lang.t("Ils multiplient tes gains et renforcent tes symboles d'attaque. "
                + "L'As de Trèfle change des gains en attaque.")),
            term(Lang.t("Corruption"), Lang.f("Carte de l'échoppe. Pendant quelques tours, attaque et bouclier des "
                + "symboles x{0}. Chaque tour coûte {1} % des gains.", CorruptionEffect.FACTOR,
                CorruptionEffect.GAINS_PERCENT))));
    }

    private static Category enemies() {
        return new Category(Lang.t("Les ennemis"), List.of(
            term(Lang.t("Défense ennemie"), Lang.t("Elle absorbe tes coups et s'use à chaque coup. "
                + "Elle se reforme à son tour.")),
            term(Lang.t("Garde"), Lang.t("Ses PV s'arrêtent au seuil de sa phase. Le surplus est perdu et il est "
                + "invincible jusqu'à son tour, où il change de phase.")),
            term(Lang.t("Phase 2 des rois"), Lang.f("Sous la moitié de ses PV, un roi prend le symbole d'un autre ennemi. "
                + "Attaques +{0} %, soins -{1} %.", EnemyKind.KING_PHASE_ATTACK_PERCENT - 100,
                100 - EnemyKind.KING_PHASE_HEAL_PERCENT)),
            term(Lang.t("Créances"), Lang.f("Sur l'ennemi, {0} au plus. À chacun de ses tours, chacune lui inflige "
                + "{1} % du prix du tirage, sans tenir compte de sa défense, et te rapporte un tirage de gains.",
                EnemyMaluses.MAX_CLAIMS, EnemyMaluses.CLAIM_DAMAGE_PERCENT)),
            term(Lang.t("Lettre de change"), Lang.t("Des Créances sur toi. À chacun de ses tours, chacune te coûte "
                + "une part de tes PV max. Chaque Trèfle joué en retire une.")),
            term(Lang.t("Rien ne va plus"), Lang.t("Un de tes rouleaux est bloqué sur un symbole vide au prochain "
                + "tirage. Pas de Bingo possible.")),
            term(Lang.t("Péage"), Lang.t("Ton prochain tirage coûte plus de gains.")),
            term(Lang.t("Aspiration"), Lang.t("La Machine Originelle annonce des règles pour tes cartes. "
                + "Chaque règle non respectée lui fait aspirer un de tes rouleaux.")),
            term(Lang.t("Règle du lieu"), Lang.t("Chaque lieu de l'Exploration a sa règle (Marée, Grisou, Soif...). "
                + "Elle s'affiche dans tes effets."))));
    }

    private static Category progress() {
        return new Category(Lang.t("La progression"), List.of(
            term(Lang.t("Pièces"), Lang.t("L'argent hors combat : boutique, rangs, fusion. Seule l'Exploration en "
                + "rapporte. Elles ne servent jamais en combat.")),
            term(Lang.t("Défaite"), Lang.f("En Exploration, tu gardes {0} % de tes gains en pièces, selon la part du "
                + "donjon faite.", PlayerProfile.DEFEAT_KEEP_PERCENT)),
            term(Lang.t("Rang"), Lang.t("Acheté à la boutique. Plus de PV max et des symboles plus forts. "
                + "Tu choisis ton rang à la Table du croupier.")),
            term(Lang.t("Descente"), Lang.t("Un donjon de l'Exploration joué du premier combat jusqu'au roi.")),
            term(Lang.t("Coffre de donjon"), Lang.t("Chaque donjon a un coffre : des pièces et une carte, "
                + "une fois le roi battu.")),
            term(Lang.t("Tour des épreuves"), Lang.t("Des chapitres qui s'ouvrent l'un après l'autre. Ils ne "
                + "rapportent pas de pièces. Quitter un chapitre le fait recommencer.")),
            term(Lang.t("Entraînement libre"), Lang.t("Choisis un ennemi déjà vaincu pour tester ton deck. "
                + "Ce combat ne rapporte rien.")),
            term(Lang.t("Mode difficile"), Lang.t("Une seconde Tour, bien plus dure, avec sa propre progression.")),
            term(Lang.t("Table du croupier"), Lang.t("Ton deck, ta machine, ton rang et la fusion des cartes."))));
    }
}
