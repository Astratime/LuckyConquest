package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.i18n.Lang;

/**
 * Symboles de la machine à sous de l'ennemi. Leur effet de base est renforcé
 * par les cartes que l'ennemi joue (voir {@link EnemyCards}).
 */
public enum EnemySymbol {

    /** Attaque le joueur. */
    SWORD("ÉPÉE"),
    /** Ajoute de la défense à l'ennemi, pendant le prochain tour du joueur. */
    SHIELD("BOUCLIER"),
    /** Rend des PV à l'ennemi, en pourcentage de ses PV max. */
    POTION("POTION"),
    /** Renvoie au joueur une part des dégâts qu'il inflige pendant son prochain tour. */
    THORNS("ÉPINES"),
    /** Mord le joueur et rend à l'ennemi une part de ses PV max pour chaque PV volé. */
    FANG("CROC"),
    /** Toutes ses attaques frappent plus fort, jusqu'à la fin du combat. */
    RAGE("RAGE"),
    /** Il triche : les jauges du joueur (Coffre, Sang, Lames) perdent une part de leur contenu. */
    LOADED_DIE("DÉ PIPÉ"),
    /** Il prend une part des gains du joueur ; ce qu'il vole renforce sa prochaine attaque. */
    INTEREST("INTÉRÊTS"),
    /** Il lance une roulette : rouge, ses attaques du tour doublent ; noir, ses Boucliers ; zéro, les deux. */
    ZERO("ZÉRO"),
    /** Il rejoue la dernière carte jouée par le joueur, à moitié de sa force. */
    MIRROR("REFLET"),
    /** Son compte à rebours avance ; arrivé au bout, il explose. */
    HOURGLASS("SABLIER"),
    /** Il fait tapis : à son tour suivant, sa mise double (ses attaques avec), sauf s'il est touché d'ici là. */
    ALL_IN("TAPIS"),
    /** Il ronge une carte : au prochain tour du joueur, une carte de sa main part à la défausse. */
    NIBBLE("GRIGNOTAGE"),
    /** Au prochain tirage du joueur, un de ses rouleaux tourne deux fois et garde le pire résultat. */
    DRUNK("IVRESSE"),
    /** Au prochain tour du joueur, ses cartes en main sont faces cachées. */
    BLIND("AVEUGLEMENT"),
    /** Il vole la meilleure carte du joueur (pour tout le combat) et la joue contre lui. */
    BOARDING("ABORDAGE"),
    /** Au prochain tirage du joueur, ses symboles de gain deviennent des pierres : ils ne rapportent rien. */
    NUGGET("PÉPITE"),
    /** Une attaque qui traverse le bouclier du joueur. */
    DRILL("FORAGE"),
    /** Son attaque grandit, pour tout le combat (sans limite). */
    ANVIL("ENCLUME"),
    /** Au début du prochain tour du joueur, une carte au hasard de sa main est jouée d'office. */
    SONG("CHANT"),
    /** Il dévore une part des gains du joueur et se soigne d'autant. */
    BANK_BITE("MORSURE"),
    /** Une part des gains du joueur devient fausse : elle disparaît au prochain tirage, si elle n'est pas dépensée. */
    FAKE_MONEY("FAUSSE MONNAIE"),
    /** Il annonce un symbole : s'il sort au prochain tirage du joueur, ses attaques du tour suivant triplent. */
    PREDICTION("PRÉDICTION"),
    /** Chacun tire une carte au hasard de son deck : la plus haute frappe l'autre. */
    DUEL("DUEL"),
    /** Au prochain tour du joueur, chaque carte jouée lui coûte une part de ses gains, puis une somme fixe. */
    TAX("TAXE"),
    /** Une règle du jeu change pendant deux tours : sans combinaisons, sans Bingo, ou rouleaux qui tournent deux fois. */
    NEW_RULE("NOUVELLE RÈGLE"),
    /** Il confisque une carte du deck du joueur jusqu'à la fin du combat (plus s'il a gros gains). */
    FRISK("FOUILLE"),
    /** Tout ou rien : plus riche que lui, le joueur perd tous ses gains ; sinon, lui perd une part de ses PV. */
    BANKRUPTCY("FAILLITE"),
    /** Croupier en chef : à son prochain tirage, un rouleau du joueur est bloqué sur un symbole vide. */
    NOTHING_GOES("RIEN NE VA PLUS"),
    /** Croupier en chef : il ramasse une part des gains du joueur et en fait de la défense. */
    RAKE("RÂTEAU"),
    /** Portier : il garde la meilleure carte du joueur jusqu'à la fin de son tour suivant. */
    CLOAKROOM("VESTIAIRE"),
    /** Portier : un coup qui ignore la moitié du bouclier du joueur. */
    BOUNCER("VIDEUR"),
    /** Maison (la Façade) : un Bouclier, et sa défense ne s'use pas sous les coups jusqu'à son tour. */
    LOAD_BEARING("MUR PORTEUR"),
    /** Maison (le Coffre) : il se soigne et vole une part des gains. */
    SAFE("COFFRE-FORT"),
    /** Maison (la Salle de jeu) : un coup, et les gains du prochain tirage du joueur sont réduits. */
    HOUSE_EDGE("AVANTAGE DE LA MAISON"),
    /** Machine Originelle : chaque Engrenage renforce ses autres symboles du tirage. */
    GEAR("ENGRENAGE"),
    /** Machine Originelle : au prochain tirage du joueur, son meilleur rouleau devient son pire. */
    SHORT_CIRCUIT("COURT-CIRCUIT"),
    /** Machine Originelle : la Comète Dorée tombe, une part des PV max du joueur qui ignore son bouclier. */
    GOLDEN_COMET("COMÈTE"),
    /** Machine Originelle : une règle ; au prochain tirage, un rouleau aspiré si les cartes jouées ne la respectent pas. */
    ASPIRATION("ASPIRATION"),
    /** Golem d'or : sa peau d'or tient un tour, même sous la moitié de ses PV. */
    PLATING("PLACAGE"),
    /** Golem d'or : un coup aussi fort que sa défense du moment. */
    INGOT("LINGOT"),
    /** Cœur de la Montagne : un coup, et le joueur pioche une carte de moins au prochain tour. */
    ROCKFALL("ÉBOULEMENT"),
    /** Cœur de la Montagne : il se soigne, et sa peau d'or revient un tour. */
    HEARTBEAT("BATTEMENT"),
    /** Bandit manchot : il avale une part des gains ; à 3 pièces, son prochain tirage est un Jackpot. */
    SWALLOWED_COIN("PIÈCE AVALÉE"),
    /** Bandit manchot : il relance son pire rouleau, pour viser le Jackpot. */
    ARMED_ARM("BRAS ARMÉ"),
    /** Fête foraine : deux cartes de la main échangées avec la pioche, au prochain tour. */
    JUGGLE("JONGLAGE"),
    /** Il annonce une famille : si le joueur n'en joue aucune, son prochain coup frappe x2. */
    ACT("NUMÉRO"),
    /** Le prochain tirage du joueur donne moins d'attaque. */
    SCARE("FRAYEUR"),
    /** Un rouleau du joueur passe dans le noir : il prend un symbole au hasard. */
    TUNNEL("TUNNEL"),
    /** Le meilleur rouleau du joueur est relancé après son tirage. */
    TURNSTILE("TOURNIQUET"),
    /** Sa roue tombe sur une case : deux Épées, deux Boucliers, une Potion ou rien. */
    BIG_WHEEL("GRAND TOUR"),
    /** Les Paires du prochain tirage du joueur ne rapportent rien. */
    RIGGED_PRIZE("LOT TRUQUÉ"),
    /** Sans Paire ni Bingo au prochain tirage, les dégâts du joueur sont divisés par 2. */
    RIGGED_GAME("JEU TRUQUÉ"),
    /** Désert : le prochain tirage coûte un tirage de plus. */
    TOLL("PÉAGE"),
    /** Il pose des Créances sur le joueur. */
    BILL("LETTRE DE CHANGE"),
    /** Une carte de la main est ligotée : elle ne se joue pas pendant 2 tours. */
    WRAP("BANDELETTES"),
    /** Venin : PV perdus à chaque tour du joueur, pendant 3 tours. */
    VENOM("VENIN"),
    /** Un rouleau du joueur montre un mirage : il ne compte pas. */
    MIRAGE("MIRAGE"),
    /** Le bouclier du joueur fond de moitié. */
    BURN("BRÛLURE"),
    /** Un souffle qui passe le bouclier et brûle une carte de la main. */
    FIRE_BREATH("SOUFFLE DE FEU"),
    /** Lumière du bingo (rouleau du joueur) : ce rouleau de l'ennemi s'illumine, ne fait rien, et il perd des PV. */
    BINGO_LIGHT("LUMIÈRE DU BINGO"),
    /** Rapide de Minuit : sa chaudière monte d'un cran ; au 3e, elle explose (gros coup). Tes gros coups la font baisser. */
    PRESSURE("PRESSION"),
    /** Une carte au hasard de ta main disparaît dans sa poche jusqu'à la fin de ton prochain tour. */
    PICKPOCKET("VOL À LA TIRE"),
    /** Il se soigne et reforme sa défense. */
    BANQUET("BANQUET"),
    /** La prochaine carte que tu joues s'endort : elle ne fait rien. */
    LULLABY("BERCEUSE"),
    /** Si tu l'as peu blessé depuis son dernier tour, il te prend ton bouclier ; sinon, il tire. */
    HOLDUP("BRAQUAGE"),
    /** Palace des Neiges : il ferme à clé ta plus forte carte en main, jusqu'à la fin de ton prochain tour. */
    KEY("CLÉ"),
    /** Il gèle un de tes rouleaux : au prochain tirage, il affiche le même symbole. */
    FREEZE("GEL"),
    /** Il frappe, et chaque Avalanche grossit la suivante ; un gros coup de ta part la fait fondre. */
    AVALANCHE("AVALANCHE"),
    /** Il se soigne et reforme sa défense, d'un seul symbole. */
    CHAMPAGNE("CHAMPAGNE GLACÉ"),
    /** Temple du Destin : le glas sonne ; un de tes rouleaux est bloqué à ton prochain tirage. */
    KNELL("GLAS"),
    /** Un de tes rouleaux est lié au sien : à ton prochain tirage, il montre le symbole qu'il choisit (le plus rare de ta machine). */
    FATE_THREAD("FIL DU DESTIN"),
    /** Il lance deux dés et frappe ; un double double ses attaques de ce tour. */
    DOUBLE_SIX("DOUBLE SIX"),
    /** Il te renvoie tes Bingos : chaque Bingo de ce combat, depuis son dernier Karma, frappe en plus. */
    KARMA("KARMA"),
    /** Manoir hanté : il prend une part de ta jauge de Sang et s'en soigne ; sans Sang, il frappe. */
    BLOOD_PACT("PACTE DE SANG"),
    /** Au prochain tour, ta meilleure carte piochée retourne sous ta pioche. */
    CURSE("MALÉDICTION"),
    /** Il se drape dans son linceul : la première fois qu'il tombe, il se relève avec une part de ses PV. */
    SHROUD("LINCEUL"),
    /** Au début de ton prochain tour, il choisit ta plus faible carte et tu la joues d'office. */
    STRINGS("FILS"),
    /** Guerre des Tables : il appelle un soldat ; ses soldats tirent à chacun de ses tours. Tes gros coups les mettent à terre. */
    RALLY("RASSEMBLEMENT"),
    /** Guerre des Tables : il charge une part de tes gains dans son canon et tire, à travers une part de ton bouclier. */
    CANNON("CANON À JETONS"),
    /** Guerre des Tables : elle a lu tes plans ; un Bouclier par carte que tu as jouée à ton dernier tour. */
    SPY("ESPIONNAGE"),
    /** Guerre des Tables : il touche sa prime ; s'il a peu souffert depuis son dernier tour, il frappe bien plus fort. */
    CONTRACT("CONTRAT"),
    /** Guerre des Tables : il mise tout ; ses attaques du tour doublent, mais il n'a plus de défense jusqu'à son tour suivant. */
    ALL_OUT("VA-TOUT");

