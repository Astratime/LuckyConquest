"""Cartes spéciales (lot 6) : les trois Étoiles de la boutique (2026-11-13), cousines de l'As de Trèfle :
elles vident la Bonne étoile pour un Bingo d'attaque, de défense ou de gains. Style des cartes classiques
(voir base.py et README.md). Aucun texte, aucun chiffre."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401

G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
ST, STD, STL = hexc('#9aa6b4'), hexc('#5e6878'), hexc('#d4dce4')
R, RD = hexc('#d8333f'), hexc('#8e1420')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ab0f0')
WD, WDD = hexc('#a0643a'), hexc('#6a3e22')

CLOVER = ['.g.g.', 'ggggg', '.ggg.', 'ggggg', '..g..']


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def star_points(cx, cy, r_out, r_in, turn=-90):
    pts = []
    for i in range(10):
        r = r_out if i % 2 == 0 else r_in
        a = math.radians(turn + i * 36)
        pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return pts


def star(L, cx, cy, r=8):
    """Une étoile dorée à cinq branches, contour encre, reflet clair en haut à gauche."""
    S = layer()
    d = ImageDraw.Draw(S)
    poly(d, star_points(cx, cy, r, r * 0.45), Y)
    S = outline(S)
    d = ImageDraw.Draw(S)
    poly(d, star_points(cx + 1, cy + 1, r * 0.45, r * 0.22), YD)
    d.point([(cx - 1, cy - 2), (cx - 2, cy - 1), (cx - 1, cy - 1)], fill=YL)
    d.point((cx, cy - r + 2), fill=YL)
    L.alpha_composite(S)


def sparkle(d, x, y, col=YL):
    d.point([(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)], fill=col)
    d.point((x, y), fill=WHITE)


def finish(im, L):
    clip(im, L)
    corners(im, CLOVER, {'g': GD})
    return im


def etoile_filante(pal):
    """Une étoile qui file en diagonale, sa traînée derrière elle, au-dessus d'une épée dressée."""
    im = card(*pal)
    L = layer()
    # épée
    S = layer()
    d = ImageDraw.Draw(S)
    d.rectangle([18, 22, 20, 38], fill=STL)
    poly(d, [(18, 22), (19, 19), (20, 22)], STL)
    d.rectangle([14, 38, 24, 39], fill=Y)
    d.rectangle([18, 40, 20, 44], fill=WD)
    d.rectangle([17, 45, 21, 46], fill=Y)
    S = outline(S)
    d = ImageDraw.Draw(S)
    d.line([(20, 23), (20, 37)], fill=ST)
    d.line([(14, 39), (24, 39)], fill=YD)
    L.alpha_composite(S)
    # traînée
    d = ImageDraw.Draw(L)
    for k in range(9):
        d.line([(8 + k, 30 - k * 1.6), (10 + k, 30 - k * 1.6)], fill=YL if k % 2 else Y)
    for k in range(6):
        d.point((7 + k, 25 - k * 1.5), fill=Y)
        d.point((12 + k, 31 - k * 1.4), fill=YD)
    star(L, 24, 14, 8)
    d = ImageDraw.Draw(L)
    sparkle(d, 9, 12)
    sparkle(d, 29, 30)
    return finish(im, L)


def etoile_du_berger(pal):
    """Un bouclier bleu au centre, une grande étoile posée dessus qui veille."""
    im = card(*pal)
    L = layer()
    S = layer()
    d = ImageDraw.Draw(S)
    poly(d, [(9, 18), (29, 18), (29, 32), (19, 45), (9, 32)], B)
    S = outline(S)
    d = ImageDraw.Draw(S)
    poly(d, [(11, 20), (19, 20), (19, 42), (11, 31)], BL)
    poly(d, [(19, 20), (27, 20), (27, 31), (19, 42)], B)
    d.line([(19, 20), (19, 42)], fill=BD)
    d.line([(10, 19), (28, 19)], fill=BL)
    L.alpha_composite(S)
    star(L, 19, 28, 7)
    star(L, 19, 11, 4)
    d = ImageDraw.Draw(L)
    sparkle(d, 9, 10)
    sparkle(d, 29, 11)
    return finish(im, L)


def etoile_d_or(pal):
    """Une grande étoile d'or au-dessus de trois piles de pièces."""
    im = card(*pal)
    L = layer()
    S = layer()
    d = ImageDraw.Draw(S)
    for x0, h in ((7, 3), (15, 5), (23, 4)):
        for k in range(h):
            y = 44 - k * 3
            d.ellipse([x0, y - 2, x0 + 8, y + 1], fill=Y)
    S = outline(S)
    d = ImageDraw.Draw(S)
    for x0, h in ((7, 3), (15, 5), (23, 4)):
        for k in range(h):
            y = 44 - k * 3
            d.line([(x0 + 1, y + 1), (x0 + 7, y + 1)], fill=YD)
            d.line([(x0 + 2, y - 2), (x0 + 5, y - 2)], fill=YL)
    L.alpha_composite(S)
    star(L, 19, 15, 9)
    d = ImageDraw.Draw(L)
    sparkle(d, 8, 22)
    sparkle(d, 30, 24)
    sparkle(d, 30, 8)
    return finish(im, L)


DESIGNS = {'etoile_filante': etoile_filante, 'etoile_du_berger': etoile_du_berger, 'etoile_d_or': etoile_d_or}
