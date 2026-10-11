"""Les cinq Joker au format des cartes classiques (voir base.py), rendus par cards_jokers.py."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402

R, RD = hexc('#d8333f'), hexc('#8e1420')
G, GD = hexc('#3fae4a'), hexc('#23793a')
P, PD = hexc('#9a4ad8'), hexc('#5e2c8f')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
S, SD = hexc('#f6dcbc'), hexc('#d8aa84')
B, BD = hexc('#3a6fd8'), hexc('#24459a')
LIME, LIMED = hexc('#8ef05a'), hexc('#3fae4a')

# Couleurs de carte (liseré clair, face, ombre) quand on lance avec --couleurs
COLORED = '--couleurs' in sys.argv
PALETTES = {
    'souriant':    ('#e6fbf8', '#a8dcd8', '#6aa8a4'),   # turquoise
    'masque':      ('#ffeef0', '#e8b4bc', '#a8707c'),   # rose velours
    'cour':        ('#eef4ff', '#b8cdf0', '#7a92c0'),   # bleu de cour
    'malveillant': ('#f0e8fa', '#c4b0e0', '#8670ac'),   # violet
    'marotte':     ('#fff8e0', '#f0d890', '#b89c50'),   # or pâle
}


def colors(name):
    if not COLORED:
        return (FACE, WHITE, SHADOW)
    light, face, shadow = (hexc(c) for c in PALETTES[name])
    return (face, light, shadow)


# petit bonnet à trois pointes pour les index d'angle (5x5)
HAT = ['p.g.r', 'pygyr', '.yyy.', 'kwkwk']
HAT_PAL = {'p': P, 'g': G, 'r': R, 'y': Y, 'k': INK, 'w': WHITE}
STAR = ['..y..', '.yyy.', 'yyyyy', '.yyy.', 'y...y']
STAR_PAL = {'y': Y}


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


# ---------------------------------------------------------------- 1
def souriant():
    """Joker souriant : la tête du bouffon, bonnet à trois pointes et grelots."""
    im = card(*colors('souriant'))
    L = layer()
    d = ImageDraw.Draw(L)
    # pointes du bonnet
    poly(d, [(12, 25), (16, 24), (10, 16), (8, 14)], P)
    poly(d, [(16, 24), (22, 24), (19, 12)], G)
    poly(d, [(22, 24), (26, 25), (30, 14), (28, 16)], R)
    poly(d, [(13, 25), (15, 24), (11, 17)], PD)
    poly(d, [(20, 24), (22, 24), (19, 13)], GD)
    poly(d, [(24, 25), (26, 25), (28, 18)], RD)
    # bandeau en damier
    for i, x in enumerate(range(11, 28, 2)):
        d.rectangle([x, 24, x + 1, 25], fill=WHITE if i % 2 == 0 else INK)
    # visage
    d.ellipse([11, 25, 27, 39], fill=S)
    d.ellipse([20, 30, 26, 38], fill=S)
    d.point([(25, 34), (25, 35), (24, 37)], fill=SD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # grelots
    for (bx, by) in [(8, 13), (19, 10), (30, 13)]:
        d.rectangle([bx - 1, by - 1, bx + 1, by + 1], fill=Y)
        d.point((bx - 1, by - 1), fill=YL)
        d.point((bx + 1, by + 1), fill=YD)
    # yeux plissés, losanges peints, nez, sourire
    d.point([(14, 30), (15, 29), (16, 30), (22, 30), (23, 29), (24, 30)], fill=INK)
    d.point([(15, 32), (23, 32)], fill=P)
    d.point([(15, 33), (23, 33)], fill=R)
    d.rectangle([18, 31, 20, 33], fill=R)
    d.point((18, 31), fill=hexc('#ff9a9a'))
    d.line([(14, 34), (15, 36), (17, 37), (21, 37), (23, 36), (24, 34)], fill=INK)
    d.line([(16, 36), (22, 36)], fill=RD)
    # collerette
    for i, x in enumerate(range(11, 26, 3)):
        poly(d, [(x, 39), (x + 3, 39), (x + 1.5, 42)], [Y, R, G, P][i % 4])
    L = outline(L)
    clip(im, L)
    corners(im, HAT, HAT_PAL)
    return im


# ---------------------------------------------------------------- 2
def masque():
    """Masque du Joker : un loup mi-or mi-nuit, cimier à grelots, ruban."""
    im = card(*colors('masque'))
    L = layer()
    d = ImageDraw.Draw(L)
    # cimier
    poly(d, [(14, 22), (18, 22), (10, 12)], INK)
    poly(d, [(17, 22), (21, 22), (19, 9)], Y)
    poly(d, [(20, 22), (24, 22), (28, 12)], WHITE)
    poly(d, [(20, 22), (21, 22), (19, 11)], YD)
    # masque : moitié gauche or, droite nuit
    left = [(19, 22), (14, 22), (9, 24), (7, 28), (9, 32), (14, 34), (18, 33), (19, 31)]
    poly(d, left, Y)
    right = [(38 - x, y) for (x, y) in left]
    poly(d, right, PD)
    d.line([(10, 32), (14, 33), (18, 32)], fill=YD)
    d.point([(22, 25), (26, 24), (29, 27), (24, 31), (28, 31)], fill=Y)
    d.point([(10, 26), (11, 25)], fill=YL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # yeux en amande, vides
    for ex in (13, 25):
        d.rectangle([ex - 1, 27, ex + 1, 29], fill=INK)
        d.point([(ex - 2, 28), (ex + 2, 28)], fill=INK)
        d.point((ex + 1, 28), fill=YL)
    d.point([(13, 31), (25, 31)], fill=R)
    # grelots du cimier
    for (bx, by) in [(10, 11), (19, 8), (28, 11)]:
        d.rectangle([bx - 1, by - 1, bx + 1, by + 1], fill=Y)
        d.point((bx - 1, by - 1), fill=YL)
    # rubans et bâton
    d.line([(7, 30), (6, 34), (8, 38), (6, 41)], fill=R)
    d.line([(31, 30), (32, 34), (30, 38), (31, 40)], fill=R)
    d.line([(25, 34), (27, 43)], fill=YD)
    d.line([(26, 34), (28, 43)], fill=Y)
    L = outline(L)
    clip(im, L)
    corners(im, ['.r.r.', 'rrrrr', '.rrr.', '..r..'], {'r': R})
    return im


# ---------------------------------------------------------------- 3
def cour_half(d):
    # buste
    poly(d, [(8, 27), (11, 22), (27, 22), (30, 27)], R)
    for x in range(11, 27, 4):
        d.rectangle([x + 2, 22, x + 3, 27], fill=B)
    # collerette
    for i, x in enumerate(range(12, 26, 2)):
        poly(d, [(x, 20), (x + 2, 20), (x + 1, 23)], Y if i % 2 == 0 else R)
    # visage
    d.ellipse([14, 12, 24, 21], fill=S)
    # bonnet
    poly(d, [(14, 13), (19, 9), (19, 13)], R)
    poly(d, [(14, 13), (10, 9), (9, 6), (13, 7), (18, 9)], R)
    poly(d, [(24, 13), (19, 9), (19, 13)], B)
    poly(d, [(24, 13), (28, 9), (29, 6), (25, 7), (20, 9)], B)
    d.rectangle([13, 12, 25, 13], fill=Y)
    # marotte
    d.line([(29, 27), (30, 13)], fill=Y)
    d.ellipse([28, 9, 32, 13], fill=S)


def cour():
    """Joker de cour : figure de jeu de cartes à deux têtes, en aplats rouge, bleu et jaune."""
    pal = colors('cour')
    im = card(*pal)
    top = layer()
    d = ImageDraw.Draw(top)
    cour_half(d)
    top = outline(top)
    d = ImageDraw.Draw(top)
    d.point([(17, 16), (21, 16)], fill=INK)
    d.point([(19, 17)], fill=R)
    d.line([(17, 19), (21, 19)], fill=R)
    d.point([(16, 18), (22, 18)], fill=hexc('#f08a8a'))
    for (bx, by) in [(9, 6), (29, 6)]:
        d.rectangle([bx - 1, by - 1, bx, by], fill=Y)
    d.point([(29, 11), (31, 11)], fill=INK)
    # coupe la moitié haute à la diagonale du milieu
    import numpy as np
    arr = np.array(top)
    yy, xx = np.mgrid[0:H, 0:W]
    arr[yy > 27 - (xx - 19) * 0.12] = 0
    top = Image.fromarray(arr)
    both = layer()
    both.alpha_composite(top)
    both.alpha_composite(top.rotate(180))
    dd = ImageDraw.Draw(both)
    dd.line([(4, 29), (33, 25)], fill=INK)
    # médaillon étoile
    dd.ellipse([15, 23, 23, 31], fill=pal[0])
    dd.ellipse([15, 23, 23, 31], outline=INK)
    sprite(dd, 17, 25, STAR, STAR_PAL)
    clip(im, both)
    return im


# ---------------------------------------------------------------- 4
def malveillant():
    """Joker malveillant : face sombre dans l'ombre, yeux et sourire à crocs vert acide."""
    im = card(*colors('malveillant'))
    L = layer()
    d = ImageDraw.Draw(L)
    dark, dark2 = hexc('#2a1a3e'), hexc('#1a1028')
    poly(d, [(8, 26), (8, 15), (9, 12), (13, 16), (15, 22)], dark)
    poly(d, [(30, 26), (30, 15), (29, 12), (25, 16), (23, 22)], dark)
    poly(d, [(12, 24), (16, 13), (19, 6), (22, 13), (26, 24)], dark2)
    d.ellipse([7, 18, 31, 42], fill=dark)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # yeux en fente
    poly(d, [(11, 27), (17, 25), (16, 28), (12, 28)], LIME)
    poly(d, [(27, 27), (21, 25), (22, 28), (26, 28)], LIME)
    d.point([(14, 26), (14, 27), (24, 26), (24, 27)], fill=INK)
    # sourire immense, bouche noire et crocs
    pts_top = [(19 - 12 * math.cos(math.pi * k / 16), 31 + 2 * math.sin(math.pi * k / 16)) for k in range(17)]
    pts_bot = [(19 - 12 * math.cos(math.pi * k / 16), 31 + 9 * math.sin(math.pi * k / 16)) for k in range(16, -1, -1)]
    poly(d, pts_top + pts_bot, hexc('#05030a'))
    d.line([(round(x), round(y)) for x, y in pts_top], fill=LIME)
    d.line([(round(x), round(y)) for x, y in pts_bot], fill=LIMED)
    for k in range(1, 9):
        t = math.pi * k / 9
        x = round(19 - 12 * math.cos(t))
        y = round(31 + 2 * math.sin(t))
        d.line([(x, y), (x, y + (3 if k % 2 else 2))], fill=LIME)
    for k in range(1, 8):
        t = math.pi * (k + 0.5) / 9
        x = round(19 - 12 * math.cos(t))
        yb = round(31 + 9 * math.sin(t))
        d.line([(x, yb), (x, yb - 2)], fill=LIMED)
    d.point([(6, 30), (32, 30)], fill=LIME)
    # grelots verts
    for (bx, by) in [(9, 11), (19, 6), (29, 11)]:
        d.rectangle([bx - 1, by - 1, bx + 1, by + 1], fill=LIMED)
        d.point((bx - 1, by - 1), fill=LIME)
    clip(im, L)
    corners(im, ['..k..', '.kkk.', 'kkkkk', '..k..', '.kkk.'], {'k': INK})
    return im


