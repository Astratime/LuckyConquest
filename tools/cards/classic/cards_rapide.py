"""Cartes des coffres du Rapide de Minuit (10 lieux, 2026-11-20) au style des cartes classiques.
Voir README.md : pas de texte ni de chiffre, contours INK, sujet centré, tout passe par clip().
Un motif d'angle par donjon : roue de locomotive, cloche de service, lune, coffre."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401
from base import ImageDraw  # noqa: E402

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD = hexc('#3fae4a'), hexc('#23793a')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
O, OD = hexc('#f08a2a'), hexc('#b8541a')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ec0ff')
WD, WM, WL = hexc('#6b3e1e'), hexc('#9a5e2c'), hexc('#c8884a')
IR, IRD, IRL = hexc('#9aa4b4'), hexc('#5e6878'), hexc('#dfe6f0')
CO, COL = hexc('#2c2a3c'), hexc('#4a4860')
BX, BXD = hexc('#6a1a2a'), hexc('#4a0e1a')            # vernis bordeaux des wagons
C, CD = hexc('#fbf6ea'), hexc('#d8ccb4')
GR = hexc('#7fd0a0')                                 # billet vert
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


def puff(d, x, y, r=3, col=WH):
    d.ellipse([x - r, y - r, x + r, y + r], fill=col)


def coin(d, x, y, r=2):
    d.ellipse([x - r, y - r, x + r, y + r], fill=Y)
    d.point((x - 1, y - 1), fill=YL)
    d.point((x + r - 1, y + r - 1), fill=YD)


def card_back(d, x, y, w=8, h=11, col=B):
    d.rectangle([x, y, x + w, y + h], fill=C)
    d.rectangle([x + 1, y + 1, x + w - 1, y + h - 1], fill=col)
    for j in range(y + 2, y + h - 1, 2):
        for i in range(x + 2 + (j // 2) % 2, x + w - 1, 2):
            d.point((i, j), fill=BD)


# --- motifs d'angle (5x5), un par donjon ---
WHEEL = ['.rrr.', 'rkykr', 'ryyyr', 'rkykr', '.rrr.']
WHEEL_P = {'r': R, 'k': RD, 'y': Y}
SERVICE = ['..y..', '.yyy.', 'yylyy', 'yyyyy', 'ddddd']
SERVICE_P = {'y': Y, 'l': YL, 'd': YD}
MOON = ['.yy..', 'yy...', 'yy...', 'yy...', '.yy..']
MOON_P = {'y': YL}
SAFE = ['iiiii', 'iddi.', 'idyi.', 'iiiii']
SAFE_P = {'i': IR, 'd': IRD, 'y': Y}


# ================================================================ La Locomotive
def pelle_a_charbon(pal):
    """Pelle à charbon : la pelle en diagonale, chargée de charbon rougeoyant, une carte qui en sort."""
    im, L, d = start(pal)
    d.line([(26, 12), (16, 34)], fill=WM, width=3)
    d.line([(27, 12), (17, 34)], fill=WL)
    d.rectangle([24, 8, 30, 12], fill=WM)                    # la poignée
    poly(d, [(9, 33), (21, 31), (23, 42), (11, 45)], IR)     # la lame
    d.line([(10, 34), (21, 32)], fill=IRL)
    L, d = again(L)
    for x, y in [(13, 37), (16, 36), (19, 38), (14, 40), (18, 41)]:
        d.rectangle([x, y, x + 2, y + 1], fill=CO)
        d.point((x + 1, y), fill=O)
    d.point((16, 39), fill=Y)
    card_back(d, 6, 14, 7, 10)
    return finish(im, L, WHEEL, WHEEL_P)


def sifflet_du_chef_de_gare(pal):
    """Sifflet du chef de gare : un sifflet de laiton sur sa chaînette, deux traits de son, une flèche qui relance."""
    im, L, d = start(pal)
    d.ellipse([10, 24, 22, 36], fill=Y)
    d.rectangle([16, 24, 30, 30], fill=Y)
    d.line([(17, 25), (29, 25)], fill=YL)
    L, d = again(L)
    d.ellipse([13, 27, 19, 33], fill=YD)
    d.rectangle([26, 26, 28, 28], fill=CO)
    for k in range(5):                                       # la chaînette
        d.point((12 - k, 36 + k * 2), fill=YD)
    for k, (x, y) in enumerate([(31, 18), (33, 22)]):        # le son
        d.line([(x - 3, y + 2), (x, y - 1)], fill=INK)
    # une flèche ronde : le tirage relancé
    for a in range(20, 300, 12):
        r = math.radians(a)
        d.point((round(19 + 8 * math.cos(r)), round(14 - 6 * math.sin(r))), fill=G)
    poly(d, [(26, 10), (29, 14), (24, 15)], G)
    return finish(im, L, WHEEL, WHEEL_P)


def chaudiere_ardente(pal):
    """Chaudière ardente (rare) : la porte du foyer grande ouverte, les flammes qui en sortent, le manomètre au rouge."""
    im, L, d = start(pal)
    d.rectangle([7, 20, 31, 46], fill=CO)
    d.rectangle([7, 20, 31, 22], fill=COL)
    for x in (10, 19, 28):
        d.point((x, 24), fill=Y)
        d.point((x, 43), fill=Y)
    d.ellipse([20, 6, 32, 18], fill=Y)                       # le manomètre
    L, d = again(L)
    d.ellipse([22, 8, 30, 16], fill=C)
    d.line([(26, 12), (29, 9)], fill=R)
    d.rectangle([11, 27, 27, 41], fill=INK)                  # le foyer
    poly(d, [(12, 41), (14, 30), (17, 36), (19, 27), (22, 35), (24, 30), (26, 41)], O)
    poly(d, [(15, 41), (17, 35), (19, 31), (21, 36), (23, 41)], Y)
    d.point((19, 38), fill=YL)
    d.rectangle([5, 27, 8, 41], fill=IRD)                    # la porte ouverte
    return finish(im, L, WHEEL, WHEEL_P)


# ================================================================ Le Wagon-restaurant
def plat_du_jour(pal):
    """Plat du jour : une assiette fumante, un coeur de vapeur au-dessus et deux pièces à côté."""
    im, L, d = start(pal)
    d.ellipse([7, 30, 31, 44], fill=WH)
    L, d = again(L)
    d.ellipse([11, 33, 27, 41], fill=CD)
    d.ellipse([13, 33, 25, 39], fill=O)
    d.point((16, 35), fill=Y)
    d.point((21, 36), fill=G)
    for x in (14, 19, 24):                                   # la vapeur
        d.line([(x, 29), (x - 1, 26), (x + 1, 23), (x, 20)], fill=IRL)
    d.polygon([(19, 18), (16, 14), (16, 12), (18, 11), (19, 12), (20, 11), (22, 12), (22, 14)], fill=R)
    coin(d, 30, 25)
    coin(d, 8, 26)
    return finish(im, L, SERVICE, SERVICE_P)


def service_a_la_place(pal):
    """Service à la place : un plateau d'argent porté haut, deux cartes dessus, une pièce."""
    im, L, d = start(pal)
    d.ellipse([6, 22, 32, 30], fill=IR)
    d.line([(19, 30), (19, 44)], fill=IRD, width=2)
    d.ellipse([14, 42, 24, 46], fill=WH)                     # la main gantée
    L, d = again(L)
    d.ellipse([8, 23, 30, 28], fill=IRL)
    card_back(d, 11, 11, 7, 11)
    card_back(d, 19, 10, 7, 11, R)
    coin(d, 29, 20)
    return finish(im, L, SERVICE, SERVICE_P)


