"""Rend les cartes spéciales au style des cartes classiques, en deux versions :
  assets/cards/special_classic/blanc/<id>.png     : couleurs des cartes classiques (liseré blanc, face gris clair),
                                                    celles qu'affiche le jeu
  assets/cards/special_classic/couleurs/<id>.png  : couleur de la famille (une couleur par donjon), voir palettes.py
Chaque module cards_*.py expose DESIGNS = {id: fonction(pal) -> image 38x54}, avec pal = (face, light, shadow)
à passer à base.card(*pal). Tout élément qui reprend la couleur de la face doit utiliser pal[0].

    python3 tools/cards/classic/render.py [module ...]        (Python 3, Pillow, numpy ; sans argument : tous)
"""
import importlib
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from base import FACE, WHITE, SHADOW, hexc, up  # noqa: E402
from palettes import FAMILIES, CARD_FAMILY  # noqa: E402

MODULES = ['cards_jokers', 'cards_w1', 'cards_w2', 'cards_w3', 'cards_w4', 'cards_bingo', 'cards_archetypes']


def palette(card_id):
    light, face, shadow = (hexc(c) for c in FAMILIES[CARD_FAMILY.get(card_id, 'bingo')])
    return (face, light, shadow)


def designs(names):
    out = {}
    for n in names:
        try:
            out.update(importlib.import_module(n).DESIGNS)
        except ModuleNotFoundError as e:
            if e.name != n:
                raise
    return out


def main():
    names = sys.argv[1:] or MODULES
    root = os.path.join(HERE, '..', '..', '..', 'assets', 'cards', 'special_classic')
    for sub in ('blanc', 'couleurs'):
        os.makedirs(os.path.join(root, sub), exist_ok=True)
    ds = designs(names)
    for cid, fn in ds.items():
        up(fn((FACE, WHITE, SHADOW))).save(os.path.join(root, 'blanc', cid + '.png'))
        up(fn(palette(cid))).save(os.path.join(root, 'couleurs', cid + '.png'))
    print(len(ds), 'cartes')


if __name__ == '__main__':
    main()
