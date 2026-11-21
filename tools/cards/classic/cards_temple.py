"""Cartes des coffres du Temple du Destin (10 lieux, 2026-11-21) au style des cartes classiques.
Voir README.md : pas de texte ni de chiffre, contours INK, sujet centré, tout passe par clip().
Un motif d'angle par donjon : cloche (Parvis), lanterne (Jardin des oracles), dé (Salle des dés), oeil (Sanctuaire)."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401
from base import ImageDraw  # noqa: E402

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
O, OD = hexc('#f08a2a'), hexc('#b8541a')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ec0ff')
P, PD, PL = hexc('#8a4fc8'), hexc('#552a88'), hexc('#c8a0f0')
WD, WM, WL = hexc('#6b3e1e'), hexc('#9a5e2c'), hexc('#c8884a')
BR, BRD, BRL = hexc('#c8902a'), hexc('#7a5014'), hexc('#ffe39a')     # bronze
IV, IVD = hexc('#f6f0dc'), hexc('#c8bc9c')                           # ivoire
CO, COL = hexc('#2c2a3c'), hexc('#4a4860')
C, CD = hexc('#fbf6ea'), hexc('#d8ccb4')
WH = WHITE


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def start(pal):
    im = card(*pal)
    L = layer()
    return im, L, ImageDraw.Draw(L)


def finish(im, L, rows, cpal):
    clip(im, L)
    corners(im, rows, cpal)
    return im


def again(L):
    """Cerne ce qui est dessiné, puis rend un pinceau pour dessiner par-dessus."""
    L = outline(L)
    return L, ImageDraw.Draw(L)


def coin(d, x, y, r=2):
    d.ellipse([x - r, y - r, x + r, y + r], fill=Y)
    d.point((x - 1, y - 1), fill=YL)
    d.point((x + r - 1, y + r - 1), fill=YD)


def sparkle(d, x, y, col=YL):
    d.point([(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)], fill=col)
    d.point((x, y), fill=WH)


def card_back(d, x, y, w=8, h=11, col=B):
    d.rectangle([x, y, x + w, y + h], fill=C)
    d.rectangle([x + 1, y + 1, x + w - 1, y + h - 1], fill=col)
    for j in range(y + 2, y + h - 1, 2):
        for i in range(x + 2 + (j // 2) % 2, x + w - 1, 2):
            d.point((i, j), fill=BD)


def die(d, x, y, s, pips, col=IV, pip=R):
    """Un dé de face, côté `s`, avec ses points (liste de positions 0..2 en x et y)."""
    d.rectangle([x, y, x + s, y + s], fill=col)
    step = s / 4
    for i, j in pips:
        px, py = round(x + step * (i + 1)), round(y + step * (j + 1))
        d.rectangle([px - 1, py - 1, px, py], fill=pip)


SIX = [(0, 0), (0, 1), (0, 2), (2, 0), (2, 1), (2, 2)]
FIVE = [(0, 0), (2, 0), (1, 1), (0, 2), (2, 2)]

# --- motifs d'angle (5x5), un par donjon ---
BELL = ['..y..', '.yyy.', '.yyy.', 'yyyyy', '..d..']
BELL_P = {'y': BR, 'd': BRD}
LANTERN = ['.ddd.', 'rrrrr', 'ryyyr', 'rrrrr', '.ddd.']
LANTERN_P = {'d': CO, 'r': R, 'y': Y}
DIE = ['iiiii', 'iriii', 'iiiii', 'iiiri', 'iiiii']
DIE_P = {'i': IVD, 'r': R}
EYE = ['.yyy.', 'yckcy', 'ykkky', 'yckcy', '.yyy.']
EYE_P = {'y': Y, 'c': WH, 'k': CO}


# ================================================================ Le Parvis
def baton_d_encens(pal):
    """Bâton d'encens : trois bâtons dans un brûle-parfum de bronze, une fumée en coeur, un petit bouclier."""
    im, L, d = start(pal)
    d.ellipse([9, 34, 29, 44], fill=BR)
    d.rectangle([11, 38, 27, 44], fill=BR)
    for x in (11, 26):
        d.rectangle([x, 44, x + 2, 47], fill=BRD)
    L, d = again(L)
    d.line([(12, 39), (26, 39)], fill=BRL)
    for x, top in ((15, 22), (19, 18), (23, 22)):
        d.line([(x, 37), (x, top)], fill=WD)
        d.point((x, top), fill=O)
    for k in range(6):                                       # la fumée qui monte
        d.point((19 + round(3 * math.sin(k)), 16 - k), fill=CD)
    d.polygon([(19, 12), (15, 8), (15, 6), (17, 5), (19, 7), (21, 5), (23, 6), (23, 8)], fill=R)
    poly(d, [(26, 16), (31, 15), (31, 20), (28, 23), (25, 20)], B)
    d.point((27, 17), fill=BL)
    return finish(im, L, BELL, BELL_P)


