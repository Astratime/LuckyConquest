"""
Tables des lieux de l'Exploration : une table à l'air du lieu (rebord, feutre,
emblème, cadres), tirée de la table de casino d'origine (assets/table).

Le rebord garde la forme d'origine (cadre étirable 9-patch) : ses couleurs sont
remplacées par la matière du lieu, avec une rainure le long du bord et un
ornement dans chaque coin (les coins ne sont pas étirés). Le feutre est un motif
répété de 32 x 32 pixels de jeu. Tout est dessiné sur la grille du jeu (1 pixel
= 3 pixels de texture), sans lissage.

Produit assets/table/<lieu>/*.png pour prairie, foire, port, mines et desert (le
casino, la Tour et l'Entraînement gardent la table d'origine, assets/table/*.png). Lancer depuis la racine du dépôt :
    python3 tools/table/generate_place_tables.py
"""
import os
import shutil

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'assets', 'table')
P = 3  # un pixel du jeu
OUT = (30, 12, 10)

# Couleurs de la table d'origine
LEATHER = [(150, 40, 52), (116, 24, 36), (94, 18, 28), (64, 10, 18)]  # reflet, cuir, creux, fond
GOLD = [(255, 214, 84), (218, 165, 32), (150, 100, 14)]
GOLD_LINE = (232, 200, 96)
FELT_DARK = (18, 70, 38)

GOLDS = [(255, 214, 84), (218, 165, 32), (150, 100, 14)]
BRASS = [(232, 196, 120), (190, 146, 70), (128, 92, 38)]
IRON = [(206, 210, 218), (146, 152, 164), (88, 92, 104)]

SUITS = {
    'coeur': [".X.X.", "XXXXX", "XXXXX", ".XXX.", "..X.."],
    'carreau': ["..X..", ".XXX.", "XXXXX", ".XXX.", "..X.."],
    'pique': ["..X..", ".XXX.", "XXXXX", "XXXXX", "..X.."],
    'trefle': [".XXX.", "XXXXX", "XXXXX", "..X..", ".XXX."],
}

THEMES = {
    'prairie': dict(
        rail=[(190, 136, 80), (156, 104, 58), (126, 82, 44), (92, 58, 30)], groove=(120, 78, 42),
        metal=GOLDS, felt=[(60, 126, 60), (50, 110, 52), (72, 142, 70)], ornament='suits', motif='pips',
        emblem='suits'),
    'foire': dict(
        rail=[(244, 232, 210), (204, 44, 52), (162, 30, 44), (110, 18, 30)], groove=(244, 232, 210),
        metal=GOLDS, felt=[(76, 44, 116), (64, 36, 100), (92, 58, 134)], ornament='star', motif='stars',
        emblem='star'),
    'port': dict(
        rail=[(150, 104, 62), (112, 72, 40), (86, 54, 30), (60, 36, 20)], groove=(198, 166, 106),
        metal=BRASS, felt=[(28, 66, 106), (22, 54, 90), (38, 82, 126)], ornament='rivet', motif='waves',
        emblem='anchor'),
    'mines': dict(
        rail=[(128, 132, 144), (98, 66, 40), (78, 52, 30), (54, 34, 20)], groove=(64, 42, 24),
        metal=IRON, felt=[(60, 58, 68), (50, 48, 58), (72, 70, 82)], ornament='plate', motif='ore',
        emblem='pickaxes'),
    'desert': dict(
        rail=[(242, 212, 148), (214, 170, 100), (180, 136, 76), (138, 98, 52)], groove=(38, 160, 160),
        metal=GOLDS, felt=[(150, 64, 42), (128, 52, 34), (168, 78, 52)], ornament='gem', motif='diamonds',
        emblem='sun'),
}


def load(name):
    return Image.open(os.path.join(ROOT, name + '.png')).convert('RGBA')


def remap(img, mapping):
    """Remplace des couleurs exactes (alpha conservé)."""
    a = np.array(img)
    out = a.copy()
    for src, dst in mapping.items():
        m = (a[:, :, 0] == src[0]) & (a[:, :, 1] == src[1]) & (a[:, :, 2] == src[2])
        out[m, :3] = dst
    return Image.fromarray(out)


