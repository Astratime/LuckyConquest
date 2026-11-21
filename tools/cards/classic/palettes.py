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
    # --- Fête foraine (cartes des rois, 2026-11-14) ---
    'chapiteau':     ('#fdecec', '#f0a8a8', '#b46464'),   # toile rouge
    'train_fantome': ('#eee8f4', '#bcaed0', '#7e7096'),   # brume violette
    'grande_roue':   ('#fff4e2', '#f4cc90', '#bc9050'),   # néon orangé
    'stands':        ('#e8f6ec', '#a4d8b4', '#64a078'),   # vert de foire
    # --- Rapide de Minuit (2026-11-20) ---
    'locomotive':       ('#eceef4', '#b4b8c8', '#767a90'),   # fumée
    'wagon_restaurant': ('#fdf0e8', '#ecc4a8', '#b08468'),   # nappe et vernis
    'wagon_lit':        ('#eeeafc', '#c0b4ec', '#8274b4'),   # nuit violette
    'fourgon':          ('#eef4ee', '#b4ccb8', '#76927a'),   # acier vert
    # --- Désert de Braise (cartes des rois, 2026-11-14) ---
    'caravane':      ('#fdf4e4', '#e8cc98', '#ae8e58'),   # sable
    'tombeau':       ('#f4f0e2', '#d0c4a0', '#948864'),   # calcaire
    'oasis':         ('#e4f8f4', '#98d8cc', '#5a9e92'),   # eau de l'oasis
    'volcan':        ('#ffe8e0', '#f0a08c', '#b05c48'),   # lave
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
    'arch_cour':      ('#fdeef0', '#f0b4bc', '#b0606e'),   # velours royal
    'arch_revenant':  ('#eef0f8', '#c4c8dc', '#7c809c'),   # brume
    'arch_horloger':  ('#fbf0e0', '#e4c494', '#a47a44'),   # laiton
    # --- cartes à usure (2026-11-14) ---
    'usure':          ('#fff4ea', '#f0cca4', '#b48a60'),   # cire
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
    'golden_reel': 'echoppe', 'dealer_hand': 'echoppe', 'bounty': 'echoppe', 'lucky_strike': 'echoppe',
    'scratch_ticket': 'echoppe',
    'etoile_filante': 'trefle', 'etoile_du_berger': 'trefle', 'etoile_d_or': 'trefle',
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
    'heraut': 'arch_cour', 'dame_de_cour': 'arch_cour', 'sceptre_du_roi': 'arch_cour', 'quinte_royale': 'arch_cour',
    'fossoyeur': 'arch_revenant', 'dame_blanche': 'arch_revenant', 'roi_des_ombres': 'arch_revenant',
    'requiem': 'arch_revenant',
    'bougie': 'usure', 'graine': 'usure', 'pile_ou_face': 'usure',
    'cle_de_remontage': 'arch_horloger', 'coucou': 'arch_horloger', 'grande_aiguille': 'arch_horloger',
    'douze_coups': 'arch_horloger',
    # cartes des rois (2026-11-14) : la couleur du donjon de leur roi
    'roi_provocation': 'pique', 'roi_taux_usuraire': 'trefle', 'roi_saignee': 'coeur', 'roi_ronces': 'carreau',
    'roi_roulement_de_tambour': 'chapiteau', 'roi_tunnel_sans_fin': 'train_fantome', 'roi_roue_lestee': 'grande_roue',
    'roi_carte_biseautee': 'stands',
    'roi_plein_regime': 'locomotive', 'roi_banquet': 'wagon_restaurant', 'roi_marchand_de_sable': 'wagon_lit',
    'roi_haut_les_mains': 'fourgon',
    'pelle_a_charbon': 'locomotive', 'sifflet_du_chef_de_gare': 'locomotive', 'chaudiere_ardente': 'locomotive',
    'plat_du_jour': 'wagon_restaurant', 'service_a_la_place': 'wagon_restaurant', 'menu_gastronomique': 'wagon_restaurant',
    'billet_de_seconde': 'wagon_lit', 'couchette': 'wagon_lit', 'billet_de_premiere': 'wagon_lit',
    'coffre_blinde': 'fourgon', 'aiguillage': 'fourgon', 'soupape': 'fourgon',
    'roi_dents_de_rat': 'cale', 'roi_tournee_du_patron': 'taverne', 'roi_feu_du_phare': 'phare', 'roi_grappin': 'galion',
    'roi_tamis_noir': 'filon', 'roi_meche_de_diamant': 'puits', 'roi_coup_d_enclume': 'forge', 'roi_filon': 'gouffre',
    'roi_refrain': 'bar', 'roi_rage_du_jackpot': 'machines', 'roi_frenesie': 'coffres', 'roi_encre': 'vip',
    'roi_usure': 'caravane', 'roi_momification': 'tombeau', 'roi_fata_morgana': 'oasis', 'roi_braise': 'volcan',
}
