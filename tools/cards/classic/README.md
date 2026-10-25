# Cartes spéciales au style des cartes classiques

Demande d'Astra (2026-10-11) : redessiner toutes les cartes spéciales dans le style des 5 Joker validés
(`jokers_src.py`).

## Contraintes (strictes)
- Pixel art. Grille 38x54, agrandie x5 en 190x270 (la taille des cartes classiques `assets/cards/light/*.png`).
- Toujours partir de `base.card(*pal)` : contour noir aux coins coupés, liseré clair en haut/gauche, ombre à
  droite/en bas, face. NE JAMAIS dessiner sur la bordure : tout dessin passe par `base.clip(im, layer)`
  (zone dessinable x 4..33, y 4..49). Les index d'angle passent par `base.corners(im, rows, pal_dict)`
  (petit motif ~5x5 en haut à gauche, retourné en bas à droite, comme la lettre des cartes classiques).
- AUCUN TEXTE, AUCUN CHIFFRE, AUCUNE LETTRE sur les cartes (le jeu est multilingue). Pour « Pioche +2 » ou
  « Gains +500 », montrer la quantité par le dessin (2 cartes, une pile de pièces…), jamais par un chiffre.
  Seule exception : les cartes d'archétype numérotées (`cards_archetypes.py`, demande d'Astra du 2026-10-25)
  portent leur rang en index dans le coin haut gauche (A, K, Q, J, 10, 9, comme les cartes classiques), suivi
  de leur emblème, pour les combinaisons. L'index ne cache jamais le dessin : `fit_art` le place à côté et le
  resserre au besoin.
- Contours en `base.INK` (comme les enseignes des cartes classiques) ; couleurs vives et lisibles, sujet
  centré, ~24x30 cases, qui se lit au premier coup d'oeil même en petit. Laisser un peu de face visible
  autour du sujet (comme les Joker), ne pas remplir toute la face d'un décor.
- `pal = (face, light, shadow)`. Une carte est rendue deux fois (face blanche/grise classique, et face colorée
  de sa famille) : tout élément qui reprend la couleur de la face doit utiliser `pal[0]`, jamais `base.FACE`.
- Le sujet de chaque carte : regarder l'illustration pleine carte `assets/cards/special_full/<id>.png`
  et son effet (`assets/cards/definitions/{special,donjons,lieux}.json`) pour garder la même idée, mais
  redessinée simplement dans ce style. Chaque carte doit être différente des autres.

## Fichiers
- Chaque module `cards_*.py` de ce dossier expose `DESIGNS = {id: fonction(pal) -> Image 38x54}`.
  Reprendre les helpers de `jokers_src.py` (poly, sprite, outline…) en les copiant ou en important `base`.
- Rendu : `python3 tools/cards/classic/render.py [cards_wN]`
  -> `assets/cards/special_classic/blanc/<id>.png` (affichées par le jeu) et `.../couleurs/<id>.png`.