def block(d, x, y, color):
    """Un pixel du jeu en (x, y), coordonnées de la grille du jeu."""
    d.rectangle([x * P, y * P, x * P + P - 1, y * P + P - 1], fill=color)


def sprite(d, rows, x, y, colors):
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch in colors:
                block(d, x + i, y + j, colors[ch])


def ornament(d, kind, x, y, t):
    """Ornement de coin (5 x 5 pixels du jeu, coin haut gauche en x, y)."""
    m = t['metal']
    if kind == 'suits':
        suit = ['pique', 'coeur', 'trefle', 'carreau'][ornament.count % 4]
        ornament.count += 1
        ink = (216, 32, 44) if suit in ('coeur', 'carreau') else (34, 26, 40)
        sprite(d, SUITS[suit], x, y, {'X': ink})
    elif kind == 'star':
        sprite(d, ["..a..", ".aba.", "abbba", ".aba.", "a...a"], x, y, {'a': m[2], 'b': m[0]})
        sprite(d, ["..a..", "..b..", "abbba", ".bab.", ".a.a."], x, y, {'a': m[1], 'b': m[0]})
    elif kind == 'rivet':
        sprite(d, [".ccc.", "cbbbc", "cbacc", "cbbbc", ".ccc."], x, y, {'a': OUT, 'b': m[0], 'c': m[2]})
    elif kind == 'plate':
        sprite(d, ["ccccc", "cacac", "cbbbc", "cacac", "ccccc"], x, y, {'a': m[0], 'b': m[1], 'c': m[2]})
    elif kind == 'gem':
        sprite(d, [".ccc.", "cbabc", "caaac", "cbabc", ".ccc."], x, y,
               {'a': (40, 176, 176), 'b': (150, 236, 230), 'c': m[1]})


ornament.count = 0


