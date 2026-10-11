"""Cartes spéciales (lot w2) au style des cartes classiques : Port des Contrebandiers, gains, rouleaux.
Pas de texte, pas de chiffre. Voir CONSIGNES.md et base.py."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
P, PD = hexc('#9a4ad8'), hexc('#5e2c8f')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
O = hexc('#f08a2a')
S, SD = hexc('#f6dcbc'), hexc('#d8aa84')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ec8f8')
W1, W2, W3 = hexc('#c07c44'), hexc('#8a5228'), hexc('#5e3418')
GR, GRD, GRL = hexc('#9aa4b0'), hexc('#5c6470'), hexc('#dfe6ee')
PA, PAD = hexc('#f6e8c4'), hexc('#d8bc84')
DK, DK2 = hexc('#2e2a40'), hexc('#46405e')
SICK, SICKD = hexc('#9cc65a'), hexc('#5e8c34')
GH, GHD = hexc('#eef2ff'), hexc('#b4bee0')
WH = WHITE


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def start(pal):
    im = card(*pal)
    L = layer()
    return im, L, ImageDraw.Draw(L)


def finish(im, L, corner, cpal):
    clip(im, L)
    corners(im, corner, cpal)
    return im


def coin(d, x0, y0, x1, y1):
    d.ellipse([x0, y0, x1, y1], fill=Y)


def gear(d, cx, cy, r, col=Y, dark=YD):
    for k in range(8):
        a = k * math.pi / 4
        tx, ty = cx + (r + 1) * math.cos(a), cy + (r + 1) * math.sin(a)
        d.rectangle([round(tx) - 1, round(ty) - 1, round(tx), round(ty)], fill=col)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=col)
    d.ellipse([cx - r + 2, cy - r + 2, cx + r - 2, cy + r - 2], fill=dark)


def cherry(d, x, y):
    """Petite paire de cerises (symbole de rouleau), 8x8 environ."""
    d.line([(x + 2, y + 4), (x + 4, y)], fill=GD)
    d.line([(x + 6, y + 4), (x + 4, y)], fill=GD)
    d.point((x + 5, y), fill=G)
    d.ellipse([x, y + 4, x + 3, y + 7], fill=R)
    d.ellipse([x + 5, y + 4, x + 8, y + 7], fill=R)
    d.point([(x + 1, y + 5), (x + 6, y + 5)], fill=RL)


def mug(d, x0, y0, x1, y1, handle='r'):
    """Chope en bois cerclée, mousse débordante."""
    if handle == 'r':
        d.rectangle([x1, y0 + 4, x1 + 3, y1 - 4], fill=W2)
        d.rectangle([x1 + 1, y0 + 6, x1 + 1, y1 - 6], fill=(0, 0, 0, 0))
    elif handle == 'l':
        d.rectangle([x0 - 3, y0 + 4, x0, y1 - 4], fill=W2)
        d.rectangle([x0 - 1, y0 + 6, x0 - 1, y1 - 6], fill=(0, 0, 0, 0))
    d.rectangle([x0, y0, x1, y1], fill=W1)
    for x in range(x0 + 2, x1, 3):
        d.line([(x, y0 + 2), (x, y1)], fill=W2)
    d.line([(x0 + 1, y0 + 2), (x0 + 1, y1)], fill=hexc('#dca06a'))
    for yy in (y0 + 3, y1 - 2):
        d.line([(x0, yy), (x1, yy)], fill=GR)
        d.line([(x0, yy + 1), (x1, yy + 1)], fill=GRD)
    # mousse
    d.ellipse([x0 - 1, y0 - 3, x0 + 3, y0 + 1], fill=WH)
    d.ellipse([x0 + 2, y0 - 4, x1 - 2, y0 + 1], fill=WH)
    d.ellipse([x1 - 3, y0 - 3, x1 + 1, y0 + 1], fill=WH)
    d.rectangle([x0, y0, x1, y0 + 1], fill=WH)
    d.rectangle([x0 + 2, y0 + 2, x0 + 3, y0 + 4], fill=WH)


# ================================================================ Cale
def piege_a_rats(pal):
    """Tapette à rats : planche en bois, arceau d'acier, morceau de fromage ; le rat arrive."""
    im, L, d = start(pal)
    # planche
    d.rectangle([8, 35, 30, 40], fill=W1)
    d.line([(8, 39), (30, 39)], fill=W2)
    d.line([(8, 40), (30, 40)], fill=W3)
    d.point([(12, 37), (20, 36), (26, 37)], fill=W2)
    # arceau
    d.line([(10, 35), (10, 27), (28, 27), (28, 35)], fill=GR, width=1)
    d.line([(11, 27), (27, 27)], fill=GRL)
    d.rectangle([17, 33, 21, 34], fill=GRD)
    # fromage
    poly(d, [(15, 34), (23, 34), (23, 30), (17, 29)], Y)
    d.line([(17, 29), (23, 30)], fill=YL)
    # rat
    d.ellipse([10, 13, 24, 23], fill=GR)
    poly(d, [(22, 15), (29, 19), (22, 22)], GR)
    d.ellipse([12, 11, 16, 15], fill=hexc('#e8a0b0'))
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(10, 21), (8, 24), (7, 22)], fill=hexc('#e8a0b0'))
    d.point([(24, 17)], fill=INK)
    d.point([(29, 19)], fill=hexc('#e8a0b0'))
    d.line([(26, 21), (30, 22)], fill=GRD)
    d.line([(26, 17), (30, 15)], fill=GRD)
    d.point([(19, 31), (21, 32)], fill=YD)
    d.point([(14, 13)], fill=SD)
    d.line([(12, 21), (21, 21)], fill=GRD)
    return finish(im, L, ['....y', '..yyy', 'yyyyy', 'ydyyd'], {'y': Y, 'd': YD})


