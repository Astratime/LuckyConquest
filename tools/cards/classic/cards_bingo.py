"""Cartes « Bingo <symbole> » (offertes par la carte Bingo de l'échoppe) et carte du Jeu bonus.
Le symbole vient du rouleau du jeu (assets/symbols), réduit à la grille de la carte."""
import os
import numpy as np
from PIL import Image, ImageDraw
from base import card, clip, corners, layer, INK, WHITE, hexc

SYMBOLS = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '..', 'assets', 'symbols', '')
Y, YD, YL, R = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8'), hexc('#d8333f')
STAR = ['..y..', '.yyy.', 'yyyyy', '.yyy.', 'y...y']

BINGO_SYMBOLS = {
    'double_bar': '1-double_bar', 'cherry': '2-cherry', 'seven': '3-seven', 'bar': '4-bar', 'grape': '5-grape',
    'bell': '6-bell', 'diamond': '7-diamond', 'triple_cherry': '8-triple_cherry', 'triple_seven': '9-triple_seven',
    'gold_bar': '10-gold_bar', 'watermelon': '11-watermelon', 'horseshoe': '13-horseshoe', 'ecu': '14-ecu',
    'sword': '15-sword', 'heart': '16-heart', 'die': '17-die', 'star': '18-star', 'bomb': '19-bomb',
    'crown': '20-crown', 'nugget': '21-nugget',
}


def symbol_sprite(name, max_w=24, max_h=21):
    """Le symbole du rouleau sans son fond, réduit en gardant sa palette (pas de flou)."""
    a = np.array(Image.open(SYMBOLS + name + '.png').convert('RGBA'))[::4, ::4].copy()   # grille 36x30
    bg = a[0, 0, :3].astype(int)
    a[(np.abs(a[:, :, :3].astype(int) - bg).sum(2) == 0)] = 0
    im = Image.fromarray(a)
    im = im.crop(im.getbbox())
    k = min(max_w / im.width, max_h / im.height, 1)
    w, h = max(1, round(im.width * k)), max(1, round(im.height * k))
    small = im.resize((w, h), Image.NEAREST)
    return small


def frame(pal, art):
    im = card(*pal)
    lay = layer()
    d = ImageDraw.Draw(lay)
    # enseigne lumineuse
    d.rectangle([11, 7, 27, 10], fill=INK)
    for i, x in enumerate(range(13, 27, 3)):
        d.rectangle([x, 8, x + 1, 9], fill=YL if i % 2 == 0 else Y)
    # fenêtre du rouleau
    d.rectangle([5, 13, 32, 40], fill=INK)
    d.rectangle([6, 14, 31, 39], fill=YD)
    d.rectangle([7, 15, 30, 38], fill=WHITE)
    art(lay, d)
    # trois voyants allumés : trois symboles pareils
    for x in (13, 18, 23):
        d.rectangle([x, 43, x + 2, 45], fill=INK)
        d.point((x + 1, 44), fill=R)
    clip(im, lay)
    corners(im, STAR, {'y': Y})
    return im


def bar_slab(d, y, h=7):
    """Barre BAR sans ses lettres (le texte ne survit pas à la réduction, et pas de texte sur les cartes)."""
    d.rectangle([8, y, 29, y + h - 1], fill=INK)
    d.rectangle([9, y + 1, 28, y + h - 2], fill=hexc('#2a2838'))
    d.line([(10, y + 1), (27, y + 1)], fill=hexc('#5a5870'))
    for x in range(11, 27, 4):
        d.rectangle([x, y + 3, x + 1, y + h - 3], fill=hexc('#f4ead8'))


def gold_ingot(d):
    d.polygon([(11, 22), (26, 22), (30, 33), (7, 33)], fill=INK)
    d.polygon([(12, 23), (25, 23), (28, 32), (9, 32)], fill=Y)
    d.polygon([(12, 23), (25, 23), (24, 26), (13, 26)], fill=YL)
    d.line([(9, 32), (28, 32)], fill=YD)


def bingo_card(sym):
    def draw(pal):
        def art(lay, d):
            if sym == '4-bar':
                return bar_slab(d, 24)
            if sym == '1-double_bar':
                bar_slab(d, 19)
                return bar_slab(d, 29)
            if sym == '10-gold_bar':
                return gold_ingot(d)
            sp = symbol_sprite(sym)
            lay.alpha_composite(sp, (19 - sp.width // 2, 27 - sp.height // 2))
        return frame(pal, art)
    return draw


def jeu_bonus(pal):
    def art(lay, d):
        cols = [hexc('#d8333f'), hexc('#3a6fd8'), hexc('#f4ead8'), hexc('#3fae4a'), hexc('#f2c14e')]
        for j in range(6):
            for i in range(6):
                x, y = 8 + i * 4, 16 + j * 4
                lit = i == j
                d.rectangle([x, y, x + 2, y + 2], fill=YL if lit else INK)
                d.point((x + 1, y + 1), fill=R if lit else cols[(i * 2 + j * 3) % 5])
    return frame(pal, art)


DESIGNS = {'bingo_' + k: bingo_card(v) for k, v in BINGO_SYMBOLS.items()}
DESIGNS['jeu_bonus'] = jeu_bonus
