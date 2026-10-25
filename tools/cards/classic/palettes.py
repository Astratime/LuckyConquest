"""Couleurs de carte (liseré clair, face, ombre) : une couleur par donjon, une par famille pour les autres cartes.
La version « blanche » utilise toujours les couleurs des cartes classiques (base.WHITE, base.FACE, base.SHADOW)."""

FAMILIES = {
    # --- Prairie ---
    'pique':    ('#eef0f6', '#b8c0d4', '#7c86a0'),   # acier
    'trefle':   ('#eefae8', '#b4dca4', '#74a868'),   # vert
    'coeur':    ('#ffecec', '#f0b4b4', '#b87070'),   # rose rouge
    'carreau':  ('#fff2e4', '#f4c898', '#bc8c54'),   # ambre
    # --- Port des Contrebandiers ---
    'cale':     ('#f6ece0', '#d4b896', '#9a7c5c'),   # bois
    'taverne':  ('#fff6dc', '#ecd28c', '#b49a4c'),   # miel
    'phare':    ('#eaf6ff', '#a8d0f0', '#6a96bc'),   # ciel
    'galion':   ('#e8ecf4', '#a4b0c8', '#6a7690'),   # ardoise
    # --- Mines d'Or ---
    'filon':    ('#fff8dc', '#f0dc8c', '#b8a04c'),   # or
    'puits':    ('#f0ece6', '#c4bab0', '#8a8076'),   # pierre
    'forge':    ('#ffece0', '#f0b08c', '#b4704c'),   # braise
    'gouffre':  ('#f2e8f0', '#cbb4c4', '#8e7488'),   # mauve sombre
    # --- Casino Englouti ---
    'bar':      ('#e4faf6', '#9cdcd0', '#5ea496'),   # lagon
    'machines': ('#fde8f6', '#e8a8d4', '#ac6898'),   # magenta
    'coffres':  ('#e6f6fa', '#a0d4e0', '#6098a8'),   # eau
    'vip':      ('#f0e8fc', '#c8b0ec', '#8a70b4'),   # pourpre
    # --- autres cartes spéciales ---
    'gains':    ('#e8faf0', '#a8e0c0', '#68a884'),   # menthe : cartes de gains
    'main':     ('#ecf0ff', '#b4c0f0', '#7884bc'),   # lavande : pioche et main
    'rouleaux': ('#f4ecff', '#d4bcf4', '#9a80c4'),   # lilas : cartes de rouleaux
    'echoppe':  ('#fbe8ec', '#e0a4b0', '#a06474'),   # lie-de-vin : échoppe du combat
    'bingo':    ('#fff4e0', '#f4d49c', '#c09c5c'),   # dorée : cartes Bingo
    # --- les Joker (validés par Astra le 2026-10-11) ---
    'j_souriant':    ('#e6fbf8', '#a8dcd8', '#6aa8a4'),
    'j_masque':      ('#ffeef0', '#e8b4bc', '#a8707c'),
    'j_cour':        ('#eef4ff', '#b8cdf0', '#7a92c0'),
    'j_malveillant': ('#f0e8fa', '#c4b0e0', '#8670ac'),
    'j_marotte':     ('#fff8e0', '#f0d890', '#b89c50'),
    # --- archétypes de deck (2026-10-23) ---
    'arch_joker':     ('#f4ecfc', '#d4bcec', '#9a7cbc'),   # violet du fou
    'arch_lutin':     ('#eafaea', '#a8e0a4', '#68a464'),   # vert trèfle
    'arch_rentier':   ('#fdf6e4', '#e8d4a0', '#ae9a60'),   # parchemin
    'arch_bluffeur':  ('#eceef4', '#b4b8c8', '#767a90'),   # gris fumée
    'arch_relanceur': ('#e6f2fc', '#a4c8ec', '#6890b8'),   # bleu machine
}

