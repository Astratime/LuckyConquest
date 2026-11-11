"""Miniatures des petits Jeux bonus (Roue, Plinko, Course de chevaux) pour l'écran Cinématique.
Même cadre de machine que cards/test/jeu_bonus.png (40 x 56, agrandi x12), teinté par jeu, et la fenêtre
redessinée. Lancement : python3 tools/cards/classic/bonus_games.py (depuis la racine du dépôt)."""
import colorsys
import math
import os
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '..', 'assets', 'cards', 'test')
SCALE = 12
INK, GOLD, GOLD_D, GOLD_L = (28, 15, 36, 255), (240, 200, 60, 255), (150, 95, 20, 255), (255, 229, 138, 255)
RED, RED_D, CREAM, SILVER = (220, 40, 60, 255), (110, 15, 30, 255), (246, 238, 220, 255), (185, 192, 200, 255)
GREEN = (63, 158, 74, 255)
PURPLES = {(70, 15, 85), (110, 30, 130), (85, 20, 100), (70, 10, 90)}


def base(hue):
    """Le cadre du Jeu bonus, ses violets tournés vers la teinte {@code hue} (0-1), la fenêtre vidée."""
    im = Image.open(os.path.join(ROOT, 'jeu_bonus.png')).convert('RGBA').resize((40, 56), Image.NEAREST)
    px = im.load()
    for y in range(56):
        for x in range(40):
            r, g, b, a = px[x, y]
            if (r, g, b) in PURPLES:
                h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
                nr, ng, nb = colorsys.hls_to_rgb(hue, l, s)
                px[x, y] = (round(nr * 255), round(ng * 255), round(nb * 255), a)
    d = ImageDraw.Draw(im)
    d.rectangle([8, 15, 30, 37], fill=INK)
    return im, d


def wheel():
    im, d = base(0.98)
    cx, cy, r = 19, 26, 10
    for i in range(10):
        a0 = i * 36
        d.pieslice([cx - r, cy - r, cx + r, cy + r], a0, a0 + 36, fill=RED if i % 2 == 0 else RED_D)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=GOLD)
    for i in range(10):
        a = math.radians(i * 36)
        d.point((round(cx + math.cos(a) * (r - 2)), round(cy + math.sin(a) * (r - 2))), fill=GOLD_L)
    d.rectangle([cx - 1, cy - 1, cx + 1, cy + 1], fill=GOLD)
    d.polygon([(cx - 2, 15), (cx + 2, 15), (cx, 18)], fill=CREAM)
    return im


def plinko():
    im, d = base(0.36)
    # les clous en triangle, la bille dorée, les cases du bas (bords rouges, centre bleu)
    for row in range(5):
        for i in range(row + 3):
            x = 19 + (i - (row + 2) / 2) * 4
            d.point((round(x), 18 + row * 3), fill=SILVER)
    d.rectangle([20, 22, 21, 23], fill=GOLD_L)
    d.point((21, 22), fill=CREAM)
    colors = [RED, (232, 112, 29, 255), (232, 184, 29, 255), GREEN, (47, 111, 200, 255),
              GREEN, (232, 184, 29, 255), (232, 112, 29, 255), RED]
    for k, color in enumerate(colors):
        x = 9 + k * 2 + (1 if k > 4 else 0)
        d.rectangle([x, 32, x + 1, 36], fill=color)
    d.line([(8, 31), (30, 31)], fill=GOLD_D)
    return im


HORSE = [
    "......jj....",
    ".....jjj..m.",
    ".m..bbbjbmbb",
    "mmbbbbbbbbbh",
    "m.bbbbbbbb..",
    "..bb....bb..",
    ".b..b..b..b.",
]


def horses():
    im, d = base(0.6)
    turf, turf_d = (47, 122, 62, 255), (37, 99, 47, 255)
    silks = [(232, 51, 63, 255), (47, 127, 232, 255), (242, 197, 34, 255)]
    for lane in range(3):
        y = 16 + lane * 7
        d.rectangle([8, y, 30, y + 6], fill=turf if lane % 2 == 0 else turf_d)
        d.line([(8, y + 6), (30, y + 6)], fill=CREAM)
        ox = 10 + lane * 4
        for ry, row in enumerate(HORSE):
            for rx, c in enumerate(row):
                color = {'b': (138, 90, 46, 255), 'm': (58, 36, 20, 255), 'j': silks[lane], 'h': (26, 16, 16, 255)}.get(c)
                if color and y + ry - 1 <= y + 5:
                    d.point((ox + rx, y + ry - 1), fill=color)
    for k in range(11):
        d.point((29, 16 + k * 2), fill=CREAM)
        d.point((30, 17 + k * 2), fill=CREAM)
    return im


if __name__ == '__main__':
    for name, draw in (('jeu_bonus_roue', wheel), ('jeu_bonus_plinko', plinko), ('jeu_bonus_chevaux', horses)):
        draw().resize((40 * SCALE, 56 * SCALE), Image.NEAREST).save(os.path.join(ROOT, name + '.png'))
        print(name)