def rhum(pal):
    """Bouteille de rhum à tête de mort, un petit verre et un trèfle porte-bonheur."""
    im, L, d = start(pal)
    # verre
    d.rectangle([25, 35, 30, 42], fill=PA)
    d.rectangle([25, 38, 30, 42], fill=O)
    # bouteille
    d.rectangle([16, 8, 21, 11], fill=W2)
    d.rectangle([17, 11, 20, 18], fill=W1)
    poly(d, [(17, 18), (20, 18), (24, 22), (24, 43), (13, 43), (13, 22)], W1)
    d.line([(15, 22), (15, 42)], fill=hexc('#dca06a'))
    d.line([(23, 23), (23, 42)], fill=W2)
    d.rectangle([13, 27, 24, 34], fill=PA)
    d.line([(13, 34), (24, 34)], fill=PAD)
    # trèfle
    for (cx, cy) in [(9, 36), (11, 36), (9, 38), (11, 38)]:
        d.ellipse([cx - 1, cy - 1, cx + 1, cy + 1], fill=G)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(10, 39), (10, 42)], fill=GD)
    d.point((10, 37), fill=GD)
    d.point([(17, 9), (17, 12)], fill=hexc('#dca06a'))
    # tête de mort de l'étiquette
    sprite(d, 16, 28, ['.kkk.', 'kpkpk', 'kkkkk', '.k.k.', 'k...k'],
           {'k': INK, 'p': PA})
    d.line([(26, 37), (29, 37)], fill=YL)
    d.point((26, 36), fill=WH)
    return finish(im, L, ['.kk.', '.oo.', 'oooo', 'oyyo', 'oooo'], {'k': W2, 'o': W1, 'y': PA})


def carte_au_tresor(pal):
    """Parchemin enroulé : une île, le chemin en pointillés et la croix rouge."""
    im, L, d = start(pal)
    d.rectangle([9, 12, 29, 40], fill=PA)
    d.rectangle([7, 9, 31, 13], fill=PAD)
    d.rectangle([7, 39, 31, 43], fill=PAD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(7, 11), (31, 11)], fill=PA)
    d.line([(7, 41), (31, 41)], fill=PA)
    d.line([(9, 12), (29, 12)], fill=INK)
    d.line([(9, 40), (29, 40)], fill=INK)
    # île
    d.ellipse([12, 17, 27, 35], fill=G)
    d.ellipse([12, 17, 27, 35], outline=GD)
    d.ellipse([16, 21, 22, 27], fill=GL)
    # vagues
    for x in (10, 25):
        d.point([(x, 37), (x + 1, 36), (x + 2, 37)], fill=B)
    d.point([(26, 15), (27, 14), (28, 15)], fill=B)
    # chemin et croix
    for (x, y) in [(14, 32), (15, 30), (16, 28), (18, 27), (20, 28), (21, 26)]:
        d.point((x, y), fill=INK)
    d.line([(21, 19), (25, 23)], fill=R, width=2)
    d.line([(25, 19), (21, 23)], fill=R, width=2)
    d.point([(21, 19), (25, 19), (21, 23), (25, 23)], fill=RD)
    # palmier
    d.line([(15, 22), (15, 25)], fill=W2)
    d.point([(14, 21), (16, 21), (13, 22), (17, 22)], fill=GD)
    return finish(im, L, ['r...r', '.r.r.', '..r..', '.r.r.', 'r...r'], {'r': R})