def rail(t):
    img = remap(load('table_rail'), dict(zip(LEATHER, t['rail'])) | dict(zip(GOLD, t['metal'])))
    d = ImageDraw.Draw(img)
    a = np.array(load('table_rail'))
    main = (a[:, :, 0] == LEATHER[1][0]) & (a[:, :, 1] == LEATHER[1][1]) & (a[:, :, 2] == LEATHER[1][2])
    # Rainure (ou bande) le long du bord, seulement sur la matière principale.
    groove = Image.new('L', img.size, 0)
    ImageDraw.Draw(groove).rounded_rectangle([6 * P, 6 * P, img.width - 6 * P - 1, img.height - 6 * P - 1],
                                             radius=8 * P, outline=255, width=P)
    g = (np.array(groove) > 0) & main
    arr = np.array(img)
    arr[g, :3] = t['groove']
    img = Image.fromarray(arr)
    d = ImageDraw.Draw(img)
    for cx, cy in ((3, 3), (img.width // P - 8, 3), (3, img.height // P - 8), (img.width // P - 8, img.height // P - 8)):
        ornament(d, t['ornament'], cx, cy, t)
    return img


def felt(t):
    base, dark, light = t['felt']
    size = 32
    rng = np.random.default_rng(11)
    img = Image.new('RGBA', (size * P, size * P), base + (255,))
    d = ImageDraw.Draw(img)
    for _ in range(80):  # grain du feutre, comme l'original
        block(d, int(rng.integers(0, size)), int(rng.integers(0, size)), dark if rng.random() < 0.65 else light)
    motif = t['motif']
    spots = [(4, 5), (20, 13), (10, 23), (26, 28)]
    for k, (x, y) in enumerate(spots):
        if motif == 'pips':
            suit = ['pique', 'coeur', 'trefle', 'carreau'][k]
            sprite(d, SUITS[suit], x, y, {'X': dark})
        elif motif == 'stars':
            sprite(d, [".a.", "aaa", ".a."], x, y, {'a': light})
            block(d, x + 1, y + 1, (150, 120, 190))
        elif motif == 'waves':
            sprite(d, [".aa...", "a..a.a", "....a."], x, y, {'a': light})
        elif motif == 'ore':
            sprite(d, ["ab", "b."], x, y, {'a': (255, 214, 84), 'b': (176, 130, 40)})
            block(d, x + 4, y + 2, light)
        elif motif == 'diamonds':
            sprite(d, [".a.", "a.a", ".a."], x, y, {'a': light})
    return img


def emblem(t):
    """Emblème au milieu de la table (22 x 15 pixels du jeu), ombre portée comme l'original."""
    w, h = 22, 15
    m = t['metal']
    img = Image.new('RGBA', (w * P, h * P), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    kind = t['emblem']
    if kind == 'suits':
        rows = {}
        for k, suit in enumerate(['pique', 'coeur', 'trefle', 'carreau']):
            ink = (216, 32, 44) if suit in ('coeur', 'carreau') else (34, 26, 40)
            rows[k] = (suit, ink)
        # bandeau doré, puis les 4 symboles
        for x in range(1, w - 1):
            for y in range(4, 11):
                block(d, x, y, m[1] if y in (4, 10) else (246, 238, 220))
        for x in range(1, w - 1):
            block(d, x, 4, m[0])
        for k in range(4):
            suit, ink = rows[k]
            sprite(d, SUITS[suit], 1 + k * 5, 5, {'X': ink})
    elif kind == 'star':
        sprite(d, ["....a....", "...aba...", "..abbba..", "aaabbbaaa", ".abbbbba.", "..abbba..", ".abbabba.",
                   ".aba.aba.", ".a.....a."], 6, 3, {'a': m[2], 'b': m[0]})
        for x in range(0, 6):
            block(d, x, 7, m[1])
            block(d, w - 1 - x, 7, m[1])
    elif kind == 'anchor':
        sprite(d, ["...aaa...", "...a.a...", "...aaa...", ".aaaaaaa.", "....a....", "....a....", "a...a...a",
                   "aa..a..aa", ".aa.a.aa.", "..aaaaa..", "....a...."], 6, 2, {'a': m[0]})
        for x in range(0, 5):
            block(d, x, 7, m[1])
            block(d, w - 1 - x, 7, m[1])
    elif kind == 'pickaxes':
        sprite(d, [".aaa.....aaa.", "a...a...a...a", ".....b.b.....", "......b......", ".....b.b.....",
                   "....b...b....", "...b.....b...", "..b.......b..", ".b.........b."], 4, 3,
               {'a': m[0], 'b': (156, 104, 58)})
    elif kind == 'sun':
        sprite(d, ["a...a...a", ".a.aaa.a.", "..abbba..", "aaabcbaaa", "..abbba..", ".a.aaa.a.", "a...a...a"],
               6, 4, {'a': m[0], 'b': m[1], 'c': (40, 176, 176)})
        for x in range(0, 5):
            block(d, x, 7, m[1])
            block(d, w - 1 - x, 7, m[1])
    # ombre portée d'un pixel du jeu, comme l'emblème d'origine
    a = np.array(img)
    shadow = np.zeros_like(a)
    mask = a[:, :, 3] > 0
    sh = np.zeros_like(mask)
    sh[P:, P:] = mask[:-P, :-P]
    shadow[sh & ~mask] = (0, 0, 0, 70)
    out = Image.fromarray(shadow)
    out.alpha_composite(img)
    return out


def main():
    for place, t in THEMES.items():
        folder = os.path.join(ROOT, place)
        os.makedirs(folder, exist_ok=True)
        metal = dict(zip(GOLD, t['metal']))
        line = t['metal'][0]
        rail(t).save(os.path.join(folder, 'table_rail.png'))
        felt(t).save(os.path.join(folder, 'felt.png'))
        emblem(t).save(os.path.join(folder, 'felt_emblem.png'))
        remap(load('pile_mat'), metal | {FELT_DARK: tuple(int(v * 0.65) for v in t['felt'][1]),
                                         GOLD_LINE: line}).save(os.path.join(folder, 'pile_mat.png'))
        remap(load('reel_frame'), metal).save(os.path.join(folder, 'reel_frame.png'))
        remap(load('card_slot'), {GOLD_LINE: line}).save(os.path.join(folder, 'card_slot.png'))
        remap(load('felt_line'), metal).save(os.path.join(folder, 'felt_line.png'))
        for name in ('reel_cell', 'bulbs', 'bulb_glow', 'felt_light'):
            shutil.copy(os.path.join(ROOT, name + '.png'), os.path.join(folder, name + '.png'))

if __name__ == '__main__':
    main()