def menu_gastronomique(pal):
    """Menu gastronomique (rare) : la cloche d'argent soulevée, un festin doré dessous, des étoiles."""
    im, L, d = start(pal)
    d.rectangle([6, 36, 32, 38], fill=Y)
    poly(d, [(8, 22), (14, 12), (22, 9), (30, 13), (32, 20), (20, 24)], IR)   # la cloche, en biais
    L, d = again(L)
    d.line([(14, 13), (22, 10)], fill=IRL)
    d.ellipse([20, 7, 23, 10], fill=Y)
    d.ellipse([10, 28, 18, 36], fill=O)                      # le festin
    d.ellipse([17, 26, 27, 36], fill=Y)
    d.point((21, 28), fill=YL)
    d.ellipse([25, 31, 30, 36], fill=R)
    for x, y in [(7, 30), (31, 27), (19, 44)]:
        d.line([(x - 1, y), (x + 1, y)], fill=YL)
        d.line([(x, y - 1), (x, y + 1)], fill=YL)
    return finish(im, L, SERVICE, SERVICE_P)


# ================================================================ Le Wagon-lit
def billet_de_seconde(pal):
    """Billet de seconde : un billet vert poinçonné, un petit bouclier et une pièce."""
    im, L, d = start(pal)
    d.rectangle([6, 18, 32, 32], fill=GR)
    L, d = again(L)
    d.rectangle([8, 20, 30, 30], fill=hexc('#a8e8c4'))
    d.line([(20, 20), (20, 30)], fill=GD)
    d.rectangle([10, 23, 17, 24], fill=GD)
    d.rectangle([10, 26, 15, 27], fill=GD)
    d.ellipse([24, 22, 27, 25], fill=pal[0])                 # le trou du poinçon
    poly(d, [(12, 36), (20, 34), (28, 36), (27, 42), (20, 47), (13, 42)], B)  # le bouclier
    d.line([(14, 37), (20, 35)], fill=BL)
    coin(d, 29, 12)
    return finish(im, L, MOON, MOON_P)