def cloche_de_bronze(pal):
    """Cloche de bronze : une cloche de temple qui sonne, son battant, deux ondes, une fausse piste en zigzag."""
    im, L, d = start(pal)
    poly(d, [(13, 14), (25, 14), (28, 34), (31, 38), (7, 38), (10, 34)], BR)
    d.rectangle([17, 10, 21, 14], fill=BRD)
    L, d = again(L)
    d.line([(14, 16), (12, 34)], fill=BRL)
    d.line([(9, 34), (29, 34)], fill=BRD)
    d.ellipse([17, 38, 21, 42], fill=BRD)                    # le battant
    for r in (4, 7):                                         # le son
        d.arc([2 - r, 22 - r, 2 + r, 22 + r], 300, 60, fill=INK)
        d.arc([36 - r, 22 - r, 36 + r, 22 + r], 120, 240, fill=INK)
    d.line([(10, 45), (14, 42), (18, 46), (22, 42), (26, 46), (29, 43)], fill=R)
    return finish(im, L, BELL, BELL_P)


def maillet_de_gong(pal):
    """Maillet de gong (rare) : un gong de bronze sur son portique rouge, le maillet qui le frappe."""
    im, L, d = start(pal)
    d.rectangle([5, 8, 33, 11], fill=R)
    d.rectangle([7, 11, 9, 47], fill=R)
    d.rectangle([29, 11, 31, 47], fill=R)
    d.ellipse([10, 16, 28, 34], fill=BR)
    L, d = again(L)
    d.line([(6, 9), (32, 9)], fill=RL)
    d.ellipse([13, 19, 25, 31], fill=BRD)
    d.ellipse([14, 20, 24, 30], fill=BR)
    d.ellipse([17, 23, 21, 27], fill=Y)
    d.point((15, 21), fill=BRL)
    d.line([(14, 12), (16, 17)], fill=WL)
    d.line([(24, 12), (22, 17)], fill=WL)
    d.line([(22, 44), (31, 36)], fill=WM, width=2)          # le maillet
    d.ellipse([18, 39, 24, 45], fill=R)
    for x, y in ((10, 40), (27, 42)):
        sparkle(d, x, y)
    return finish(im, L, BELL, BELL_P)


# ================================================================ Le Jardin des oracles
def feuille_de_the(pal):
    """Feuille de thé : une tasse fumante, une feuille verte qui tourne (relance), une pièce."""
    im, L, d = start(pal)
    d.ellipse([8, 30, 28, 44], fill=WH)
    d.rectangle([8, 30, 28, 37], fill=WH)
    d.ellipse([26, 32, 32, 38], fill=WH)
    L, d = again(L)
    d.ellipse([10, 29, 26, 33], fill=G)
    d.ellipse([27, 33, 31, 37], fill=pal[0])
    d.line([(10, 40), (26, 40)], fill=B)
    poly(d, [(14, 12), (22, 8), (26, 14), (20, 20), (14, 18)], G)   # la feuille
    d.line([(14, 18), (24, 10)], fill=GD)
    for a in range(30, 300, 15):                             # la flèche qui relance
        r = math.radians(a)
        d.point((round(20 + 10 * math.cos(r)), round(14 - 8 * math.sin(r))), fill=GD)
    coin(d, 31, 24)
    return finish(im, L, LANTERN, LANTERN_P)


