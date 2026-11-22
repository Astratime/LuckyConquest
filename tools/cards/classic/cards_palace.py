"""Cartes du Palace des Neiges (10 lieux, 2026-11-21) au style des cartes classiques : les 12 cartes des coffres
et les 4 cartes des rois. Voir README.md : pas de texte ni de chiffre, contours INK, sujet centré, tout passe
par clip(). Un motif d'angle par donjon pour les coffres (sonnette, patin, cabine, coupe) ; la petite couronne
des cartes des rois pour les rois (comme cards_rois.py)."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401
from base import ImageDraw  # noqa: E402

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ec0ff')
IC, ICD, ICL = hexc('#8ccaf0'), hexc('#3a7ab8'), hexc('#e0f4ff')   # la glace
SN, SND = hexc('#f6faff'), hexc('#c4d4e8')                          # la neige
IR, IRD, IRL = hexc('#9aa4b4'), hexc('#5e6878'), hexc('#dfe6f0')
WD, WM = hexc('#6b3e1e'), hexc('#9a5e2c')
CO = hexc('#2c2a3c')
CH, CHD = hexc('#f6dc8a'), hexc('#d0a850')                          # le champagne
GR, GRD = hexc('#2a6a4a'), hexc('#1a4a32')                          # les sapins
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


def flake(d, x, y, r=3, col=ICL):
    """Un petit flocon à six branches."""
    for a in range(0, 180, 60):
        t = math.radians(a + 90)
        dx, dy = round(r * math.cos(t)), round(r * math.sin(t))
        d.line([(x - dx, y - dy), (x + dx, y + dy)], fill=col)
    d.point((x, y), fill=WH)


def key(d, x, y, col=Y, dark=YD, light=YL):
    """Une clé d'hôtel debout : l'anneau en haut, la tige, le panneton en bas à droite."""
    d.ellipse([x - 4, y, x + 4, y + 8], fill=col)
    d.ellipse([x - 2, y + 2, x + 2, y + 6], fill=(0, 0, 0, 0))
    d.rectangle([x - 1, y + 8, x + 1, y + 24], fill=col)
    d.rectangle([x + 1, y + 18, x + 5, y + 19], fill=col)
    d.rectangle([x + 1, y + 22, x + 4, y + 24], fill=col)
    d.line([(x - 1, y + 9), (x - 1, y + 23)], fill=light)
    d.point((x + 3, y + 7), fill=dark)


def glass(d, x, y, fill=CH):
    """Une coupe de champagne (évasée), sa jambe et son pied."""
    poly(d, [(x - 6, y), (x + 6, y), (x + 1, y + 6), (x - 1, y + 6)], ICL)
    poly(d, [(x - 5, y + 1), (x + 5, y + 1), (x + 1, y + 5), (x - 1, y + 5)], fill)
    d.rectangle([x, y + 6, x, y + 12], fill=ICL)
    d.rectangle([x - 3, y + 12, x + 3, y + 13], fill=ICL)


def snowball(d, x, y, r):
    d.ellipse([x - r, y - r, x + r, y + r], fill=SN)
    d.ellipse([x - r + 2, y, x + r, y + r], fill=SND)
    d.point((x - r // 2, y - r // 2), fill=WH)


# --- motifs d'angle (5x5), un par donjon ---
BELL = ['..y..', '.yyy.', '.yly.', 'yyyyy', 'ddddd']
BELL_P = {'y': Y, 'l': YL, 'd': YD}
SKATE = ['.b...', '.b...', '.bbbb', '.bbbb', 'iiiii']
SKATE_P = {'b': B, 'i': IRD}
CABIN = ['kkkkk', '..k..', '.rrr.', '.rir.', '.rrr.']
CABIN_P = {'k': CO, 'r': R, 'i': IC}
COUPE = ['iiiii', '.ccc.', '..i..', '..i..', '.iii.']
COUPE_P = {'i': ICD, 'c': CHD}
CROWN = ['y.y.y', 'yyyyy', 'yyyyy']
CROWN_P = {'y': YD}


# ================================================================ Le Hall
def passe_partout(pal):
    """Passe-partout : la clé d'or qui ouvre tout, devant un rouleau retenu par un cadenas ouvert."""
    im, L, d = start(pal)
    d.rectangle([20, 14, 32, 36], fill=WH)                   # un rouleau de machine
    d.rectangle([22, 18, 30, 32], fill=IRL)
    L, d = again(L)
    d.rectangle([23, 22, 29, 28], fill=R)                    # le symbole retenu
    d.point((25, 24), fill=RL)
    d.arc([22, 8, 30, 18], 180, 360, fill=IRD, width=2)      # le cadenas ouvert
    key(d, 12, 16)
    L, d = again(L)
    return finish(im, L, BELL, BELL_P)