def couchette(pal):
    """Couchette : un lit de wagon, l'oreiller, la couverture rouge, et la lune à la fenêtre."""
    im, L, d = start(pal)
    d.rectangle([6, 10, 32, 24], fill=BX)                    # la fenêtre
    d.rectangle([5, 32, 33, 40], fill=WM)                    # le lit
    d.rectangle([5, 40, 7, 46], fill=WD)
    d.rectangle([31, 40, 33, 46], fill=WD)
    L, d = again(L)
    d.rectangle([8, 12, 30, 22], fill=hexc('#1a2a4a'))
    d.ellipse([22, 13, 28, 19], fill=YL)
    d.ellipse([24, 12, 30, 18], fill=hexc('#1a2a4a'))
    d.point((11, 15), fill=WH)
    d.point((16, 19), fill=WH)
    d.rectangle([7, 28, 15, 32], fill=WH)                    # l'oreiller
    d.rectangle([15, 29, 32, 34], fill=R)                    # la couverture
    d.line([(15, 29), (32, 29)], fill=RL)
    return finish(im, L, MOON, MOON_P)


def billet_de_premiere(pal):
    """Billet de première (rare) : un billet doré à liseré, une couronne, deux cartes en éventail."""
    im, L, d = start(pal)
    d.rectangle([6, 26, 32, 40], fill=Y)
    L, d = again(L)
    d.rectangle([8, 28, 30, 38], fill=YL)
    d.rectangle([9, 29, 29, 37], fill=Y)
    poly(d, [(13, 35), (13, 30), (16, 33), (19, 29), (22, 33), (25, 30), (25, 35)], YD)  # la couronne
    d.ellipse([27, 31, 29, 33], fill=pal[0])
    card_back(d, 10, 10, 8, 12, R)
    card_back(d, 19, 9, 8, 12)
    return finish(im, L, MOON, MOON_P)