CARD_FAMILY = {
    'pierre_a_aiguiser': 'pique', 'dague_de_l_ombre': 'pique', 'guillotine': 'pique',
    'trefle_a_quatre_feuilles': 'trefle', 'bourse_garnie': 'trefle', 'fortune_du_roi': 'trefle',
    'transfusion': 'coeur', 'baiser_vampire': 'coeur', 'pacte_de_sang': 'coeur',
    'rempart': 'carreau', 'miroir_taille': 'carreau', 'diamant_brut': 'carreau',
    'piege_a_rats': 'cale', 'rhum': 'cale', 'carte_au_tresor': 'cale', 'scorbut': 'cale',
    'chope': 'taverne', 'tournee_generale': 'taverne', 'fut_de_poudre': 'taverne',
    'longue_vue': 'phare', 'lanterne': 'phare', 'rayon_du_phare': 'phare',
    'sabre_d_abordage': 'galion', 'pavillon_noir': 'galion', 'mutinerie': 'galion',
    'pioche_du_mineur': 'filon', 'tamis': 'filon', 'veine_d_or': 'filon',
    'casque': 'puits', 'etai': 'puits', 'corde_de_rappel': 'puits',
    'marteau_de_forge': 'forge', 'trempe': 'forge', 'lame_forgee': 'forge',
    'dynamite': 'gouffre', 'lampe_a_carbure': 'gouffre', 'coeur_d_or': 'gouffre',
    'bulle_d_air': 'bar', 'bouchons_d_oreille': 'bar', 'cocktail_des_abysses': 'bar',
    'piece_truquee': 'machines', 'levier_rouille': 'machines', 'jackpot_englouti': 'machines',
    'perle_noire': 'coffres', 'cage_a_requin': 'coffres', 'coffre_de_l_epave': 'coffres',
    'harpon': 'vip', 'ancre': 'vip', 'trident': 'vip',
    'gain_500': 'gains', 'gain_2000': 'gains', 'lucky_charm': 'gains', 'pot_de_lutin': 'gains', 'bet': 'gains',
    'safe': 'gains', 'insurance': 'gains', 'bribe': 'gains', 'double_or_nothing': 'gains', 'all_in': 'gains',
    'rank_token': 'gains',
    'draw_2': 'main', 'draw_3': 'main', 'in_the_sleeve': 'main', 'recycle': 'main', 'reroll': 'main', 'magnet': 'main',
    'rigged_reel': 'rouleaux', 'ghost_reel': 'rouleaux', 'rainbow': 'rouleaux', 'overheat': 'rouleaux',
    'bingo': 'echoppe', 'russian_roulette': 'echoppe', 'corruption': 'echoppe', 'cursed_joker': 'echoppe',
    'joker': 'j_souriant', 'masque_du_joker': 'j_masque', 'joker_de_cour': 'j_cour',
    'joker_malveillant': 'j_malveillant', 'marotte_du_joker': 'j_marotte',
    'joker_sauvage': 'arch_joker', 'rire_du_joker': 'arch_joker', 'couronne_du_fou': 'arch_joker',
    'atelier_du_joker': 'arch_joker', 'joker_cameleon': 'arch_joker',
    'chapeau_du_lutin': 'arch_lutin', 'gigue_du_lutin': 'arch_lutin', 'bout_de_l_arc_en_ciel': 'arch_lutin',
    'marteau_du_cordonnier': 'arch_lutin', 'farce_du_lutin': 'arch_lutin',
    'reconnaissance_de_dette': 'arch_rentier', 'taux_variable': 'arch_rentier', 'interets_composes': 'arch_rentier',
    'recouvrement': 'arch_rentier', 'rente_viagere': 'arch_rentier',
    'intimidation': 'arch_bluffeur', 'cartes_marquees': 'arch_bluffeur', 'fausse_piste': 'arch_bluffeur',
    'tour_de_passe_passe': 'arch_bluffeur', 'coup_de_bluff': 'arch_bluffeur',
    'rouleau_retenu': 'arch_relanceur', 'memoire_de_la_machine': 'arch_relanceur', 'calibrage': 'arch_relanceur',
    'second_levier': 'arch_relanceur', 'martingale': 'arch_relanceur',
    'diable_en_boite': 'arch_joker', 'chamboule_tout': 'arch_relanceur', 'sceau_du_pharaon': 'arch_rentier',
    'magot_du_dragon': 'arch_lutin',
}
