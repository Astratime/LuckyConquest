"""Miniatures des petits Jeux bonus (Roue, Coffres, Ticket à gratter) pour l'écran Cinématique.
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
WOOD, WOOD_D, GREEN = (122, 74, 36, 255), (74, 42, 18, 255), (60, 180, 90, 255)
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


def chests():
    im, d = base(0.36)
    for j in range(2):
        for i in range(3):
            x, y = 9 + i * 7, 18 + j * 9
            opened = (i + j) % 2 == 0
            d.rectangle([x, y + 3, x + 5, y + 6], fill=WOOD)
            d.line([(x, y + 4), (x + 5, y + 4)], fill=GOLD_D)
            if opened:
                d.rectangle([x, y - 1, x + 5, y], fill=WOOD_D)
                d.rectangle([x + 1, y + 2, x + 4, y + 2], fill=GOLD_L)
                d.point((x + 2, y + 1), fill=GOLD)
            else:
                d.rectangle([x, y + 1, x + 5, y + 2], fill=WOOD_D)
                d.point((x + 2, y + 3), fill=GOLD)
    return im


def ticket():
    im, d = base(0.6)
    d.rectangle([10, 16, 28, 36], fill=GOLD_D)
    d.rectangle([11, 17, 27, 35], fill=CREAM)
    d.rectangle([11, 17, 27, 18], fill=GREEN)
    for j in range(3):
        for i in range(3):
            x, y = 12 + i * 5, 20 + j * 5
            if (i, j) in ((0, 0), (1, 1), (2, 2)):
                d.rectangle([x, y, x + 3, y + 3], fill=GOLD)
                d.point((x + 1, y + 1), fill=GOLD_L)
            elif (i + j) % 3 == 0:
                d.rectangle([x, y, x + 3, y + 3], fill=INK)
                d.point((x + 2, y + 2), fill=RED)
            else:
                d.rectangle([x, y, x + 3, y + 3], fill=SILVER)
                d.line([(x + 1, y + 1), (x + 1, y + 2)], fill=(125, 133, 142, 255))
    return im


if __name__ == '__main__':
    for name, draw in (('jeu_bonus_roue', wheel), ('jeu_bonus_coffres', chests), ('jeu_bonus_ticket', ticket)):
        draw().resize((40 * SCALE, 56 * SCALE), Image.NEAREST).save(os.path.join(ROOT, name + '.png'))
        print(name)