# ================================================================ Le Fourgon blindé
def coffre_blinde(pal):
    """Coffre blindé : un coffre-fort d'acier riveté, sa roue de serrure, fermé à double tour."""
    im, L, d = start(pal)
    d.rectangle([7, 12, 31, 44], fill=IR)
    L, d = again(L)
    d.rectangle([9, 14, 29, 42], fill=IRD)
    d.rectangle([10, 15, 28, 41], fill=IR)
    for x in (11, 27):
        for y in (16, 40):
            d.point((x, y), fill=IRL)
    d.ellipse([13, 22, 25, 34], fill=IRD)                    # la roue de serrure
    d.ellipse([15, 24, 23, 32], fill=IRL)
    for a in range(0, 360, 60):
        r = math.radians(a)
        d.line([(19, 28), (round(19 + 5 * math.cos(r)), round(28 + 5 * math.sin(r)))], fill=IRD)
    d.ellipse([18, 27, 20, 29], fill=Y)
    d.rectangle([28, 20, 31, 24], fill=CO)                   # les gonds
    d.rectangle([28, 32, 31, 36], fill=CO)
    return finish(im, L, SAFE, SAFE_P)


def aiguillage(pal):
    """Aiguillage : deux rails qui se séparent, le levier rouge basculé, une flèche qui change de voie."""
    im, L, d = start(pal)
    for y in range(26, 48, 4):
        d.rectangle([8, y, 30, y + 1], fill=WM)              # les traverses
    L, d = again(L)
    d.line([(13, 47), (14, 26)], fill=IRL, width=2)
    d.line([(25, 47), (24, 36)], fill=IRL, width=2)
    d.line([(24, 36), (30, 24)], fill=IRL, width=2)          # la voie qui bifurque
    d.line([(24, 36), (22, 26)], fill=IRD, width=2)
    d.line([(8, 20), (14, 9)], fill=IRD, width=2)            # le levier
    d.ellipse([12, 6, 17, 11], fill=R)
    d.rectangle([5, 19, 11, 22], fill=CO)
    poly(d, [(24, 12), (31, 12), (31, 9), (34, 14), (31, 19), (31, 16), (24, 16)], G)
    return finish(im, L, SAFE, SAFE_P)


def soupape(pal):
    """Soupape (rare) : une soupape de laiton qui crache un grand jet de vapeur, un rouleau qui brille."""
    im, L, d = start(pal)
    d.rectangle([14, 30, 24, 46], fill=Y)
    d.rectangle([11, 28, 27, 31], fill=YD)
    d.ellipse([16, 20, 22, 28], fill=Y)
    L, d = again(L)
    d.line([(15, 32), (15, 45)], fill=YL)
    d.rectangle([18, 36, 20, 40], fill=YD)
    for x, y, r in [(19, 15, 4), (13, 11, 3), (25, 10, 3), (19, 7, 3), (9, 16, 2), (29, 16, 2)]:
        puff(d, x, y, r)
    L, d = again(L)
    d.point((18, 13), fill=IRL)
    d.rectangle([28, 34, 33, 44], fill=WH)                   # un rouleau, qui brille
    d.rectangle([29, 37, 32, 41], fill=IRL)
    d.point((30, 38), fill=YL)
    return finish(im, L, SAFE, SAFE_P)


DESIGNS = {
    'pelle_a_charbon': pelle_a_charbon, 'sifflet_du_chef_de_gare': sifflet_du_chef_de_gare,
    'chaudiere_ardente': chaudiere_ardente,
    'plat_du_jour': plat_du_jour, 'service_a_la_place': service_a_la_place, 'menu_gastronomique': menu_gastronomique,
    'billet_de_seconde': billet_de_seconde, 'couchette': couchette, 'billet_de_premiere': billet_de_premiere,
    'coffre_blinde': coffre_blinde, 'aiguillage': aiguillage, 'soupape': soupape,
}