def sonnette_de_reception(pal):
    """Sonnette de réception : la sonnette dorée sur le comptoir, deux cartes qui arrivent, une pièce."""
    im, L, d = start(pal)
    d.rectangle([5, 38, 33, 46], fill=WM)                    # le comptoir
    d.rectangle([5, 38, 33, 39], fill=WD)
    d.pieslice([10, 24, 28, 42], 180, 360, fill=Y)            # la sonnette
    d.rectangle([9, 33, 29, 35], fill=YD)
    L, d = again(L)
    d.rectangle([18, 21, 20, 24], fill=YD)
    d.line([(13, 31), (17, 26)], fill=YL)
    for x, y in [(6, 18), (30, 14)]:                          # le « ding »
        d.line([(x, y), (x + 2, y - 3)], fill=INK)
    d.rectangle([22, 6, 29, 17], fill=WH)                    # deux cartes qui arrivent
    d.rectangle([23, 7, 28, 16], fill=B)
    d.rectangle([12, 9, 19, 20], fill=WH)
    d.rectangle([13, 10, 18, 19], fill=R)
    coin(d, 8, 26)
    return finish(im, L, BELL, BELL_P)


def cle_d_or(pal):
    """Clé d'or (rare) : une grande clé dorée qui brille, et des pièces qui tombent du trousseau."""
    im, L, d = start(pal)
    key(d, 19, 8)
    L, d = again(L)
    for x, y in [(10, 12), (29, 16), (8, 30)]:
        d.line([(x - 2, y), (x + 2, y)], fill=YL)
        d.line([(x, y - 2), (x, y + 2)], fill=YL)
        d.point((x, y), fill=WH)
    for x, y in [(10, 40), (16, 44), (27, 42), (31, 36)]:
        coin(d, x, y)
    return finish(im, L, BELL, BELL_P)


# ================================================================ La Patinoire
def lames_de_patin(pal):
    """Lames de patin : un patin blanc à lame d'acier, une rayure de glace et une flèche qui relance."""
    im, L, d = start(pal)
    d.rectangle([10, 16, 22, 30], fill=WH)                   # la tige du patin
    poly(d, [(10, 30), (30, 30), (30, 34), (10, 34)], WH)
    d.rectangle([8, 36, 32, 37], fill=IR)                    # la lame
    L, d = again(L)
    for y in range(18, 29, 3):
        d.line([(13, y), (19, y)], fill=IRD)                 # les lacets
    d.line([(8, 37), (32, 37)], fill=IRL)
    d.line([(6, 42), (32, 40)], fill=ICL)                    # la rayure sur la glace
    for a in range(20, 300, 14):                             # une flèche ronde : le tirage relancé
        r = math.radians(a)
        d.point((round(28 + 4 * math.cos(r)), round(12 - 4 * math.sin(r))), fill=B)
    poly(d, [(31, 8), (34, 12), (29, 12)], B)
    return finish(im, L, SKATE, SKATE_P)


def pirouette(pal):
    """Pirouette : une spirale de glace qui tourne, un bouclier bleu au centre."""
    im, L, d = start(pal)
    for k in range(60):
        t = k / 60 * 4 * math.pi
        r = 3 + k * 0.22
        d.point((round(19 + r * math.cos(t)), round(26 + r * math.sin(t))), fill=ICD)
        d.point((round(19 + r * math.cos(t)) + 1, round(26 + r * math.sin(t))), fill=IC)
    poly(d, [(13, 22), (19, 20), (25, 22), (24, 28), (19, 33), (14, 28)], B)
    L, d = again(L)
    d.line([(15, 23), (19, 21)], fill=BL)
    flake(d, 9, 12)
    flake(d, 30, 40)
    return finish(im, L, SKATE, SKATE_P)


def couronne_de_givre(pal):
    """Couronne de givre (rare) : une couronne de stalactites de glace, un grand flocon au centre."""
    im, L, d = start(pal)
    d.rectangle([7, 30, 31, 36], fill=IC)
    for k, x in enumerate(range(8, 31, 4)):
        top = 12 if k == 3 else (18 if k % 2 else 22)
        poly(d, [(x - 2, 30), (x + 2, 30), (x, top)], ICL if k % 2 else IC)
    L, d = again(L)
    d.line([(7, 31), (31, 31)], fill=ICL)
    for x in (11, 19, 27):
        d.ellipse([x - 1, 32, x + 1, 34], fill=B)
    flake(d, 19, 42, 4, IC)
    return finish(im, L, SKATE, SKATE_P)


