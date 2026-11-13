"""
Cosmétiques : dos de cartes, tapis de la table de combat et habillage de la
machine à sous, dans quatre styles (néon, bois, or, abysses).

Chaque style produit, dans assets/cosmetics/<style>/ :
  - back.png       : dos des cartes du joueur, même forme et même cadre que
                     assets/cards/light/BACK.png (19 x 27 pixels de jeu, x10) ;
  - felt.png       : feutre de la table, motif répété de 32 x 32 pixels de jeu (x3) ;
  - reel_frame.png : carrosserie de la machine (cadre 9-patch, couleurs remplacées) ;
  - reel_cell.png  : fenêtre d'un rouleau (cadre 9-patch, couleurs remplacées).

Tout est dessiné sur la grille du jeu, sans lissage ni texte. Lancer depuis la racine du dépôt :
    python3 tools/cosmetics/generate_cosmetics.py
"""
import math
import os
import random

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'assets')
OUT_DIR = os.path.join(ROOT, 'cosmetics')

# ---------------------------------------------------------------------------
# Styles
# ---------------------------------------------------------------------------

STYLES = {
    'neon': dict(
        # dos : cadre (contour, anneau, liseré, ombre), fond, motif
        ring=(255, 79, 216), inner=(60, 240, 255), shade=(150, 30, 140), body=(26, 12, 52),
        a=(255, 79, 216), b=(60, 240, 255), c=(255, 240, 120),
        # feutre
        felt=[(30, 16, 56), (24, 12, 46), (38, 22, 70)], felt_a=(80, 30, 112), felt_b=(40, 110, 150),
        # machine : clair, moyen, sombre, contour ; fenêtre ; case (ombre, face, reflet)
        metal=[(255, 122, 230), (194, 43, 176), (106, 20, 112)], outline=(18, 5, 28),
        window=[(56, 40, 92), (34, 22, 60), (20, 12, 36)],
        cell=[(196, 184, 232), (236, 230, 255), (250, 248, 255)]),
    'bois': dict(
        ring=(214, 168, 92), inner=(250, 224, 160), shade=(120, 76, 36), body=(128, 78, 40),
        a=(96, 56, 26), b=(160, 104, 56), c=(232, 196, 120),
        felt=[(122, 76, 40), (108, 66, 34), (138, 88, 48)], felt_a=(92, 56, 28), felt_b=(150, 98, 54),
        metal=[(196, 140, 84), (150, 98, 54), (96, 60, 30)], outline=(40, 20, 10),
        window=[(70, 46, 30), (44, 28, 18), (28, 18, 12)],
        cell=[(214, 196, 160), (246, 234, 206), (255, 248, 230)]),
    'or': dict(
        ring=(255, 236, 150), inner=(255, 250, 220), shade=(150, 100, 14), body=(218, 165, 32),
        a=(176, 124, 20), b=(255, 214, 84), c=(255, 255, 255),
        felt=[(196, 146, 30), (180, 132, 24), (214, 166, 44)], felt_a=(160, 112, 16), felt_b=(240, 200, 90),
        metal=[(255, 246, 190), (255, 214, 84), (184, 132, 24)], outline=(60, 36, 4),
        window=[(90, 66, 20), (54, 40, 12), (34, 24, 8)],
        cell=[(230, 210, 150), (255, 246, 214), (255, 252, 236)]),
    'abysses': dict(
        ring=(70, 210, 200), inner=(170, 250, 240), shade=(20, 90, 110), body=(10, 34, 64),
        a=(40, 120, 160), b=(110, 230, 220), c=(220, 255, 250),
        felt=[(12, 40, 70), (8, 32, 58), (18, 52, 86)], felt_a=(30, 80, 120), felt_b=(60, 150, 170),
        metal=[(120, 220, 210), (40, 140, 150), (16, 70, 90)], outline=(4, 14, 24),
        window=[(30, 60, 84), (16, 36, 54), (8, 22, 36)],
        cell=[(170, 206, 214), (222, 242, 244), (240, 252, 252)]),
}

BLACK = (0, 0, 0)

# ---------------------------------------------------------------------------
# Dos de cartes
# ---------------------------------------------------------------------------

BACK_W, BACK_H, BACK_P = 19, 27, 10