def scorbut(pal):
    """Tête de pirate verdâtre au bandana rouge, dents gâtées ; un citron barré."""
    im, L, d = start(pal)
    d.ellipse([10, 13, 28, 33], fill=SICK)
    poly(d, [(10, 21), (12, 14), (19, 11), (26, 14), (28, 21)], R)
    poly(d, [(27, 17), (32, 14), (32, 21)], R)
    # citron
    d.ellipse([8, 36, 15, 43], fill=hexc('#e8e04a'))
    # bulles
    d.ellipse([25, 37, 28, 40], fill=SICK)
    d.ellipse([29, 32, 31, 34], fill=SICK)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.point([(14, 15), (20, 13), (24, 16), (17, 18)], fill=WH)
    d.line([(10, 21), (28, 21)], fill=RD)
    d.line([(29, 17), (31, 19)], fill=RD)
    # yeux exorbités
    for ex in (15, 23):
        d.rectangle([ex - 1, 23, ex + 1, 25], fill=hexc('#fff6c8'))
        d.point((ex, 24), fill=INK)
    d.line([(13, 22), (17, 22)], fill=SICKD)
    # bouche
    d.rectangle([15, 28, 23, 30], fill=INK)
    d.point([(16, 28), (19, 28), (22, 28), (17, 30), (21, 30)], fill=hexc('#e8e0a0'))
    d.point([(12, 27), (26, 26), (13, 30)], fill=SICKD)
    d.point([(26, 38)], fill=hexc('#d4f0a0'))
    d.point([(10, 37)], fill=WH)
    # croix sur le citron
    d.line([(8, 36), (15, 43)], fill=R)
    d.line([(15, 36), (8, 43)], fill=R)
    return finish(im, L, ['..g..', '.ggg.', 'ggggg', 'gglgg', '.ggg.'], {'g': SICKD, 'l': SICK})