def boule_de_cristal(pal):
    """Boule de cristal : une boule bleue sur un socle d'or, deux cartes qui y apparaissent, un bouclier."""
    im, L, d = start(pal)
    d.ellipse([7, 10, 31, 34], fill=BL)
    poly(d, [(10, 33), (28, 33), (31, 42), (7, 42)], Y)
    L, d = again(L)
    d.ellipse([9, 12, 29, 32], fill=B)
    d.ellipse([11, 14, 17, 20], fill=BL)
    card_back(d, 13, 18, 5, 8, R)
    card_back(d, 20, 17, 5, 8, P)
    d.line([(9, 37), (29, 37)], fill=YD)
    poly(d, [(15, 43), (19, 42), (23, 43), (22, 47), (19, 49), (16, 47)], B)
    return finish(im, L, LANTERN, LANTERN_P)


def fil_d_or(pal):
    """Fil d'or (rare) : une bobine d'or dont le fil relie deux cartes identiques, des pièces."""
    im, L, d = start(pal)
    d.rectangle([13, 8, 25, 10], fill=WM)
    d.rectangle([13, 20, 25, 22], fill=WM)
    d.rectangle([15, 10, 23, 20], fill=Y)
    L, d = again(L)
    for y in range(11, 20, 2):
        d.line([(15, y), (23, y)], fill=YD)
    card_back(d, 7, 30, 8, 12, R)
    card_back(d, 23, 30, 8, 12, R)
    d.line([(19, 22), (19, 26), (11, 30)], fill=Y)
    d.line([(19, 26), (27, 30)], fill=Y)
    coin(d, 19, 44)
    sparkle(d, 31, 14)
    return finish(im, L, LANTERN, LANTERN_P)


# ================================================================ La Salle des dés
def de_d_ivoire(pal):
    """Dé d'ivoire : un grand dé d'ivoire sur sa face six, une épée derrière, une pièce."""
    im, L, d = start(pal)
    d.line([(8, 44), (30, 10)], fill=WH, width=3)            # l'épée
    d.rectangle([9, 18, 29, 38], fill=IV)
    L, d = again(L)
    d.line([(10, 19), (28, 19)], fill=WH)
    d.line([(28, 20), (28, 37)], fill=IVD)
    die(d, 9, 18, 20, SIX)
    coin(d, 9, 44)
    return finish(im, L, DIE, DIE_P)


def gobelet_a_des(pal):
    """Gobelet à dés : un gobelet de cuir renversé, deux dés qui roulent, le meilleur qui brille."""
    im, L, d = start(pal)
    poly(d, [(6, 12), (20, 8), (24, 24), (12, 28)], WM)
    d.rectangle([12, 32, 21, 41], fill=IV)
    d.rectangle([23, 34, 32, 43], fill=IV)
    L, d = again(L)
    poly(d, [(8, 13), (19, 10), (20, 14), (9, 17)], WL)
    d.line([(12, 27), (24, 23)], fill=WD)
    die(d, 12, 32, 9, [(1, 1)])
    die(d, 23, 34, 9, SIX)
    sparkle(d, 31, 31)
    sparkle(d, 22, 30)
    return finish(im, L, DIE, DIE_P)