# ================================================================ Le Téléphérique
def forfait_de_ski(pal):
    """Forfait de ski : le badge du forfait sur sa pince, deux cartes, et un petit sommet enneigé."""
    im, L, d = start(pal)
    poly(d, [(6, 30), (16, 16), (26, 30)], IRD)              # la montagne
    poly(d, [(12, 21), (16, 16), (20, 21), (18, 23), (16, 21), (14, 23)], SN)
    d.rectangle([14, 30, 32, 44], fill=R)                    # le badge
    L, d = again(L)
    d.rectangle([16, 32, 30, 42], fill=RL)
    d.rectangle([18, 34, 22, 38], fill=WH)
    d.line([(24, 35), (28, 35)], fill=RD)
    d.line([(24, 38), (27, 38)], fill=RD)
    d.rectangle([21, 26, 24, 30], fill=IRD)                  # la pince
    d.rectangle([22, 8, 29, 19], fill=WH)
    d.rectangle([23, 9, 28, 18], fill=B)
    d.rectangle([28, 10, 35, 21], fill=WH)
    d.rectangle([29, 11, 34, 20], fill=R)
    return finish(im, L, CABIN, CABIN_P)


def boule_de_neige(pal):
    """Boule de neige : trois boules qui grossissent en dévalant la pente."""
    im, L, d = start(pal)
    poly(d, [(4, 18), (34, 46), (4, 46)], IRL)               # la pente
    L, d = again(L)
    snowball(d, 9, 15, 2)
    snowball(d, 15, 23, 4)
    snowball(d, 24, 34, 7)
    L, d = again(L)
    for x, y in [(6, 10), (30, 12), (31, 22)]:
        flake(d, x, y, 2)
    return finish(im, L, CABIN, CABIN_P)


def avalanche(pal):
    """Avalanche (rare) : une vague de neige qui déferle du sommet et emporte des sapins."""
    im, L, d = start(pal)
    poly(d, [(4, 26), (14, 8), (24, 26)], IRD)               # le sommet
    poly(d, [(10, 15), (14, 8), (18, 15)], SN)
    L, d = again(L)
    for x, y, r in [(8, 30, 5), (16, 28, 6), (26, 32, 7), (12, 38, 7), (24, 42, 6)]:
        d.ellipse([x - r, y - r, x + r, y + r], fill=SN)
    L, d = again(L)
    for x, y in [(29, 22), (6, 44)]:                         # les sapins emportés
        poly(d, [(x - 3, y + 2), (x + 3, y + 2), (x, y - 5)], GR)
    for x, y, r in [(14, 30, 2), (22, 38, 2), (28, 30, 1)]:
        d.ellipse([x - r, y - r, x + r, y + r], fill=SND)
    return finish(im, L, CABIN, CABIN_P)


# ================================================================ La Suite royale
def seau_a_glace(pal):
    """Seau à glace : un seau d'argent plein de glaçons, une bouteille qui dépasse, un petit coeur."""
    im, L, d = start(pal)
    d.rectangle([22, 10, 26, 28], fill=GRD)                  # la bouteille
    d.rectangle([23, 6, 25, 10], fill=Y)
    poly(d, [(8, 24), (30, 24), (27, 44), (11, 44)], IR)     # le seau
    L, d = again(L)
    d.line([(10, 26), (12, 42)], fill=IRL)
    for x, y in [(11, 22), (15, 21), (19, 22), (27, 22)]:    # les glaçons
        d.rectangle([x, y, x + 3, y + 3], fill=ICL)
    d.rectangle([7, 24, 31, 25], fill=IRD)
    d.polygon([(10, 15), (7, 12), (7, 10), (9, 9), (10, 10), (11, 9), (13, 10), (13, 12)], fill=R)
    return finish(im, L, COUPE, COUPE_P)


def coupe_de_champagne(pal):
    """Coupe de champagne : une coupe qui mousse, ses bulles, un coeur et une pièce."""
    im, L, d = start(pal)
    glass(d, 19, 22)
    L, d = again(L)
    for x, y in [(17, 18), (20, 15), (22, 19), (18, 12)]:
        d.point((x, y), fill=CH)
    d.polygon([(11, 42), (8, 39), (8, 37), (10, 36), (11, 37), (12, 36), (14, 37), (14, 39)], fill=R)
    coin(d, 28, 40)
    return finish(im, L, COUPE, COUPE_P)