# ================================================================ Taverne
def chope(pal):
    """Une grande chope de bière à la mousse débordante."""
    im, L, d = start(pal)
    mug(d, 11, 17, 25, 41)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # gouttes de mousse
    d.point([(12, 18), (12, 19)], fill=WH)
    for (x, y) in [(7, 12), (30, 12)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    d.point([(19, 9)], fill=Y)
    return finish(im, L, ['.www.', 'wwwww', 'oooob', 'ooo.b', 'ooob.'], {'w': WH, 'o': W1, 'b': W2})


def tournee_generale(pal):
    """Tournée générale : trois chopes levées ensemble, celle du milieu devant."""
    im, L, d = start(pal)
    mug(d, 9, 22, 16, 36, handle='l')
    mug(d, 22, 22, 29, 36, handle='r')
    L = outline(L)
    d = ImageDraw.Draw(L)
    M = layer()
    md = ImageDraw.Draw(M)
    mug(md, 14, 27, 23, 43, handle=None)
    M = outline(M)
    L.alpha_composite(M)
    d = ImageDraw.Draw(L)
    # étincelles de trinquée
    for (x, y) in [(9, 15), (29, 15), (19, 17)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    d.point([(16, 14), (22, 14), (19, 12)], fill=Y)
    return finish(im, L, ['ww.ww', 'oo.oo', 'oo.oo', 'oo.oo'], {'w': WH, 'o': W1})


def fut_de_poudre(pal):
    """Tonneau de poudre cerclé de fer, tête de mort, mèche allumée."""
    im, L, d = start(pal)
    poly(d, [(12, 17), (26, 17), (29, 23), (29, 36), (26, 42), (12, 42), (9, 36), (9, 23)], W1)
    for x in (13, 17, 21, 25):
        d.line([(x, 18), (x, 41)], fill=W2)
    d.line([(11, 22), (11, 37)], fill=hexc('#dca06a'))
    for yy in (20, 38):
        d.line([(9 if yy != 20 else 10, yy), (28 if yy == 20 else 28, yy)], fill=GRD)
        d.line([(10, yy + 1), (28, yy + 1)], fill=GRD)
    d.rectangle([9, 29, 29, 30], fill=GRD)
    d.ellipse([12, 15, 26, 19], fill=W2)
    # étiquette
    d.rectangle([15, 25, 23, 34], fill=PA)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.ellipse([13, 16, 25, 18], fill=W3)
    sprite(d, 16, 26, ['.kkkk.', 'kkkkkk', 'k.kk.k', 'kkkkkk', '.k..k.', 'k....k', '.k..k.'],
           {'k': INK})
    d.point([(17, 28), (20, 28)], fill=PA)
    # mèche
    d.line([(19, 17), (20, 13), (23, 11), (24, 9)], fill=INK)
    d.line([(20, 16), (21, 13)], fill=PAD)
    # étincelle
    d.point([(25, 8), (24, 7), (26, 6), (27, 8), (23, 6)], fill=Y)
    d.point([(25, 7), (24, 8)], fill=O)
    d.point([(25, 6), (27, 6), (28, 9), (22, 8)], fill=R)
    d.point((25, 5), fill=YL)
    return finish(im, L, ['..y..', '.yoy.', 'yorоy'.replace('о', 'o'), '.ror.', '..r..'],
                  {'y': Y, 'o': O, 'r': R})


# ================================================================ Phare
def longue_vue(pal):
    """Longue-vue en laiton déployée en diagonale, lentille bleue."""
    im, L, d = start(pal)
    # du gros bout (haut droite) à l'oculaire (bas gauche)
    def seg(x0, y0, x1, y1, w, col):
        d.line([(x0, y0), (x1, y1)], fill=col, width=w)
    seg(10, 41, 15, 34, 4, Y)
    seg(14, 35, 21, 26, 5, W1)
    seg(20, 27, 27, 17, 6, Y)
    seg(26, 18, 29, 14, 7, YD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(9, 41), (14, 34)], fill=YL)
    d.line([(19, 26), (25, 17)], fill=YL)
    d.line([(15, 33), (20, 26)], fill=hexc('#dca06a'))
    # bagues
    for (x, y) in [(14, 36), (20, 28)]:
        d.line([(x - 2, y - 1), (x + 2, y + 2)], fill=YD)
    d.ellipse([26, 12, 31, 17], fill=BL)
    d.ellipse([26, 12, 31, 17], outline=INK)
    d.point([(27, 13), (28, 13)], fill=WH)
    # reflets
    for (x, y) in [(10, 14), (25, 37)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    return finish(im, L, ['.bbb.', 'bwllb', 'bllbb', 'bllbb', '.bbb.'], {'b': INK, 'l': BL, 'w': WH})


def lanterne(pal):
    """Lanterne de marin : anse, chapeau de fer, flamme derrière la vitre."""
    im, L, d = start(pal)
    d.ellipse([15, 7, 23, 14], fill=GRD)
    d.ellipse([17, 9, 21, 13], fill=(0, 0, 0, 0))
    poly(d, [(13, 15), (25, 15), (27, 19), (11, 19)], DK2)
    d.rectangle([12, 19, 26, 37], fill=YL)
    d.rectangle([11, 37, 27, 41], fill=DK2)
    d.rectangle([12, 19, 13, 37], fill=DK2)
    d.rectangle([25, 19, 26, 37], fill=DK2)
    d.rectangle([18, 19, 20, 20], fill=DK2)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.rectangle([14, 21, 24, 36], fill=Y)
    d.ellipse([16, 24, 22, 35], fill=O)
    poly(d, [(19, 22), (21, 30), (19, 33), (17, 30)], YL)
    d.point([(19, 31), (19, 32)], fill=WH)
    d.line([(14, 21), (14, 36)], fill=YL)
    d.line([(13, 16), (24, 16)], fill=GR)
    d.line([(12, 38), (26, 38)], fill=GR)
    # rayons
    for (x, y) in [(7, 24), (31, 24), (7, 33), (31, 33)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    return finish(im, L, ['..y..', '.yy..', '.yoy.', 'yoooy', '.yoy.'], {'y': Y, 'o': O})


def rayon_du_phare(pal):
    """Phare rayé rouge et blanc sur son rocher, faisceaux éblouissants."""
    im, L, d = start(pal)
    beams = layer()
    bd = ImageDraw.Draw(beams)
    poly(bd, [(17, 14), (7, 10), (7, 19)], YL)
    poly(bd, [(21, 14), (31, 10), (31, 19)], YL)
    # rocher
    d.ellipse([9, 38, 29, 46], fill=GR)
    # tour
    poly(d, [(16, 18), (22, 18), (24, 41), (14, 41)], WH)
    for (y0, y1) in [(22, 25), (29, 32), (36, 39)]:
        poly(d, [(14 + (41 - y0) * 0.087, y0), (24 - (41 - y0) * 0.087, y0),
                 (24 - (41 - y1) * 0.087, y1), (14 + (41 - y1) * 0.087, y1)], R)
    d.rectangle([14, 16, 24, 18], fill=DK2)
    d.rectangle([16, 11, 22, 16], fill=Y)
    poly(d, [(15, 11), (19, 6), (23, 11)], R)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(18, 12), (18, 15)], fill=DK2)
    d.line([(20, 12), (20, 15)], fill=DK2)
    d.point([(17, 12), (21, 12)], fill=YL)
    d.rectangle([18, 37, 20, 40], fill=W3)
    d.line([(17, 19), (16, 40)], fill=GRL)
    d.line([(12, 43), (16, 41)], fill=GRD)
    d.point([(25, 43), (26, 42)], fill=GRD)
    d.point((19, 5), fill=Y)
    beams.alpha_composite(L)
    return finish(im, beams, ['y...y', '.yyy.', '.yry.', '.yyy.', 'y...y'], {'y': Y, 'r': R})


# ================================================================ Galion
def sabre_d_abordage(pal):
    """Sabre d'abordage : lame courbe d'acier, garde en coquille dorée."""
    im, L, d = start(pal)
    # lame courbe du bas-gauche vers le haut-droite
    pts_a, pts_b = [], []
    for k in range(13):
        t = k / 12
        x = 15 + 15 * t
        y = 32 - 23 * t - 3 * math.sin(math.pi * t)
        w = 3.2 * (1 - t) + 0.6
        pts_a.append((x - w * 0.6, y - w * 0.4))
        pts_b.append((x + w * 0.6, y + w * 0.4))
    poly(d, pts_a + pts_b[::-1], GRL)
    d.line([(round(x), round(y)) for x, y in pts_b], fill=GR)
    # poignée et pommeau
    d.line([(9, 41), (14, 34)], fill=W2, width=3)
    d.ellipse([7, 40, 10, 43], fill=Y)
    # coquille
    d.ellipse([10, 28, 20, 38], fill=Y)
    d.ellipse([11, 29, 15, 33], fill=YL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(12, 36), (17, 31)], fill=YD)
    d.line([(9, 39), (10, 38)], fill=W1)
    d.line([(10, 37), (16, 31)], fill=INK)
    d.line([(10, 37), (13, 39)], fill=INK)
    d.line([(20, 25), (28, 12)], fill=WH)
    for (x, y) in [(28, 30), (9, 16)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=GRL)
    return finish(im, L, ['....w', '...w.', '..w..', 'yy...', 'yy...'], {'w': GR, 'y': Y})


