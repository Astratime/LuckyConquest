"""Cartes des coffres du Manoir hanté (10 lieux, 2026-11-20) au style des cartes classiques.
Voir README.md : pas de texte ni de chiffre, contours INK, sujet centré, tout passe par clip().
Un motif d'angle par donjon : chandelier, livre, croix de tombe, clé de remontoir."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401
from base import ImageDraw  # noqa: E402

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD = hexc('#3fae4a'), hexc('#23793a')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
O = hexc('#f08a2a')
B, BL = hexc('#3a6fd8'), hexc('#8ec0ff')
V, VD, VL = hexc('#7a3ab0'), hexc('#4a1a72'), hexc('#c8a0f0')
WD, WM, WL = hexc('#6b3e1e'), hexc('#9a5e2c'), hexc('#c8884a')
ST, STD, STL = hexc('#8a8a98'), hexc('#5a5a68'), hexc('#c4c4d0')
CO = hexc('#2c2a3c')
C, CD = hexc('#fbf6ea'), hexc('#d8ccb4')
GH, GHD = hexc('#d8f4ec'), hexc('#8ed0bc')            # le vert pâle des spectres
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


def ghost(d, x, y, w=12, h=14):
    """Un petit fantôme : tête ronde, bas en dents de scie (le Spectre)."""
    d.ellipse([x, y, x + w, y + w], fill=GH)
    d.rectangle([x, y + w // 2, x + w, y + h - 2], fill=GH)
    for k in range(0, w, 3):
        poly(d, [(x + k, y + h - 2), (x + k + 1.5, y + h + 1), (x + k + 3, y + h - 2)], GH)


def ghost_face(d, x, y, w=12):
    d.rectangle([x + w // 3 - 1, y + w // 3, x + w // 3, y + w // 3 + 2], fill=CO)
    d.rectangle([x + 2 * w // 3, y + w // 3, x + 2 * w // 3 + 1, y + w // 3 + 2], fill=CO)
    d.ellipse([x + w // 2 - 1, y + w // 2 + 1, x + w // 2 + 1, y + w // 2 + 4], fill=CO)


def candle(d, x, y, h=12, col=C):
    d.rectangle([x, y, x + 3, y + h], fill=col)
    poly(d, [(x + 1.5, y - 5), (x - 0.5, y - 1), (x + 3.5, y - 1)], O)
    d.point((x + 1, y - 2), fill=YL)


def card_back(d, x, y, w=8, h=11, col=V):
    d.rectangle([x, y, x + w, y + h], fill=C)
    d.rectangle([x + 1, y + 1, x + w - 1, y + h - 1], fill=col)
    for j in range(y + 2, y + h - 1, 2):
        for i in range(x + 2 + (j // 2) % 2, x + w - 1, 2):
            d.point((i, j), fill=VD)


# --- motifs d'angle (5x5), un par donjon ---
CANDLES = ['o.o.o', 'c.c.c', 'c.c.c', 'yyyyy', '..y..']
CANDLES_P = {'o': O, 'c': C, 'y': Y}
BOOK = ['vvvv.', 'vccv.', 'vccv.', 'vccv.', 'vvvv.']
BOOK_P = {'v': V, 'c': C}
CROSS = ['..s..', 'sssss', '..s..', '..s..', '.sss.']
CROSS_P = {'s': STL}
KEY = ['.yy..', 'y..y.', '.yy..', '..y..', '..yy.']
KEY_P = {'y': Y}


# ================================================================ Le Salon de jeu
def bougie_noire(pal):
    """Bougie noire : une bougie sombre à la flamme verte, une carte qui sort de sa fumée."""
    im, L, d = start(pal)
    d.rectangle([16, 22, 22, 44], fill=CO)
    d.ellipse([13, 42, 25, 47], fill=STD)
    poly(d, [(19, 10), (15, 19), (19, 22), (23, 19)], G)
    L, d = again(L)
    poly(d, [(19, 13), (17, 19), (21, 19)], GH)
    d.line([(17, 25), (17, 40)], fill=STD)
    for k in range(3):
        d.point((24 + k, 14 - k * 2), fill=STL)
    card_back(d, 24, 22, 7, 10)
    return finish(im, L, CANDLES, CANDLES_P)


def mise_du_revenant(pal):
    """Mise du revenant : une main de fantôme pose une pile de jetons et un coeur sur le tapis."""
    im, L, d = start(pal)
    d.rectangle([6, 38, 31, 46], fill=G)
    ghost(d, 8, 8, 14, 16)
    for k in range(4):
        d.ellipse([22, 34 - k * 3, 30, 38 - k * 3], fill=R if k % 2 == 0 else C)
    L, d = again(L)
    ghost_face(d, 8, 8, 14)
    d.line([(6, 39), (31, 39)], fill=GD)
    poly(d, [(15, 33), (11, 29), (11, 27), (13, 26), (15, 28), (17, 26), (19, 27), (19, 29)], R)
    return finish(im, L, CANDLES, CANDLES_P)


def pacte_de_minuit(pal):
    """Pacte de minuit (rare) : un parchemin scellé de cire rouge sous une horloge à minuit, un spectre qui en sort."""
    im, L, d = start(pal)
    d.ellipse([18, 6, 32, 20], fill=C)
    d.rectangle([7, 26, 27, 44], fill=CD)
    d.ellipse([5, 24, 11, 30], fill=CD)
    d.ellipse([23, 40, 29, 46], fill=CD)
    L, d = again(L)
    d.line([(25, 13), (25, 8)], fill=CO)                     # les deux aiguilles sur minuit
    d.line([(25, 13), (26, 9)], fill=CO)
    for y in (30, 33, 36):
        d.line([(10, y), (23, y)], fill=WL)
    d.ellipse([19, 37, 25, 43], fill=R)
    d.point((21, 39), fill=RL)
    ghost(d, 7, 8, 9, 11)
    return finish(im, L, CANDLES, CANDLES_P)


# ================================================================ La Bibliothèque
def grimoire(pal):
    """Grimoire : un livre épais ouvert, une flèche ronde violette qui relance au-dessus des pages."""
    im, L, d = start(pal)
    poly(d, [(5, 32), (19, 35), (19, 46), (5, 43)], VD)
    poly(d, [(33, 32), (19, 35), (19, 46), (33, 43)], VD)
    poly(d, [(6, 31), (19, 34), (19, 44), (6, 41)], C)
    poly(d, [(32, 31), (19, 34), (19, 44), (32, 41)], C)
    L, d = again(L)
    for y in (35, 38):
        d.line([(8, y), (16, y + 1)], fill=CD)
        d.line([(22, y + 1), (30, y)], fill=CD)
    for a in range(20, 300, 12):
        r = math.radians(a)
        d.point((round(19 + 9 * math.cos(r)), round(17 - 7 * math.sin(r))), fill=V)
    poly(d, [(27, 12), (30, 16), (25, 17)], V)
    return finish(im, L, BOOK, BOOK_P)


def marque_page(pal):
    """Marque-page : un ruban rouge qui dépasse d'un livre fermé, deux cartes et une pièce glissées entre les pages."""
    im, L, d = start(pal)
    d.rectangle([8, 18, 30, 44], fill=V)
    d.rectangle([10, 20, 30, 42], fill=C)
    d.rectangle([8, 18, 28, 42], fill=V)
    L, d = again(L)
    d.line([(10, 20), (10, 40)], fill=VL)
    poly(d, [(20, 40), (24, 40), (24, 49), (22, 47), (20, 49)], R)
    card_back(d, 13, 8, 7, 11, B)
    card_back(d, 21, 7, 7, 11)
    coin(d, 16, 30, 3)
    return finish(im, L, BOOK, BOOK_P)