def double_six(pal):
    """Double six (rare) : deux dés côte à côte sur leurs six, en or, des étoiles autour."""
    im, L, d = start(pal)
    d.rectangle([5, 20, 18, 33], fill=Y)
    d.rectangle([20, 18, 33, 31], fill=Y)
    L, d = again(L)
    die(d, 5, 20, 13, SIX, Y, R)
    die(d, 20, 18, 13, SIX, Y, R)
    d.line([(6, 21), (17, 21)], fill=YL)
    d.line([(21, 19), (32, 19)], fill=YL)
    for x, y in ((9, 12), (28, 10), (19, 40), (31, 38), (7, 39)):
        sparkle(d, x, y)
    return finish(im, L, DIE, DIE_P)


# ================================================================ Le Sanctuaire
def offrande_d_or(pal):
    """Offrande d'or : un plateau d'offrande, une pyramide de pièces, une flamme de bougie."""
    im, L, d = start(pal)
    d.ellipse([6, 36, 32, 44], fill=R)
    d.rectangle([16, 43, 22, 47], fill=RD)
    L, d = again(L)
    d.ellipse([8, 37, 30, 42], fill=RL)
    for row, (n, y) in enumerate(((5, 35), (4, 30), (3, 25), (2, 20), (1, 15))):
        for k in range(n):
            coin(d, 19 - (n - 1) * 2.5 + k * 5, y, 2)
    poly(d, [(19, 6), (21, 10), (19, 12), (17, 10)], O)
    d.point((19, 10), fill=YL)
    return finish(im, L, EYE, EYE_P)


def masque_sacre(pal):
    """Masque sacré : un masque d'or aux yeux clos, un bouclier rouge derrière lui."""
    im, L, d = start(pal)
    poly(d, [(6, 14), (32, 14), (32, 36), (19, 46), (6, 36)], R)
    d.ellipse([10, 12, 28, 40], fill=Y)
    L, d = again(L)
    poly(d, [(8, 16), (14, 16), (8, 22)], RL)
    d.ellipse([11, 13, 18, 20], fill=YL)
    d.line([(13, 24), (17, 25)], fill=INK)                   # les yeux clos
    d.line([(21, 25), (25, 24)], fill=INK)
    d.line([(19, 26), (19, 31)], fill=YD)
    d.line([(16, 34), (22, 34)], fill=RD)
    d.point((19, 17), fill=R)
    return finish(im, L, EYE, EYE_P)


def oeil_de_l_idole(pal):
    """Oeil de l'idole (rare) : un grand oeil d'or qui rayonne, une épée et un gong sous lui."""
    im, L, d = start(pal)
    poly(d, [(4, 22), (12, 15), (26, 15), (34, 22), (26, 29), (12, 29)], Y)
    d.ellipse([12, 34, 24, 46], fill=BR)
    L, d = again(L)
    d.ellipse([13, 16, 25, 28], fill=WH)
    d.ellipse([15, 18, 23, 26], fill=G)
    d.ellipse([17, 20, 21, 24], fill=CO)
    d.point((18, 20), fill=WH)
    for a in range(0, 360, 30):                              # les rayons
        r = math.radians(a)
        d.point((round(19 + 15 * math.cos(r)), round(22 - 11 * math.sin(r))), fill=YD)
    d.ellipse([16, 38, 20, 42], fill=Y)
    d.line([(26, 47), (32, 33)], fill=WH, width=2)
    return finish(im, L, EYE, EYE_P)


DESIGNS = {
    'baton_d_encens': baton_d_encens, 'cloche_de_bronze': cloche_de_bronze, 'maillet_de_gong': maillet_de_gong,
    'feuille_de_the': feuille_de_the, 'boule_de_cristal': boule_de_cristal, 'fil_d_or': fil_d_or,
    'de_d_ivoire': de_d_ivoire, 'gobelet_a_des': gobelet_a_des, 'double_six': double_six,
    'offrande_d_or': offrande_d_or, 'masque_sacre': masque_sacre, 'oeil_de_l_idole': oeil_de_l_idole,
}