def bal_des_neiges(pal):
    """Bal des neiges (rare) : deux coupes qui trinquent sous un lustre de glace, des pièces et des flocons."""
    im, L, d = start(pal)
    d.rectangle([14, 10, 24, 12], fill=Y)                    # le lustre
    for x in range(14, 25, 2):
        d.line([(x, 12), (x, 16)], fill=ICL)
    glass(d, 13, 24)
    glass(d, 25, 24)
    L, d = again(L)
    d.line([(17, 21), (21, 21)], fill=YL)                    # le tintement
    d.point((19, 19), fill=WH)
    for x, y in [(8, 44), (30, 44)]:
        coin(d, x, y)
    flake(d, 7, 16)
    flake(d, 31, 16)
    return finish(im, L, COUPE, COUPE_P)


# ================================================================ Les cartes des rois
def roi_trousseau(pal):
    """Le Trousseau du concierge : trois clés d'or sur leur anneau, et une pièce."""
    im, L, d = start(pal)
    d.ellipse([12, 8, 26, 18], fill=YD)
    d.ellipse([14, 10, 24, 16], fill=(0, 0, 0, 0))
    key(d, 12, 16)
    key(d, 19, 18)
    key(d, 26, 16)
    L, d = again(L)
    coin(d, 30, 44)
    return finish(im, L, CROWN, CROWN_P)


def roi_givre_eternel(pal):
    """Le Givre éternel de l'Impératrice : un grand flocon de glace serti dans un bouclier."""
    im, L, d = start(pal)
    poly(d, [(7, 12), (19, 8), (31, 12), (29, 34), (19, 46), (9, 34)], IC)
    L, d = again(L)
    poly(d, [(10, 14), (19, 11), (28, 14), (26, 33), (19, 42), (12, 33)], ICD)
    flake(d, 19, 26, 7, ICL)
    flake(d, 19, 26, 4, WH)
    return finish(im, L, CROWN, CROWN_P)


def roi_tempete(pal):
    """La Tempête du Yéti : une énorme patte blanche aux griffes de glace, la neige qui tourbillonne."""
    im, L, d = start(pal)
    d.ellipse([9, 22, 29, 42], fill=SN)                      # la patte
    for x in (11, 17, 23, 28):
        d.ellipse([x - 3, 16, x + 3, 24], fill=SN)
    L, d = again(L)
    d.ellipse([13, 28, 25, 38], fill=SND)
    for x in (11, 17, 23, 28):                               # les griffes de glace
        poly(d, [(x - 1, 16), (x + 1, 16), (x, 11)], IC)
    for x, y in [(6, 10), (32, 30), (8, 44), (31, 8)]:
        flake(d, x, y, 2)
    return finish(im, L, CROWN, CROWN_P)


def roi_grand_cru(pal):
    """Le Grand cru du Prince : une bouteille dorée dans la glace, une coupe pleine, un coeur."""
    im, L, d = start(pal)
    d.rectangle([9, 14, 17, 40], fill=GRD)                   # la bouteille
    d.rectangle([11, 8, 15, 14], fill=GRD)
    d.rectangle([11, 6, 15, 9], fill=Y)
    d.rectangle([7, 36, 19, 44], fill=ICL)                   # la glace
    L, d = again(L)
    d.rectangle([10, 22, 16, 30], fill=Y)                    # l'étiquette
    d.line([(11, 16), (11, 34)], fill=GR)
    glass(d, 26, 24)
    L, d = again(L)
    d.polygon([(26, 18), (23, 15), (23, 13), (25, 12), (26, 13), (27, 12), (29, 13), (29, 15)], fill=R)
    return finish(im, L, CROWN, CROWN_P)


DESIGNS = {
    'passe_partout': passe_partout, 'sonnette_de_reception': sonnette_de_reception, 'cle_d_or': cle_d_or,
    'lames_de_patin': lames_de_patin, 'pirouette': pirouette, 'couronne_de_givre': couronne_de_givre,
    'forfait_de_ski': forfait_de_ski, 'boule_de_neige': boule_de_neige, 'avalanche': avalanche,
    'seau_a_glace': seau_a_glace, 'coupe_de_champagne': coupe_de_champagne, 'bal_des_neiges': bal_des_neiges,
    'roi_trousseau': roi_trousseau, 'roi_givre_eternel': roi_givre_eternel, 'roi_tempete': roi_tempete,
    'roi_grand_cru': roi_grand_cru,
}