    /** Dégâts de base d'une Épée. */
    public static final int SWORD_DAMAGE   = 20;
    /** Défense de base d'un Bouclier. */
    public static final int SHIELD_DEFENSE = 100;
    /** Soin de base d'une Potion, en pourcentage des PV max de l'ennemi. */
    public static final int POTION_PERCENT = 30;
    /** Part des dégâts du joueur renvoyée par chaque Épines, en pourcentage. */
    public static final int THORNS_PERCENT = 2;
    /** Dégâts de base d'un Croc. */
    public static final int FANG_DAMAGE    = 10;
    /** Soin d'un Croc, en pourcentage des PV max de l'ennemi, pour chaque PV volé au joueur. */
    public static final int FANG_DRAIN     = 1;
    /** Attaque ajoutée par chaque Rage, jusqu'à la fin du combat. */
    public static final int RAGE_ATTACK    = 3;
    /** La Rage ne dépasse pas ce bonus d'attaque. */
    public static final int RAGE_MAX       = 30;
    /** Les Épines ne renvoient pas plus que ces dégâts en un tour. */
    public static final int THORNS_MAX     = 20;
    /** Part des jauges du joueur vidée par chaque Dé pipé, en pourcentage. */
    public static final int DIE_PERCENT    = 25;
    /** Part des gains du joueur prise par chaque Intérêts, en pourcentage. */
    public static final int INTEREST_PERCENT = 5;
    /**
     * Points ajoutés à chaque vol de gains en % (Intérêts, Morsure bancaire, et les
     * constantes déjà relevées : Râteau, Coffre, Pièce avalée) : choix d'Astra (2026-10-27),
     * la Mise mettant une part des gains à l'abri dans la cagnotte.
     */
    public static final int THEFT_BONUS_PERCENT = 5;
    /** Gains volés pour chaque point d'attaque ajouté par les Intérêts. */
    public static final int INTEREST_PER_ATTACK = 100;
    /** Les Intérêts n'ajoutent pas plus que cette attaque. */
    public static final int INTEREST_MAX   = 25;
    /** Chances sur 37 que la roulette du Zéro tombe sur le zéro (le reste : moitié rouge, moitié noir). */
    public static final int ZERO_POCKETS   = 5;
    /** Tours de Sablier avant l'explosion. */
    public static final int HOURGLASS_MAX  = 5;
    /** Dégâts de l'explosion du Sablier (plus sa Rage), à travers le bouclier du joueur. */
    public static final int HOURGLASS_DAMAGE = 35;
    /** Un coup du joueur d'au moins cette part de ses PV max (en pour mille) fait reculer son Sablier. */
    public static final int HOURGLASS_HIT_PER_MILLE = 10;
    /** Le Tapis ne monte pas au-delà de cette mise (multiplicateur de ses attaques). */
    public static final int ALL_IN_MAX     = 4;
    /** Attaque ajoutée par chaque Enclume, jusqu'à la fin du combat (sans limite). */
    public static final int ANVIL_ATTACK   = 5;
    /** Part des gains du joueur dévorée par chaque Morsure de la banque, en pourcentage. */
    public static final int BANK_BITE_PERCENT = 10;
    /**
     * Morsure : soin au plus, en % de ses PV max, à chaque Morsure (2026-10-24 : avec de gros gains, le
     * soin dépassait la limite d'un int et devenait négatif, la Morsure blessait le requin et les Coffres
     * devenaient le donjon le plus rentable du jeu).
     */
    public static final int BANK_BITE_HEAL_MAX = 3;
    /** Les trois rouleaux de l'ennemi identiques (Jackpot) : ses attaques et ses Boucliers du tour sont multipliés d'autant. */
    public static final int JACKPOT_FACTOR = 5;