def back_pattern(style, s, x, y):
    """@return la couleur du motif au pixel (x, y) de l'intérieur (13 x 21), ou None pour le fond."""
    cx, cy = 6, 10
    dx, dy = x - cx, y - cy
    if style == 'neon':
        # losanges concentriques en néon, une étoile au centre
        d = abs(dx) + abs(dy)
        if d == 0 or (d == 1 and (dx == 0 or dy == 0)): return s['c']
        if d in (3, 7, 11, 15): return s['a'] if (d // 4) % 2 == 0 else s['b']
        if (x + y) % 4 == 0 and d > 15: return s['b']
        return None
    if style == 'bois':
        # veines du bois qui ondulent, un losange sculpté au centre
        d = abs(dx) + abs(dy) * 2 // 3
        if d == 4: return s['c']
        if d == 3: return s['a']
        if d < 3: return s['b'] if d == 0 else None
        vein = (x + int(round(1.2 * math.sin(y * 0.55 + x)))) % 4
        if vein == 0: return s['a']
        if vein == 2 and (y + x) % 5 == 0: return s['b']
        return None
    if style == 'or':
        # treillis gravé en diagonale, un losange serti au centre
        d = abs(dx) + abs(dy)
        if d <= 3:
            if d == 3: return s['a']
            return s['c'] if dx <= 0 and dy <= 0 and d <= 1 else s['b']
        if d == 4: return None
        if (x + y) % 4 == 0 or (x - y) % 4 == 0: return s['a']
        if (x + y) % 4 == 2 and (x - y) % 4 == 2: return s['c']
        return None
    if style == 'abysses':
        # vagues, bulles et un oeil des abysses au centre
        if dx * dx + dy * dy * 2 <= 8:
            if dx == 0 and dy == 0: return BLACK
            if abs(dx) <= 1 and dy == 0: return s['c']
            return s['b']
        wave = int(round(math.sin((x + y // 7 * 3) * 0.9) * 0.9))
        if (y + wave) % 7 == 0: return s['a']
        if (x, y) in ((2, 2), (10, 4), (3, 16), (9, 18), (11, 15), (1, 9)): return s['c']
        return None
    raise ValueError(style)


def make_back(style, s):
    img = Image.new('RGBA', (BACK_W * BACK_P, BACK_H * BACK_P), (0, 0, 0, 0))
    px = img.load()

    def put(x, y, color):
        for i in range(BACK_P):
            for j in range(BACK_P):
                px[x * BACK_P + i, y * BACK_P + j] = color + (255,)

    for y in range(BACK_H):
        for x in range(BACK_W):
            corner = (x in (0, BACK_W - 1)) and (y in (0, BACK_H - 1))
            if corner: continue
            if x in (0, BACK_W - 1) or y in (0, BACK_H - 1):
                color = BLACK
            elif x == BACK_W - 2 or y == BACK_H - 2:
                color = s['shade']
            elif x == 1 or y == 1:
                color = s['ring']
            elif x == 2 or y == 2 or x == BACK_W - 3 or y == BACK_H - 3:
                color = s['inner']
            else:
                ix, iy = x - 3, y - 3
                if ix in (0, 12) or iy in (0, 20):
                    color = s['body']
                else:
                    color = back_pattern(style, s, ix, iy) or s['body']
            put(x, y, color)
    return img


# ---------------------------------------------------------------------------
# Feutre (32 x 32 pixels de jeu, répété)
# ---------------------------------------------------------------------------

FELT_N, FELT_P = 32, 3


def felt_pixel(style, s, x, y, rng):
    base, dark, light = s['felt']
    noise = rng.random()
    color = dark if noise < 0.12 else light if noise > 0.9 else base
    if style == 'neon':
        if x % 16 == 0 or y % 16 == 0: return s['felt_a']
        if (x % 16 == 8 and y % 16 == 8): return s['felt_b']
        return color
    if style == 'bois':
        # planches horizontales de 8 pixels, joints sombres, nervures qui ondulent
        if y % 8 == 7: return s['felt_a']
        if x == (y // 8 * 11) % 32 and y % 8 != 7: return s['felt_a']
        grain = (y * 5 + int(3 * math.sin((x + y // 8 * 9) * 2 * math.pi / 32))) % 8
        if grain == 2: return s['felt_b'] if noise < 0.7 else color
        return color
    if style == 'or':
        # losanges de velours, coutures dorées
        if (x + y) % 16 == 0 or (x - y) % 16 == 0: return s['felt_b']
        if (x + y) % 16 == 8 and (x - y) % 16 == 8: return s['felt_a']
        return color
    if style == 'abysses':
        # reflets d'eau qui ondulent, quelques bulles
        wave = int(round(2 * math.sin(x * 2 * math.pi / 32 * 2 + y // 8)))
        if (y + wave) % 8 == 0: return s['felt_a']
        if (x, y) in ((5, 12), (21, 4), (26, 22), (12, 27)): return s['felt_b']
        return color
    raise ValueError(style)


def make_felt(style, s):
    rng = random.Random(sum(map(ord, style)))
    img = Image.new('RGBA', (FELT_N * FELT_P, FELT_N * FELT_P))
    px = img.load()
    for y in range(FELT_N):
        for x in range(FELT_N):
            color = felt_pixel(style, s, x, y, rng) + (255,)
            for i in range(FELT_P):
                for j in range(FELT_P):
                    px[x * FELT_P + i, y * FELT_P + j] = color
    return img


# ---------------------------------------------------------------------------
# Machine : couleurs de la table d'origine remplacées
# ---------------------------------------------------------------------------

FRAME_MAP = lambda s: {
    (255, 214, 84): s['metal'][0], (218, 165, 32): s['metal'][1], (150, 100, 14): s['metal'][2],
    (30, 12, 10): s['outline'],
    (70, 70, 78): s['window'][0], (40, 38, 44): s['window'][1], (22, 20, 24): s['window'][2],
}
CELL_MAP = lambda s: {
    (200, 188, 160): s['cell'][0], (244, 236, 216): s['cell'][1], (255, 250, 236): s['cell'][2],
    (30, 12, 10): s['outline'],
}


def recolor(path, mapping):
    img = Image.open(path).convert('RGBA')
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a and (r, g, b) in mapping: px[x, y] = mapping[(r, g, b)] + (a,)
    return img


def main():
    table = os.path.join(ROOT, 'table')
    for style, s in STYLES.items():
        out = os.path.join(OUT_DIR, style)
        os.makedirs(out, exist_ok=True)
        make_back(style, s).save(os.path.join(out, 'back.png'))
        make_felt(style, s).save(os.path.join(out, 'felt.png'))
        recolor(os.path.join(table, 'reel_frame.png'), FRAME_MAP(s)).save(os.path.join(out, 'reel_frame.png'))
        recolor(os.path.join(table, 'reel_cell.png'), CELL_MAP(s)).save(os.path.join(out, 'reel_cell.png'))
        print('cosmetics/' + style)


if __name__ == '__main__':
    main()