def livre_des_ombres(pal):
    """Livre des ombres (rare) : un livre noir aux fermoirs d'or, d'où s'échappent deux fantômes."""
    im, L, d = start(pal)
    d.rectangle([9, 26, 29, 46], fill=CO)
    ghost(d, 7, 7, 10, 12)
    ghost(d, 21, 11, 10, 12)
    L, d = again(L)
    ghost_face(d, 7, 7, 10)
    ghost_face(d, 21, 11, 10)
    d.rectangle([28, 30, 30, 33], fill=Y)
    d.rectangle([28, 39, 30, 42], fill=Y)
    poly(d, [(19, 30), (15, 36), (19, 42), (23, 36)], V)     # l'emblème
    d.point((19, 36), fill=VL)
    return finish(im, L, BOOK, BOOK_P)


# ================================================================ La Crypte
def os_de_la_chance(pal):
    """Os de la chance : un os en travers, un trèfle à quatre feuilles posé dessus, un petit bouclier."""
    im, L, d = start(pal)
    d.line([(9, 38), (29, 18)], fill=C, width=4)
    for x, y in [(8, 36), (11, 40), (27, 16), (31, 20)]:
        d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=C)
    L, d = again(L)
    for dx, dy in [(-3, 0), (3, 0), (0, -3), (0, 3)]:
        d.ellipse([19 + dx - 2, 28 + dy - 2, 19 + dx + 2, 28 + dy + 2], fill=G)
    d.point((19, 28), fill=GD)
    poly(d, [(8, 9), (16, 9), (16, 15), (12, 19), (8, 15)], STL)
    d.line([(12, 10), (12, 17)], fill=STD)
    return finish(im, L, CROSS, CROSS_P)