    /** Part des gains du joueur rendue fausse par chaque Fausse monnaie, en pourcentage (avant sa force). */
    public static final int FAKE_PERCENT = 5;
    /** Ses attaques sont multipliées d'autant quand sa Prédiction sort sur les rouleaux du joueur. */
    public static final int PREDICTION_FACTOR = 3;
    /** Duel gagné par le joueur : l'ennemi perd cette part de ses PV max par rang de la carte, en pour mille. */
    public static final int DUEL_PER_MILLE_PER_RANK = 2;
    /** Part des gains retirée par la Taxe à chaque carte jouée, en pourcentage (fixée par Astra, sans sa force). */
    public static final int TAX_PERCENT = 20;
    /** Gains retirés en plus par la Taxe à chaque carte jouée, même à sec : les gains peuvent passer sous zéro. */
    public static final int TAX_FLAT = 1_000;
    /** Tours pendant lesquels vaut la Nouvelle règle. */
    public static final int NEW_RULE_TURNS = 2;
    /** Tours du joueur pendant lesquels une carte volée par l'Abordage reste absente, avant de revenir dans son deck. */
    public static final int BOARDING_TURNS = 2;
    /** Une Fouille confisque une carte de plus par tranche de ces gains du joueur. */
    public static final int FRISK_GAINS_STEP = 100_000;
    /** Une Fouille ne confisque pas plus de cartes. */
    public static final int FRISK_MAX = 3;
    /** Fortune de la Banqueroute, en pourcentage de ses PV restants (40 %, choix d'Astra ; sa Saisie la baisse). */
    public static final int BANKRUPTCY_FORTUNE_PERCENT = 40;
    /** PV perdus par la Banqueroute quand sa Faillite tourne mal, en pourcentage de ses PV restants. */
    public static final int BANKRUPTCY_HP_PERCENT = 20;