# ---------------------------------------------------------------- 5
def marotte():
    """Marotte du Joker : le sceptre du fou, une petite tête à grelots au bout d'un bâton."""
    im = card(*colors('marotte'))
    L = layer()
    d = ImageDraw.Draw(L)
    # bâton en diagonale
    d.line([(24, 42), (20, 26)], fill=YD, width=2)
    d.line([(23, 42), (19, 26)], fill=Y)
    d.ellipse([21, 40, 25, 44], fill=Y)
    # rubans
    d.line([(19, 28), (14, 32), (12, 37), (9, 40)], fill=G)
    d.line([(20, 28), (24, 31), (27, 35), (30, 37)], fill=P)
    # collerette
    for i, x in enumerate(range(12, 27, 3)):
        poly(d, [(x, 24), (x + 3, 24), (x + 1.5, 28)], [R, Y, G, Y, P][i % 5])
    # tête
    d.ellipse([13, 13, 25, 25], fill=S)
    # bonnet à deux pointes tombantes
    poly(d, [(12, 16), (19, 9), (19, 15)], P)
    poly(d, [(12, 16), (8, 13), (6, 20), (10, 15)], P)
    poly(d, [(26, 16), (19, 9), (19, 15)], G)
    poly(d, [(26, 16), (30, 13), (32, 20), (28, 15)], G)
    d.rectangle([12, 14, 26, 15], fill=Y)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.point([(16, 19), (22, 19)], fill=INK)
    d.rectangle([18, 20, 20, 21], fill=R)
    d.line([(16, 22), (17, 23), (21, 23), (22, 22)], fill=INK)
    for (bx, by) in [(6, 21), (32, 21), (19, 8)]:
        d.rectangle([bx - 1, by - 1, bx + 1, by + 1], fill=Y)
        d.point((bx - 1, by - 1), fill=YL)
    L = outline(L)
    # petits grelots qui volent
    d = ImageDraw.Draw(L)
    for (bx, by) in [(9, 42), (31, 40)]:
        d.point((bx, by), fill=Y)
    clip(im, L)
    corners(im, HAT, HAT_PAL)
    return im


DESIGNS = [('1_joker_souriant', souriant), ('2_masque_du_joker', masque), ('3_joker_de_cour', cour),
           ('4_joker_malveillant', malveillant), ('5_marotte_du_joker', marotte)]