def eau_benite(pal):
    """Eau bénite : une fiole de verre bleu à la croix d'argent, des gouttes et un coeur de lumière."""
    im, L, d = start(pal)
    d.rectangle([16, 10, 22, 18], fill=BL)
    d.ellipse([9, 18, 29, 44], fill=BL)
    L, d = again(L)
    d.rectangle([16, 8, 22, 10], fill=WM)
    d.ellipse([11, 28, 27, 42], fill=B)
    d.line([(19, 22), (19, 32)], fill=WH)
    d.line([(15, 26), (23, 26)], fill=WH)
    poly(d, [(19, 40), (16, 37), (16, 35), (17.5, 34), (19, 35.5), (20.5, 34), (22, 35), (22, 37)], RL)
    for x, y in [(7, 14), (31, 20), (29, 9)]:
        poly(d, [(x, y - 2), (x - 1, y + 1), (x + 1, y + 1)], BL)
    return finish(im, L, CROSS, CROSS_P)


def pierre_tombale(pal):
    """Pierre tombale (rare) : une stèle grise, solide comme un bouclier, et un spectre qui se lève derrière."""
    im, L, d = start(pal)
    ghost(d, 18, 6, 13, 15)
    d.rectangle([8, 24, 26, 46], fill=ST)
    d.ellipse([8, 16, 26, 32], fill=ST)
    d.rectangle([5, 44, 33, 47], fill=WD)
    L, d = again(L)
    ghost_face(d, 18, 6, 13)
    d.line([(17, 22), (17, 36)], fill=STD, width=2)
    d.line([(13, 27), (21, 27)], fill=STD, width=2)
    d.line([(10, 25), (10, 42)], fill=STL)
    d.point((23, 39), fill=G)
    d.point((12, 43), fill=G)
    return finish(im, L, CROSS, CROSS_P)


# ================================================================ Le Grenier
def boite_a_musique(pal):
    """Boîte à musique : une boîte de bois ouverte, une petite danseuse dessus, la manivelle et une pièce."""
    im, L, d = start(pal)
    d.rectangle([8, 30, 30, 44], fill=WM)
    poly(d, [(8, 30), (30, 30), (34, 18), (12, 18)], WD)
    L, d = again(L)
    d.line([(8, 33), (30, 33)], fill=WL)
    d.rectangle([17, 36, 21, 40], fill=Y)
    d.line([(19, 30), (19, 22)], fill=STL)                   # la danseuse
    d.ellipse([17, 17, 21, 21], fill=C)
    poly(d, [(15, 26), (23, 26), (19, 22)], RL)
    d.line([(30, 38), (34, 38)], fill=STD)
    d.line([(34, 36), (34, 40)], fill=STD)
    coin(d, 9, 12, 3)
    return finish(im, L, KEY, KEY_P)


def malle_oubliee(pal):
    """Malle oubliée : une vieille malle cerclée de fer, le couvercle entrouvert sur des cartes et de l'or."""
    im, L, d = start(pal)
    d.rectangle([6, 28, 32, 45], fill=WM)
    poly(d, [(6, 28), (32, 28), (30, 18), (8, 18)], WD)
    L, d = again(L)
    for x in (10, 28):
        d.line([(x, 28), (x, 45)], fill=STD, width=2)
    d.rectangle([17, 30, 21, 34], fill=Y)
    card_back(d, 12, 11, 7, 10)
    card_back(d, 20, 10, 7, 10, B)
    coin(d, 15, 24)
    coin(d, 24, 24)
    return finish(im, L, KEY, KEY_P)


def miroir_hante(pal):
    """Miroir hanté (rare) : un grand miroir ovale au cadre d'or, un fantôme qui sourit dedans, des pièces qui tombent."""
    im, L, d = start(pal)
    d.ellipse([8, 6, 30, 42], fill=Y)
    d.rectangle([16, 40, 22, 46], fill=YD)
    L, d = again(L)
    d.ellipse([11, 9, 27, 39], fill=VD)
    ghost(d, 13, 15, 12, 15)
    ghost_face(d, 13, 15, 12)
    d.line([(13, 12), (16, 9)], fill=VL)
    coin(d, 7, 44)
    coin(d, 31, 44)
    return finish(im, L, KEY, KEY_P)


DESIGNS = {
    'bougie_noire': bougie_noire, 'mise_du_revenant': mise_du_revenant, 'pacte_de_minuit': pacte_de_minuit,
    'grimoire': grimoire, 'marque_page': marque_page, 'livre_des_ombres': livre_des_ombres,
    'os_de_la_chance': os_de_la_chance, 'eau_benite': eau_benite, 'pierre_tombale': pierre_tombale,
    'boite_a_musique': boite_a_musique, 'malle_oubliee': malle_oubliee, 'miroir_hante': miroir_hante,
}