    /** Râteau : part des gains ramassée, en %. */
    public static final int RAKE_PERCENT = 15; // 10, +5 points avec la cagnotte (Astra, 2026-10-27)
    /** Le Râteau ne donne pas plus de défense que ce nombre de ses Boucliers. */
    public static final int RAKE_MAX_SHIELDS = 5;
    /** Tours du joueur que manque une carte gardée au Vestiaire. */
    public static final int CLOAKROOM_TURNS = 1;
    /** Part du bouclier du joueur ignorée par le Videur, en %. */
    public static final int BOUNCER_PIERCE_PERCENT = 50;
    /** Coffre-fort : soin, en % de ses PV max, et part des gains volée, en %. */
    public static final int SAFE_HEAL_PERCENT = 5, SAFE_STEAL_PERCENT = 10; // vol 5, +5 points (Astra, 2026-10-27)
    /** Avantage de la maison : gains du prochain tirage du joueur en moins, en %. */
    public static final int HOUSE_EDGE_PERCENT = 10;
    /** Engrenage : force en plus de ses autres symboles du tirage, en %. */
    public static final int GEAR_PERCENT = 25;
    /** Comète Dorée : dégâts en % des PV max du joueur, à travers son bouclier. */
    public static final int GOLDEN_COMET_PERCENT = 5;
    /** Tours que tient la peau d'or d'un Placage ou d'un Battement. */
    public static final int PLATING_TURNS = 1;
    /** Battement : soin, en % de ses PV max. */
    public static final int HEARTBEAT_PERCENT = 5; // 10 avant le plafond de 5 % (Astra, 2026-11-01)
    /** Pièce avalée : part des gains prise, en %, et pièces qu'il lui faut pour un Jackpot. */
    public static final int COIN_PERCENT = 10, COINS_FOR_JACKPOT = 3; // 5, +5 points (Astra, 2026-10-27)
    /** Jackpot originel : trois symboles pareils sur les rouleaux de la Machine, et ce symbole agit autant de fois. */
    public static final int ORIGINAL_JACKPOT_COUNT = 3, ORIGINAL_JACKPOT_ACTS = 5;
    /** Tours du joueur pendant lesquels la Machine garde le rouleau volé en phase 2 (choix d'Astra). */
    public static final int MACHINE_THEFT_TURNS = 3;

    /** Jonglage : cartes de la main échangées avec la pioche. */
    public static final int JUGGLE_CARDS = 2;
    /** Numéro : son prochain coup est multiplié d'autant si le joueur ne joue pas la famille annoncée. */
    public static final int ACT_FACTOR = 2;
    /** Frayeur : attaque du prochain tirage en moins, en %, et Frayeurs cumulées au plus. */
    public static final int SCARE_PERCENT = 30, SCARE_MAX = 2;
    /** Jeu truqué : les dégâts du joueur sont divisés d'autant sans Paire ni Bingo. */
    public static final int RIGGED_GAME_DIVISOR = 2;
    /** Péage : coûts de tirage pris en plus, par Péage. */
    public static final int TOLL_SPINS = 1;
    /** Lettre de change : Créances posées sur le joueur, au plus, et PV perdus par Créance (pour mille des PV max). */
    public static final int BILL_MAX = 10, BILL_PER_MILLE = 5, BILL_CLAIMS = 2;
    /** Bandelettes : tours pendant lesquels la carte ligotée ne se joue pas. */
    public static final int WRAP_TURNS = 2;
    /** Venin : PV perdus à chaque tour du joueur (en % des PV max), tours, et Venins en même temps au plus. */
    public static final int VENOM_PERCENT = 1, VENOM_TURNS = 3, VENOM_MAX = 3;
    /** Brûlure : part du bouclier du joueur qui fond, en %. */
    public static final int BURN_PERCENT = 50;
    /** Sarcophage du Pharaon : soin en % de ses PV max quand il s'y enferme. */
    public static final int SARCOPHAGUS_HEAL_PERCENT = 5; // 10 avant le plafond de 5 % (Astra, 2026-11-01)

