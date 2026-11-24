"""Cartes de l'archétype Menue monnaie (du 2 au 8 ; Astra, 2026-11-23), au style des cartes classiques (voir base.py et
README.md). Comme les autres cartes d'archétype, seul le rang est écrit, en index dans le coin haut gauche (chiffres
copiés des cartes classiques), suivi de l'emblème de l'archétype (une petite pièce de cuivre) ; aucun autre texte."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401,F403
from base import INK, WHITE, hexc  # noqa: E402
import cards_archetypes  # noqa: E402
from cards_archetypes import finish, ranked, R, RD, RL, Y, YD, YL, G, GD, WD, WDD, WDL  # noqa: E402
from cards_w1 import poly, ob, stack, sparkle, coin_stack  # noqa: E402
from PIL import ImageDraw  # noqa: E402

CU, CUD, CUL = hexc('#d0783c'), hexc('#8a4420'), hexc('#f4b07a')   # cuivre
WOOL, WOOLD = hexc('#e8e0d0'), hexc('#b0a48c')

MONNAIE_I = ['.kkk.', 'kcccl', 'kclck', 'kcccl', '.kkk.']
MP = {'k': INK, 'c': CU, 'l': CUL}

cards_archetypes.RANKS.update({
    'piecette': 2, 'bas_de_laine': 3, 'monnaie_rendue': 4, 'bourse_de_cuivre': 5,
    'pile_de_pieces': 6, 'rouleau_de_pieces': 7, 'escarcelle': 8,
})


def copper_coin(d, x0, y0, s):
    """Pièce de cuivre de face, carré englobant (x0, y0, s), avec un liseré et un reflet."""
    d.ellipse([x0, y0, x0 + s, y0 + s], fill=CUD)
    d.ellipse([x0, y0, x0 + s - 1, y0 + s - 1], fill=CU)
    d.ellipse([x0 + 2, y0 + 2, x0 + s - 3, y0 + s - 3], fill=CUD)
    d.ellipse([x0 + 2, y0 + 2, x0 + s - 4, y0 + s - 4], fill=CU)
    d.point([(x0 + 3, y0 + 3), (x0 + 2, y0 + s // 2)], fill=CUL)


CU_STACK = (CU, CUD, CUL)


def piecette(pal):
    """Une seule petite pièce de cuivre qui brille."""
    Co = ob(lambda d: copper_coin(d, 11, 18, 16))
    L = stack(Co)
    d = ImageDraw.Draw(L)
    sparkle(d, 28, 16)
    sparkle(d, 9, 38)
    return finish(pal, L, MONNAIE_I, MP)


def bas_de_laine(pal):
    """Une chaussette de laine rayée, pleine de pièces qui dépassent du haut."""
    def sock(d):
        poly(d, [(13, 18), (25, 18), (25, 34), (31, 38), (31, 43), (13, 43)], WOOL)
        for y in (22, 27, 32):
            d.rectangle([13, y, 25, y + 1], fill=R)
        d.rectangle([13, 41, 31, 43], fill=WOOLD)
    So = ob(sock)
    Co = ob(lambda d: (copper_coin(d, 13, 12, 8), copper_coin(d, 19, 10, 8)))
    L = stack(Co, So)
    sparkle(ImageDraw.Draw(L), 30, 14)
    return finish(pal, L, MONNAIE_I, MP)


def monnaie_rendue(pal):
    """Deux pièces, et une flèche verte qui revient vers soi."""
    Co = ob(lambda d: (copper_coin(d, 8, 26, 12), copper_coin(d, 18, 32, 12)))

    def arrow(d):
        d.arc([10, 10, 30, 30], 200, 360, fill=G, width=3)
        poly(d, [(7, 18), (15, 18), (11, 24)], G)
    Ar = ob(arrow)
    L = stack(Co, Ar)
    return finish(pal, L, MONNAIE_I, MP)


def bourse_de_cuivre(pal):
    """Une bourse de cuir fermée par un cordon, une pièce de cuivre cousue devant."""
    def purse(d):
        d.ellipse([9, 20, 31, 44], fill=WD)
        poly(d, [(14, 22), (26, 22), (29, 15), (11, 15)], WD)
        d.rectangle([13, 20, 27, 22], fill=WDD)
        d.ellipse([10, 22, 14, 34], fill=WDL)
    Pu = ob(purse)
    Co = ob(lambda d: copper_coin(d, 15, 28, 10))
    L = stack(Pu, Co)
    d = ImageDraw.Draw(L)
    d.line([(18, 13), (16, 9)], fill=Y)
    d.line([(22, 13), (24, 9)], fill=Y)
    return finish(pal, L, MONNAIE_I, MP)


def pile_de_pieces(pal):
    """Trois piles de pièces de cuivre, de plus en plus hautes."""
    def piles(d):
        coin_stack(d, 9, 42, 4, w=3, c=CU_STACK)
        coin_stack(d, 19, 42, 7, w=3, c=CU_STACK)
        coin_stack(d, 29, 42, 10, w=3, c=CU_STACK)
    L = stack(ob(piles))
    sparkle(ImageDraw.Draw(L), 31, 16)
    return finish(pal, L, MONNAIE_I, MP)


def rouleau_de_pieces(pal):
    """Un rouleau de pièces dans son papier rouge, couché, une pièce visible au bout."""
    def roll(d):
        poly(d, [(8, 34), (24, 18), (31, 25), (15, 41)], R)
        poly(d, [(10, 33), (24, 19), (26, 21), (12, 35)], RL)
        for k in (0, 5, 10):
            d.line([(13 + k, 32 - k), (18 + k, 37 - k)], fill=RD)
    Ro = ob(roll)
    Co = ob(lambda d: copper_coin(d, 22, 15, 11))
    L = stack(Ro, Co)
    sparkle(ImageDraw.Draw(L), 9, 22)
    return finish(pal, L, MONNAIE_I, MP)


def escarcelle(pal):
    """Une escarcelle pendue à sa ceinture, ouverte, d'où tombent des pièces."""
    def belt(d):
        d.rectangle([5, 14, 33, 17], fill=WDD)
        d.rectangle([17, 13, 22, 18], fill=Y)
        d.rectangle([19, 14, 20, 17], fill=YD)
    Be = ob(belt)

    def pouch(d):
        poly(d, [(11, 19), (27, 19), (30, 34), (8, 34)], WD)
        poly(d, [(10, 19), (28, 19), (28, 24), (10, 24)], WDL)
    Po = ob(pouch)
    Co = ob(lambda d: (copper_coin(d, 10, 36, 8), copper_coin(d, 20, 39, 8)))
    L = stack(Be, Po, Co)
    return finish(pal, L, MONNAIE_I, MP)


DESIGNS = {f.__name__: ranked(f.__name__, f) for f in (
    piecette, bas_de_laine, monnaie_rendue, bourse_de_cuivre, pile_de_pieces, rouleau_de_pieces, escarcelle,
)}
