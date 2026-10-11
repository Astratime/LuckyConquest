"""Dessine toutes les cartes spéciales en pleine illustration dans assets/cards/special_full/.

Les anciennes images (assets/cards/special/) ne sont jamais modifiées.
Lancement, depuis la racine du dépôt (Python 3, Pillow, numpy) :
    python3 tools/cards/full_art/build_all.py [planche.png] [nom ...]
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from PIL import Image  # noqa: E402
from px import up  # noqa: E402
import joker  # noqa: E402
import roulette  # noqa: E402
import fortune  # noqa: E402
import cards_boutique  # noqa: E402

OUT = 'assets/cards/special_full/'

CARDS = {'joker': joker.build, 'russian_roulette': roulette.build, 'fortune_du_roi': fortune.build}
CARDS.update(cards_boutique.CARDS)
for module in ('cards_lieux', 'cards_donjons'):
    try:
        CARDS.update(__import__(module).CARDS)
    except ImportError:
        pass


def main():
    sheet_path = sys.argv[1] if len(sys.argv) > 1 else None
    only = set(sys.argv[2:])
    os.makedirs(OUT, exist_ok=True)
    names = sorted(n for n in CARDS if not only or n in only)
    images = []
    for name in names:
        img = CARDS[name]()
        up(img).save(OUT + name + '.png')
        images.append(img)
    if sheet_path:
        cols = 8
        rows = (len(images) + cols - 1) // cols
        s = 3
        sheet = Image.new('RGBA', (cols * (80 * s + 8) + 8, rows * (112 * s + 8) + 8), (28, 22, 36, 255))
        for i, img in enumerate(images):
            big = img.resize((80 * s, 112 * s), Image.NEAREST)
            sheet.paste(big, (8 + (i % cols) * (80 * s + 8), 8 + (i // cols) * (112 * s + 8)), big)
        sheet.save(sheet_path)
    print(len(names), 'cartes')


if __name__ == '__main__':
    main()