def pavillon_noir(pal):
    """Pavillon noir à tête de mort et tibias croisés, sur sa hampe."""
    im, L, d = start(pal)
    d.rectangle([9, 13, 10, 44], fill=W2)
    d.ellipse([8, 10, 11, 13], fill=Y)
    pts = [(11, 14)]
    for k in range(9):
        x = 11 + 20 * k / 8
        pts.append((x, 15 + 1.5 * math.sin(k * 0.9)))
    for k in range(8, -1, -1):
        x = 11 + 20 * k / 8
        pts.append((x, 36 + 1.5 * math.sin(k * 0.9)))
    poly(d, pts, DK)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # os croisés
    d.line([(14, 27), (28, 35)], fill=WH, width=2)
    d.line([(28, 27), (14, 35)], fill=WH, width=2)
    for (x, y) in [(14, 27), (28, 27), (14, 35), (28, 35)]:
        d.rectangle([x - 1, y - 1, x, y], fill=WH)
    # crâne
    d.ellipse([16, 17, 26, 27], fill=WH)
    d.rectangle([18, 26, 24, 29], fill=WH)
    d.rectangle([18, 21, 20, 23], fill=INK)
    d.rectangle([22, 21, 24, 23], fill=INK)
    d.point((21, 25), fill=INK)
    d.point([(19, 28), (21, 28), (23, 28)], fill=DK)
    d.point((9, 11), fill=YL)
    d.line([(12, 17), (12, 35)], fill=DK2)
    return finish(im, L, ['.www.', 'wkwkw', 'wwwww', '.w.w.'], {'w': WH, 'k': INK})