    /** Pression : crans avant l'explosion, force de l'explosion (en coups d'Épée), et coup qui fait baisser la Pression (pour mille de ses PV max). */
    public static final int PRESSURE_MAX = 3, PRESSURE_FACTOR = 3, PRESSURE_HIT_PER_MILLE = 15;
    /** Vol à la tire : tours du joueur que manque la carte volée. */
    public static final int PICKPOCKET_TURNS = 1;
    /** Banquet : soin, en % de ses PV max. */
    public static final int BANQUET_HEAL_PERCENT = 4;
    /** Braquage : il te prend ton bouclier si tu lui as fait moins que cette part de ses PV max depuis son dernier tour (en %). */
    public static final int HOLDUP_PERCENT = 3;
    /** Braquage : le bouclier volé ne lui donne pas plus que ce nombre de ses Boucliers. */
    public static final int HOLDUP_MAX_SHIELDS = 4;

    /** Clé : tours du joueur que reste fermée la carte. */
    public static final int KEY_TURNS = 1;
    /** Gel : tirages que reste gelé le rouleau. */
    public static final int FREEZE_TURNS = 1;
    /** Avalanche : chaque couche de neige ajoute ce % à ses coups ; au plus tant de couches ; un coup d'au moins
     * cette part de ses PV max (en %) la fait fondre. */
    public static final int AVALANCHE_STEP_PERCENT = 50, AVALANCHE_MAX = 4, AVALANCHE_HIT_PERCENT = 2;
    /** Champagne glacé : soin, en % de ses PV max. */
    public static final int CHAMPAGNE_HEAL_PERCENT = 3;

    /** Glas : rouleaux du joueur bloqués à son prochain tirage. */
    public static final int KNELL_REELS = 1;
    /** Fil du destin : tours du joueur où son rouleau lié montre le symbole choisi (le prochain tirage). */
    public static final int FATE_THREAD_TURNS = 2;
    /** Double six : faces des dés, et multiplicateur de ses attaques du tour sur un double. */
    public static final int DIE_FACES = 6, DOUBLE_FACTOR = 2;
    /** Karma : Bingos du joueur gardés au plus, et coups d'Épée renvoyés par Bingo. */
    public static final int KARMA_MAX = 5, KARMA_FACTOR = 2;

    /** Pacte de sang : part de ta jauge de Sang qu'il prend (en %), et son soin s'il en a pris (en % de ses PV max). */
    public static final int BLOOD_PACT_PERCENT = 40, BLOOD_PACT_HEAL_PERCENT = 3;
    /** Linceul : PV avec lesquels il se relève, en % de ses PV max. */
    public static final int SHROUD_RISE_PERCENT = 20;

    /** Rassemblement : soldats au plus, force du tir de chaque soldat (en % d'une Épée), et coup qui en met un à terre (en % de ses PV max). */
    public static final int RALLY_MAX = 3, RALLY_SHOT_PERCENT = 50, RALLY_HIT_PERCENT = 2;
    /** Canon à jetons : part de tes gains chargée (en %), force du tir chargé (en coups d'Épée), et part de ton bouclier ignorée (en %). */
    public static final int CANNON_PERCENT = 10, CANNON_FACTOR = 2, CANNON_PIERCE_PERCENT = 50;
    /** Espionnage : Boucliers au plus (un par carte que tu as jouée à ton dernier tour). */
    public static final int SPY_MAX = 4;
    /** Contrat : il touche sa prime si tu lui as fait moins que cette part de ses PV max depuis son dernier tour (en %), et frappe d'autant de coups d'Épée. */
    public static final int CONTRACT_PERCENT = 3, CONTRACT_FACTOR = 3;
    /** Va-tout : ses attaques du tour sont multipliées d'autant (une fois par tour, même avec deux Va-tout). */
    public static final int ALL_OUT_FACTOR = 2;

    private final String displayName;

    EnemySymbol(String displayName) {
        this.displayName = displayName;
    }

    /** @return le nom affiché du symbole (ex : "ÉPÉE"). */
    public String getDisplayName() { return Lang.t(displayName); }

    /**
     * @return {@code true} pour un effet spécial (tout sauf l'Épée, le Bouclier et la
     *         Potion) : celui qu'annule un Tour de passe-passe
     */
    public boolean isSpecial() { return this != SWORD && this != SHIELD && this != POTION; }

