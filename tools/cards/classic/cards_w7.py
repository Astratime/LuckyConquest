"""Cartes spéciales (lot 7) : les trois cartes à usure de la boutique (demande d'Astra du 2026-11-14), qui changent
pendant le combat : la Bougie (fond à chaque pose), la Graine (pousse quand on ne la joue pas) et la Pile ou face
(pile : effet doublé, face : rien). Style des cartes classiques (voir base.py et README.md). Aucun texte, aucun chiffre."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401

Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
OR, ORD = hexc('#f08a24'), hexc('#c0461c')
R = hexc('#d8333f')
WAX, WAXD, WAXL = hexc('#f4ecd8'), hexc('#cbbd9c'), hexc('#ffffff')
G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
EARTH, EARTHD, EARTHL = hexc('#8a5a34'), hexc('#5a361c'), hexc('#b07a4a')
SEED, SEEDD = hexc('#d8a860'), hexc('#9a6a30')
CY = hexc('#9ee4ff')


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def ob(fn):
    """Dessine un objet sur un calque à part et l'entoure d'encre."""
    L = layer()
    fn(ImageDraw.Draw(L))
    return outline(L)


def stack(*layers):
    L = layer()
    for lay in layers:
        L.alpha_composite(lay)
    return L


def finish(pal, L, idx, ipal):
    im = card(*pal)
    clip(im, L)
    corners(im, idx, ipal)
    return im


def sparkle(d, x, y, col=WHITE):
    d.point([(x, y), (x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)], fill=col)


def bougie(pal):
    """Une bougie de cire sur un bougeoir de laiton, la flamme haute, des coulures de cire qui fondent."""
    def holder(d):
        d.ellipse([7, 40, 31, 47], fill=Y)
        d.ellipse([9, 41, 29, 45], fill=YL)
        d.rectangle([14, 37, 24, 41], fill=Y)
        d.line([(14, 41), (24, 41)], fill=YD)
        d.arc([27, 34, 35, 44], 270, 90, fill=YD, width=2)
    H = ob(holder)

    def candle(d):
        d.rectangle([15, 16, 23, 37], fill=WAX)
        d.line([(23, 16), (23, 37)], fill=WAXD)
        d.line([(15, 16), (15, 37)], fill=WAXL)
        d.ellipse([15, 14, 23, 18], fill=WAXL)
        # coulures
        for x, y1 in ((16, 22), (19, 26), (22, 20)):
            d.line([(x, 17), (x, y1)], fill=WAXL)
            d.point((x, y1 + 1), fill=WAXD)
    C = ob(candle)

    def flame(d):
        poly(d, [(19, 3), (23, 10), (22, 13), (16, 13), (15, 10)], OR)
        poly(d, [(19, 6), (21, 10), (20, 12), (18, 12), (17, 10)], Y)
        d.point([(19, 10), (19, 11)], fill=YL)
    F = ob(flame)
    L = stack(H, C, F)
    d = ImageDraw.Draw(L)
    d.line([(19, 13), (19, 15)], fill=INK)
    sparkle(d, 9, 9, YL)
    sparkle(d, 29, 12, YL)
    # une goutte de cire qui tombe
    d.point([(25, 30), (25, 31)], fill=WAX)
    return finish(pal, L, ['..o..', '.ooo.', '.oyo.', '..w..', '..w..'], {'o': OR, 'y': Y, 'w': WAXD})


def graine(pal):
    """Une graine dans une motte de terre, d'où sort une jeune pousse à deux feuilles qui grandit."""
    def soil(d):
        d.chord([5, 34, 33, 52], 180, 360, fill=EARTH)
        d.rectangle([5, 43, 33, 46], fill=EARTH)
        d.line([(8, 39), (14, 36)], fill=EARTHL)
    S = ob(soil)

    def seed(d):
        d.ellipse([14, 37, 24, 44], fill=SEED)
        d.line([(16, 43), (23, 39)], fill=SEEDD)
        d.point((16, 39), fill=WHITE)
    Se = ob(seed)

    def sprout(d):
        d.line([(19, 37), (19, 17)], fill=G, width=2)
        poly(d, [(19, 25), (11, 18), (6, 19), (9, 25), (16, 28)], G)
        poly(d, [(20, 19), (27, 11), (33, 11), (31, 18), (23, 22)], G)
        d.line([(18, 26), (9, 20)], fill=GD)
        d.line([(21, 20), (31, 12)], fill=GD)
        poly(d, [(19, 17), (16, 11), (19, 7), (22, 11)], GL)
    P = ob(sprout)
    L = stack(P, S, Se)
    d = ImageDraw.Draw(L)
    for x, y in ((9, 45), (27, 42), (12, 41), (30, 46)):
        d.point((x, y), fill=EARTHD)
    sparkle(d, 8, 10, GL)
    sparkle(d, 29, 26, WHITE)
    # gouttes d'eau
    for x, y in ((11, 30), (28, 30)):
        d.line([(x, y), (x, y + 1)], fill=CY)
    return finish(pal, L, ['..g..', '.ggg.', 'gg.gg', '..e..', '..e..'], {'g': G, 'e': EARTH})


def pile_ou_face(pal):
    """Une pièce d'or lancée en l'air, qui tourne sur elle-même au-dessus d'un pouce, avec ses traînées."""
    def coin(d):
        d.ellipse([9, 8, 29, 28], fill=YD)
        d.ellipse([9, 8, 28, 27], fill=Y)
        d.ellipse([12, 11, 26, 25], fill=YD)
        d.ellipse([12, 11, 25, 24], fill=Y)
        # côté pile : un trèfle en relief ; l'autre moitié reste lisse (face)
        d.ellipse([16, 13, 19, 16], fill=YL)
        d.ellipse([14, 16, 17, 19], fill=YL)
        d.ellipse([18, 16, 21, 19], fill=YL)
        d.line([(17, 19), (17, 22)], fill=YL)
        d.point([(11, 13), (12, 12)], fill=YL)
    Co = ob(coin)

    def hand(d):
        # un poing fermé, pouce levé, en bas de la carte
        d.rounded_rectangle([10, 38, 26, 49], 3, fill=hexc('#f2c29a'))
        d.rounded_rectangle([22, 33, 27, 41], 2, fill=hexc('#f2c29a'))
        for y in (41, 44, 47):
            d.line([(11, y), (20, y)], fill=hexc('#c88a62'))
        d.rectangle([8, 46, 28, 49], fill=hexc('#3a6fd8'))
    Ha = ob(hand)
    L = stack(Ha, Co)
    d = ImageDraw.Draw(L)
    # traînées de rotation
    d.arc([5, 4, 33, 32], 200, 260, fill=YL)
    d.arc([5, 4, 33, 32], 20, 80, fill=YL)
    for y in (30, 33):
        d.point((24, y), fill=Y)
    sparkle(d, 31, 6, WHITE)
    sparkle(d, 7, 30, YL)
    return finish(pal, L, ['.yyy.', 'yylyy', 'ylyly', 'yyyyy', '.yyy.'], {'y': Y, 'l': YD})


DESIGNS = {'bougie': bougie, 'graine': graine, 'pile_ou_face': pile_ou_face}