def mutinerie(pal):
    """Mutinerie : un poing levé qui brise ses chaînes."""
    im, L, d = start(pal)
    # bras et manche
    d.rectangle([15, 28, 23, 44], fill=W1)
    d.rectangle([14, 27, 24, 29], fill=hexc('#e8e0c8'))
    # poing
    d.ellipse([12, 13, 26, 28], fill=S)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # doigts
    for x in (15, 18, 21):
        d.line([(x, 15), (x, 19)], fill=SD)
    d.line([(13, 20), (25, 20)], fill=SD)
    poly(d, [(12, 21), (17, 21), (17, 25), (13, 24)], S)
    d.line([(12, 21), (17, 21), (17, 25)], fill=INK)
    d.point([(14, 14), (17, 14), (20, 14)], fill=WH)
    d.line([(19, 30), (19, 44)], fill=W2)
    # bracelet de fer
    d.rectangle([14, 31, 24, 33], fill=GR)
    d.line([(14, 31), (24, 31)], fill=GRL)
    d.rectangle([13, 30, 25, 34], outline=INK)
    # chaînes brisées
    for side in (-1, 1):
        cx = 19 + side * 7
        for k in range(3):
            x = cx + side * k * 3
            y = 33 + k * 2
            d.rectangle([x - 1, y - 1, x + 1, y + 1], outline=GRD)
            d.point((x, y), fill=GRL)
        # éclats
        ex = cx + side * 10
        d.point([(ex, 37), (ex + side, 35), (ex - side, 40)], fill=Y)
    for (x, y) in [(8, 13), (30, 13), (19, 8)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    return finish(im, L, ['gg...', 'g.g..', '.g.y.', '...gg', '...gg'], {'g': GRD, 'y': Y})


# ================================================================ Gains
def bribe(pal):
    """Pot-de-vin : une enveloppe bourrée de billets glissée entre deux mains."""
    im, L, d = start(pal)
    # manches
    poly(d, [(6, 44), (6, 37), (11, 32), (14, 35), (11, 44)], BD)
    poly(d, [(31, 41), (31, 33), (27, 29), (24, 33), (27, 41)], BD)
    # billets
    poly(d, [(12, 20), (24, 13), (27, 19), (15, 26)], G)
    poly(d, [(14, 21), (22, 12), (27, 15), (18, 25)], GL)
    # enveloppe
    poly(d, [(8, 25), (28, 20), (31, 32), (11, 37)], PA)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(8, 25), (20, 31), (28, 20)], fill=PAD)
    d.line([(11, 36), (19, 31)], fill=PAD)
    d.line([(30, 31), (21, 30)], fill=PAD)
    d.ellipse([18, 28, 21, 31], fill=R)
    d.point([(21, 15), (20, 17), (22, 18)], fill=GD)
    M = layer()
    md = ImageDraw.Draw(M)
    md.ellipse([8, 29, 15, 36], fill=S)
    md.ellipse([24, 25, 31, 32], fill=S)
    M = outline(M)
    L.alpha_composite(M)
    d = ImageDraw.Draw(L)
    d.point([(10, 31), (26, 27)], fill=WH)
    d.line([(10, 34), (13, 34)], fill=SD)
    d.line([(26, 30), (29, 30)], fill=SD)
    for (x, y) in [(19, 9), (31, 13)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    return finish(im, L, ['ggggg', 'glylg', 'ggggg'], {'g': G, 'l': GL, 'y': GD})


def double_or_nothing(pal):
    """Double ou rien : une pièce coupée en deux, moitié or brillant, moitié vide et sombre."""
    im, L, d = start(pal)
    d.ellipse([8, 15, 30, 37], fill=Y)
    L = outline(L)
    import numpy as np
    arr = np.array(L)
    m = (arr[:, :, 3] > 0) & (arr[:, :, 0] == Y[0]) & (np.arange(W)[None, :] >= 19)
    arr[m] = PD
    L = Image.fromarray(arr)
    d = ImageDraw.Draw(L)
    d.arc([10, 17, 28, 35], 90, 270, fill=YD)
    d.arc([10, 17, 28, 35], 270, 90, fill=P)
    d.line([(19, 15), (19, 37)], fill=INK)
    # deux petites pièces côté or, un rond vide côté sombre
    for (x, y) in [(13, 21), (12, 27)]:
        d.ellipse([x, y, x + 4, y + 3], fill=YL)
        d.ellipse([x, y, x + 4, y + 3], outline=YD)
    d.ellipse([22, 23, 26, 28], outline=hexc('#c4a0f0'))
    d.point([(11, 19), (12, 18)], fill=WH)
    for (x, y) in [(9, 11), (30, 41)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    for (x, y) in [(30, 11), (9, 41)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=P)
    return finish(im, L, ['.kkk.', 'kyykk', 'kyypk', 'kyypk', '.kkk.'], {'k': INK, 'y': Y, 'p': PD})


def all_in(pal):
    """Tapis : toutes les piles de jetons poussées au centre."""
    im, L, d = start(pal)
    cols = [(R, RD), (B, BD), (Y, YD), (DK2, DK)]
    piles = [(7, 30, 6), (13, 24, 9), (19, 27, 7), (25, 21, 10)]
    piles = [(6, 6), (12, 10), (19, 8), (25, 12)]
    for pi, (x, n) in enumerate(piles):
        P_ = layer()
        pd_ = ImageDraw.Draw(P_)
        for k in range(n):
            y = 42 - 2 * k
            c, cd = cols[(k * 3 + pi) % 4]
            dx = [0, 1, 0, -1][(k + pi) % 4] if k < n - 1 else 0
            pd_.rectangle([x + dx, y - 1, x + 6 + dx, y], fill=c)
            pd_.line([(x + dx, y), (x + 6 + dx, y)], fill=cd)
            pd_.point([(x + 1 + dx, y - 1), (x + 5 + dx, y - 1)], fill=WH)
        y = 42 - 2 * n
        c, cd = cols[(n * 3 + pi) % 4]
        pd_.rectangle([x, y - 1, x + 6, y + 1], fill=c)
        pd_.rectangle([x + 2, y, x + 4, y], fill=WH)
        P_ = outline(P_)
        L.alpha_composite(P_)
    d = ImageDraw.Draw(L)
    for (x, y) in [(9, 22), (19, 14), (30, 13)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    return finish(im, L, ['.rrr.', 'rwrwr', 'rrrrr', '.ddd.'], {'r': R, 'w': WH, 'd': RD})


def rank_token(pal):
    """Jeton de rang : une médaille d'or à l'étoile, rubans rouge et bleu."""
    im, L, d = start(pal)
    poly(d, [(14, 27), (19, 30), (15, 43), (13, 40), (10, 42)], R)
    poly(d, [(24, 27), (19, 30), (23, 43), (25, 40), (28, 42)], B)
    L = outline(L)
    M = layer()
    md = ImageDraw.Draw(M)
    md.ellipse([9, 10, 29, 30], fill=Y)
    M = outline(M)
    L.alpha_composite(M)
    d = ImageDraw.Draw(L)
    d.ellipse([11, 12, 27, 28], outline=YD)
    star = []
    for k in range(10):
        a = -math.pi / 2 + k * math.pi / 5
        r = 6.5 if k % 2 == 0 else 2.8
        star.append((19 + r * math.cos(a), 20.5 + r * math.sin(a)))
    poly(d, star, YL)
    d.point([(19, 15), (18, 17), (19, 17)], fill=WH)
    d.line([(14, 29), (16, 39)], fill=RD)
    d.line([(24, 29), (22, 39)], fill=BD)
    for (x, y) in [(7, 12), (31, 12), (30, 30)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=Y)
    return finish(im, L, ['.yyy.', 'yylyy', '.yyy.', 'rr.bb', 'r...b'], {'y': Y, 'l': YL, 'r': R, 'b': B})


# ================================================================ Rouleaux
def rigged_reel(pal):
    """Rouleau truqué : une bande de rouleau aux trois symboles identiques, rouages et tournevis."""
    im, L, d = start(pal)
    gear(d, 9, 21, 3)
    gear(d, 29, 14, 2)
    gear(d, 28, 31, 3)
    d.rectangle([12, 9, 26, 41], fill=WH)
    d.rectangle([12, 9, 26, 10], fill=GR)
    d.rectangle([12, 40, 26, 41], fill=GR)
    d.line([(25, 11), (25, 39)], fill=GRL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for y in (12, 22, 31):
        cherry(d, 15, y)
    d.line([(13, 21), (24, 21)], fill=GRL)
    d.line([(13, 30), (24, 30)], fill=GRL)
    # tournevis
    T = layer()
    td = ImageDraw.Draw(T)
    td.line([(8, 44), (19, 37)], fill=GRL, width=1)
    td.line([(19, 37), (25, 33)], fill=R, width=3)
    td.point([(24, 33)], fill=RL)
    T = outline(T)
    L.alpha_composite(T)
    d = ImageDraw.Draw(L)
    d.line([(20, 36), (24, 33)], fill=RL)
    return finish(im, L, ['.y.y.', 'yyyyy', '.ydy.', 'yyyyy', '.y.y.'], {'y': Y, 'd': YD})


def ghost_reel(pal):
    """Rouleau fantôme : un petit fantôme pâle qui laisse voir un symbole de rouleau en lui."""
    im, L, d = start(pal)
    d.ellipse([11, 9, 27, 25], fill=GH)
    d.rectangle([11, 17, 27, 38], fill=GH)
    for k in range(5):
        x = 11 + k * 4
        poly(d, [(x, 38), (x + 4, 38), (x + 2, 42)], GH)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(26, 17), (26, 37)], fill=GHD)
    d.ellipse([14, 16, 17, 20], fill=INK)
    d.ellipse([21, 16, 24, 20], fill=INK)
    d.point([(15, 17), (22, 17)], fill=WH)
    d.ellipse([18, 21, 20, 24], fill=INK)
    d.point([(14, 22), (24, 22)], fill=hexc('#f4b8c8'))
    # symbole fantôme (cerises pâlies)
    d.line([(17, 31), (19, 27)], fill=hexc('#a8d8a8'))
    d.line([(21, 31), (19, 27)], fill=hexc('#a8d8a8'))
    d.ellipse([15, 31, 18, 34], fill=hexc('#f0a8b0'))
    d.ellipse([20, 31, 23, 34], fill=hexc('#f0a8b0'))
    for (x, y) in [(8, 14), (30, 26), (8, 33)]:
        d.point([(x, y - 1), (x - 1, y), (x, y), (x + 1, y), (x, y + 1)], fill=GHD)
    return finish(im, L, ['.kkk.', 'kgggk', 'gkgkg', 'ggggg', 'g.g.g'],
                  {'k': INK, 'g': GHD})


DESIGNS = {
    'piege_a_rats': piege_a_rats, 'rhum': rhum, 'carte_au_tresor': carte_au_tresor, 'scorbut': scorbut,
    'chope': chope, 'tournee_generale': tournee_generale, 'fut_de_poudre': fut_de_poudre,
    'longue_vue': longue_vue, 'lanterne': lanterne, 'rayon_du_phare': rayon_du_phare,
    'sabre_d_abordage': sabre_d_abordage, 'pavillon_noir': pavillon_noir, 'mutinerie': mutinerie,
    'bribe': bribe, 'double_or_nothing': double_or_nothing, 'all_in': all_in, 'rank_token': rank_token,
    'rigged_reel': rigged_reel, 'ghost_reel': ghost_reel,
}