    /**
     * @return la description de l'effet de base du symbole chez l'ennemi
     *         {@code kind}, en mode difficile si {@code hard} (renforcé par sa force ; ses Potions et ses Crocs
     *         soignent selon sa force de soin)
     */
    public String getDescription(EnemyKind kind, Difficulty hard) {
        return switch (this) {
            case SWORD  -> Lang.f("Épée : attaque de {0}", kind.hurt(kind.swordDamage(), hard));
            case SHIELD -> Lang.f("Bouclier : défense +{0} pendant le tour suivant",
                kind.empowered(kind.shieldDefense(), hard));
            case POTION -> Lang.f("Potion : soigne {0} % des PV max", percent(EnemyKind.cappedHeal(kind.potionPercent(0, hard))));
            case THORNS -> Lang.f("Épines : renvoie {0} % de tes dégâts. Au début de son tour", kind.thornsPercent(hard));
            case FANG   -> Lang.f("Croc : mord de {0}. Chaque PV volé lui rend {1} % de ses PV max, {2} % au plus",
                kind.hurt(FANG_DAMAGE, hard), percent(kind.drainPercent(hard)), EnemyKind.MAX_HEAL_PERCENT);
            case RAGE   -> Lang.f("Rage : attaque +{0}. Jusqu'à la fin du combat", kind.hurt(RAGE_ATTACK, hard));
            case LOADED_DIE -> Lang.f("Dé pipé : tes jauges (Coffre, Sang, Lames) perdent {0} %", kind.diePercent(hard));
            case INTEREST -> Lang.f("Intérêts : il prend {0} % de tes gains. Attaque +1 par {1} volés, au prochain coup. Sans gains, "
                + "il mord",
                kind.interestPercent(hard), INTEREST_PER_ATTACK);
            case ZERO   -> Lang.t("Zéro : rouge, ses attaques doublent. Noir, ses Boucliers. Zéro, les deux");
            case MIRROR -> Lang.t("Reflet : il rejoue ta dernière carte. À moitié de sa force");
            case HOURGLASS -> Lang.f("Sablier : +1. À {0}, il explose ({1}), à travers ton bouclier. Tes gros coups le font reculer",
                HOURGLASS_MAX, kind.hurt(HOURGLASS_DAMAGE, hard));
            case ALL_IN -> Lang.t("Tapis : à son prochain tour, ses attaques doublent. Touche-le avant pour l'annuler");
            case NIBBLE -> Lang.t("Grignotage : il ronge une carte. Au prochain tour, une carte de ta main part à la défausse");
            case DRUNK  -> Lang.t("Ivresse : à ton prochain tirage, un rouleau tourne deux fois et garde le pire résultat");
            case BLIND  -> Lang.t("Aveuglement : au prochain tour, une carte de ta main est face cachée. Ils se cumulent");
            case BOARDING -> Lang.f("Abordage : il vole ta meilleure carte en main pendant {0} tours. Il la joue contre toi",
                BOARDING_TURNS);
            case NUGGET -> Lang.t("Pépite : à ton prochain tirage, tes symboles de gain deviennent des pierres. Ils ne rapportent "
                + "rien");
            case DRILL  -> Lang.f("Forage : attaque de {0}. Traverse ton bouclier",
                kind.hurt(kind.swordDamage(), hard));
            case ANVIL  -> Lang.f("Enclume : attaque +{0}. Jusqu'à la fin du combat, sans limite",
                kind.hurt(ANVIL_ATTACK, hard));
            case SONG   -> Lang.t("Chant : au début de ton prochain tour, une carte au hasard de ta main est jouée d'office");
            case BANK_BITE -> Lang.f("Morsure : il dévore {0} % de tes gains et se soigne d'autant ({1} % de ses PV max au plus). "
                + "Sans gains, il mord", kind.bankBitePercent(hard), BANK_BITE_HEAL_MAX);
            case FAKE_MONEY -> Lang.f("Fausse monnaie : {0} % de tes gains deviennent faux. Dépense-les avant ton tirage, ou ils "
                + "disparaissent",
                kind.fakePercent(hard));
            case PREDICTION -> Lang.t("Prédiction : il annonce un de tes symboles. S'il sort à ton tirage, ses attaques du tour "
                + "triplent");
            case DUEL -> Lang.t("Duel : chacun tire une carte de son deck. La plus haute frappe l'autre. Tes As et tes figures "
                + "comptent");
            case TAX -> Lang.f("Taxe : au prochain tour, chaque carte jouée te coûte {0} % de tes gains, puis {1}. Tes gains "
                + "peuvent passer sous zéro",
                TAX_PERCENT, TAX_FLAT);
            case NEW_RULE -> Lang.f("Nouvelle règle : pendant {0} tours, plus de combinaisons, ou plus de Bingo, ou tes rouleaux "
                + "tournent deux fois (le pire reste)",
                NEW_RULE_TURNS);
            case FRISK -> Lang.f("Fouille : il confisque une carte de ton deck jusqu'à la fin du combat. Une de plus par {0} gains",
                FRISK_GAINS_STEP);
            case BANKRUPTCY -> Lang.f("Faillite : si tes gains dépassent sa fortune ({0} % de ses PV restants), il te prend tout. "
                + "Sinon, il perd {1} % de ses PV restants",
                BANKRUPTCY_FORTUNE_PERCENT, BANKRUPTCY_HP_PERCENT);
            case NOTHING_GOES -> Lang.t("Rien ne va plus : à ton prochain tirage, un de tes rouleaux est bloqué sur un symbole vide");
            case RAKE -> Lang.f("Râteau : il ramasse {0} % de tes gains et en fait de la défense. Sans gains, il frappe",
                RAKE_PERCENT);
            case CLOAKROOM -> Lang.t("Vestiaire : il garde ta meilleure carte jusqu'à la fin de ton prochain tour");
            case BOUNCER -> Lang.f("Videur : attaque de {0}. Ignore la moitié de ton bouclier", kind.hurt(kind.swordDamage(), hard));
            case LOAD_BEARING -> Lang.t("Mur porteur : Bouclier, et sa défense ne s'use pas sous tes coups jusqu'à son tour");
            case SAFE -> Lang.f("Coffre-fort : il se soigne de {0} % de ses PV max et te vole {1} % de tes gains",
                SAFE_HEAL_PERCENT, SAFE_STEAL_PERCENT);
            case HOUSE_EDGE -> Lang.f("Avantage de la maison : attaque de {0}. Tes gains du prochain tirage baissent de {1} %",
                kind.hurt(kind.swordDamage(), hard), HOUSE_EDGE_PERCENT);
            case GEAR -> Lang.f("Engrenage : ses autres symboles du tirage sont {0} % plus forts. Un de plus par rouleau qu'elle "
                + "t'a pris", GEAR_PERCENT);
            case SHORT_CIRCUIT -> Lang.t("Court-circuit : à ton prochain tirage, ton meilleur rouleau devient ton pire. Il casse "
                + "ta paire ou ton Bingo");
            case GOLDEN_COMET -> Lang.f("Comète : la Comète Dorée tombe, {0} % de tes PV max. Ignore ton bouclier",
                GOLDEN_COMET_PERCENT);
            case ASPIRATION -> Lang.t("Aspiration : elle annonce une règle, 2 cartes à jouer pour la première, 1 carte pour "
                + "chaque suivante, 3 règles au plus. Chaque règle que tes cartes du prochain tour ne respectent pas lui fait "
                + "aspirer un de tes rouleaux pour ce tirage");
            case PLATING -> Lang.t("Placage : sa peau d'or tient 1 tour, même sous la moitié de ses PV");
            case INGOT -> Lang.t("Lingot : un coup qui grandit avec sa défense du moment, jusqu'à x3");
            case ROCKFALL -> Lang.f("Éboulement : attaque de {0}. Tu pioches 1 carte de moins au prochain tour",
                kind.hurt(kind.swordDamage(), hard));
            case HEARTBEAT -> Lang.f("Battement : il se soigne de {0} % de ses PV max. Sa peau d'or revient 1 tour",
                HEARTBEAT_PERCENT);
            case SWALLOWED_COIN -> Lang.f("Pièce avalée : il te prend {0} % de tes gains et garde la pièce. À {1} pièces, son "
                + "prochain tirage est un Jackpot", COIN_PERCENT, COINS_FOR_JACKPOT);
            case ARMED_ARM -> Lang.t("Bras armé : il relance son pire rouleau, pour viser le Jackpot");
            case JUGGLE -> Lang.f("Jonglage : au début de ton prochain tour, {0} cartes de ta main sont échangées avec ta pioche",
                JUGGLE_CARDS);
            case ACT -> Lang.f("Numéro : il annonce une famille. Si tu n'en joues aucune carte, son prochain coup frappe x{0}",
                ACT_FACTOR);
            case SCARE -> Lang.f("Frayeur : ton prochain tirage donne {0} % d'attaque en moins ({1} fois au plus)",
                SCARE_PERCENT, SCARE_MAX);
            case TUNNEL -> Lang.t("Tunnel : à ton prochain tirage, un de tes rouleaux passe dans le noir et prend un symbole au "
                + "hasard");
            case TURNSTILE -> Lang.t("Tourniquet : à ton prochain tirage, ton meilleur rouleau est relancé une fois");
            case BIG_WHEEL -> Lang.f("Grand tour : sa roue tombe sur deux Épées ({0} chacune), deux Boucliers, une Potion, ou rien",
                kind.hurt(kind.swordDamage(), hard));
            case RIGGED_PRIZE -> Lang.t("Lot truqué : à ton prochain tirage, tes Paires ne rapportent aucun gain");
            case RIGGED_GAME -> Lang.f("Jeu truqué : si ton prochain tirage ne fait ni Paire ni Bingo, tes dégâts du tour sont "
                + "divisés par {0}", RIGGED_GAME_DIVISOR);
            case TOLL -> Lang.f("Péage : ton prochain tirage coûte {0} tirage de plus en gains", TOLL_SPINS);
            case BILL -> Lang.f("Lettre de change : {0} Créances sur toi ({1} au plus). À chacun de ses tours, chacune te coûte "
                + "{2} % de tes PV max. Chaque Trèfle joué en retire une", BILL_CLAIMS, BILL_MAX, Lang.decimal("0.5"));
            case WRAP -> Lang.f("Bandelettes : il ligote une carte de ta main. Elle ne se joue pas pendant {0} tours", WRAP_TURNS);
            case VENOM -> Lang.f("Venin : {0} % de tes PV max à chacun de tes tours, pendant {1} tours ({2} Venins au plus)",
                VENOM_PERCENT, VENOM_TURNS, VENOM_MAX);
            case MIRAGE -> Lang.t("Mirage : à ton prochain tirage, un de tes rouleaux montre un mirage. Il ne compte pas");
            case BURN -> Lang.f("Brûlure : ton bouclier fond de {0} %", BURN_PERCENT);
            case FIRE_BREATH -> Lang.f("Souffle de feu : attaque de {0}, à travers ton bouclier. Il brûle une carte de ta main au "
                + "prochain tour", kind.hurt(kind.swordDamage(), hard));
            case BINGO_LIGHT -> Lang.f("Lumière du bingo : ce rouleau ne fait rien. Il perd {0} % de ses PV max",
                fr.astratime.lucky.entities.EnemyMaluses.LIGHT_HP_PERCENT);
            case PRESSURE -> Lang.f("Pression : +1. À {0}, sa chaudière explose ({1}). Tes gros coups la font baisser",
                PRESSURE_MAX, kind.hurt(kind.swordDamage() * PRESSURE_FACTOR, hard));
            case PICKPOCKET -> Lang.t("Vol à la tire : une carte au hasard de ta main disparaît jusqu'à la fin de ton prochain tour");
            case BANQUET -> Lang.f("Banquet : il se soigne de {0} % de ses PV max et reforme sa défense", BANQUET_HEAL_PERCENT);
            case LULLABY -> Lang.t("Berceuse : au prochain tour, la première carte que tu joues s'endort. Elle ne fait rien");
            case HOLDUP -> Lang.f("Braquage : si tu lui as fait moins de {0} % de ses PV max depuis son dernier tour, il te prend "
                + "ton bouclier. Sinon, attaque de {1}", HOLDUP_PERCENT,
                kind.hurt(kind.swordDamage(), hard));
            case KEY -> Lang.t("Clé : il ferme à clé ta plus forte carte en main, jusqu'à la fin de ton prochain tour");
            case FREEZE -> Lang.t("Gel : un de tes rouleaux gèle. Au prochain tirage, il affiche le même symbole");
            case AVALANCHE -> Lang.f("Avalanche : attaque de {0}, puis +{1} % pour la suivante (jusqu'à +{2} %). Un coup "
                + "d'au moins {3} % de ses PV max la fait fondre", kind.hurt(kind.swordDamage(), hard), AVALANCHE_STEP_PERCENT,
                AVALANCHE_STEP_PERCENT * AVALANCHE_MAX, AVALANCHE_HIT_PERCENT);
            case CHAMPAGNE -> Lang.f("Champagne glacé : il se soigne de {0} % de ses PV max et reforme sa défense",
                CHAMPAGNE_HEAL_PERCENT);
            case KNELL -> Lang.t("Glas : un de tes rouleaux est bloqué à ton prochain tirage");
            case FATE_THREAD -> Lang.t("Fil du destin : un de tes rouleaux est lié au sien. À ton prochain tirage, il montre le "
                + "symbole le plus rare de ta machine");
            case DOUBLE_SIX -> Lang.f("Double six : il lance deux dés et frappe ({0}). Sur un double, ses attaques du tour x{1}",
                kind.hurt(kind.swordDamage(), hard), DOUBLE_FACTOR);
            case KARMA -> Lang.f("Karma : il garde le compte de tes Bingos ({0} au plus) et te les renvoie : attaque de {1} par Bingo. "
                + "Sans Bingo, il se protège", KARMA_MAX, kind.hurt(kind.swordDamage() * KARMA_FACTOR, hard));
            case BLOOD_PACT -> Lang.f("Pacte de sang : il prend {0} % de ta jauge de Sang et se soigne de {1} % de ses PV max. "
                + "Sans Sang, attaque de {2}", BLOOD_PACT_PERCENT, BLOOD_PACT_HEAL_PERCENT, kind.hurt(kind.swordDamage(), hard));
            case CURSE -> Lang.t("Malédiction : au prochain tour, ta meilleure carte piochée retourne sous ta pioche");
            case SHROUD -> Lang.f("Linceul : la première fois qu'il tombe, il se relève avec {0} % de ses PV max (une fois par "
                + "combat). Déjà drapé, il reforme sa défense", SHROUD_RISE_PERCENT);
            case STRINGS -> Lang.t("Fils : au début de ton prochain tour, il choisit ta plus faible carte et tu la joues d'office");
            case RALLY -> Lang.f("Rassemblement : il appelle un soldat ({0} au plus). À chacun de ses tours, chaque soldat tire "
                + "({1}). Un coup d'au moins {2} % de ses PV max en met un à terre", RALLY_MAX,
                kind.hurt(kind.swordDamage() * RALLY_SHOT_PERCENT / 100, hard), RALLY_HIT_PERCENT);
            case CANNON -> Lang.f("Canon à jetons : il charge {0} % de tes gains et tire ({1}). Ignore la moitié de ton bouclier. "
                + "Sans gains, il tire à vide ({2})", CANNON_PERCENT, kind.hurt(kind.swordDamage() * CANNON_FACTOR, hard),
                kind.hurt(kind.swordDamage(), hard));
            case SPY -> Lang.f("Espionnage : elle a lu tes plans. Un Bouclier par carte jouée à ton dernier tour ({0} au plus)",
                SPY_MAX);
            case CONTRACT -> Lang.f("Contrat : si tu lui as fait moins de {0} % de ses PV max depuis son dernier tour, il touche "
                + "sa prime et frappe de {1}. Sinon, attaque de {2}", CONTRACT_PERCENT,
                kind.hurt(kind.swordDamage() * CONTRACT_FACTOR, hard), kind.hurt(kind.swordDamage(), hard));
            case ALL_OUT -> Lang.f("Va-tout : ses attaques du tour x{0}. Mais il n'a plus aucune défense jusqu'à son prochain tour",
                ALL_OUT_FACTOR);
        };
    }

    /** @return {@code value} sans décimale inutile (ex : "30", "1,5"). */
    private static String percent(float value) {
        return value == Math.round(value) ? String.valueOf(Math.round(value))
            : Lang.decimal(String.valueOf(Math.round(value * 10f) / 10f));
    }

    /** @return la description de l'effet de base du symbole (chez le croupier), pour son infobulle. */
    public String getDescription() {
        return getDescription(EnemyKind.CROUPIER, Difficulty.NORMAL);
    }
}
