"""Cartes Joker au format des cartes classiques du jeu (assets/cards/light/*.png, 190x270).

Les cartes classiques sont une grille de 19x27 cases de 10 px : contour noir aux coins coupés,
liseré blanc en haut et à gauche, ombre gris-bleu à droite et en bas, face gris clair.
Ici la grille est deux fois plus fine (38x54 cases de 5 px) pour avoir de la place pour le dessin,
et chaque élément de la bordure fait 2 cases : la bordure est donc identique au pixel près.
"""
import math
from PIL import Image, ImageDraw

W, H = 38, 54
SCALE = 5
BLACK = (0, 0, 0, 255)
WHITE = (255, 255, 255, 255)
SHADOW = (155, 173, 183, 255)
FACE = (218, 218, 218, 255)
INK = (34, 32, 52, 255)          # le « noir » des enseignes classiques
# Intérieur dessinable : x 4..33, y 4..49
X0, Y0, X1, Y1 = 4, 4, 33, 49


def hexc(h):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def card(face=FACE, light=WHITE, shadow=SHADOW):
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, W - 1, H - 1], fill=BLACK)
    d.rectangle([2, 2, W - 3, H - 3], fill=shadow)
    d.rectangle([2, 2, W - 5, H - 5], fill=light)
    d.rectangle([4, 4, W - 5, H - 5], fill=face)
    # coins coupés (1 case de la carte classique = 2x2 ici)
    for cx, cy in [(0, 0), (W - 2, 0), (0, H - 2), (W - 2, H - 2)]:
        d.rectangle([cx, cy, cx + 1, cy + 1], fill=(0, 0, 0, 0))
    return im


def clip(im, layer):
    """Pose `layer` sur la carte, seulement dans la face (la bordure reste intacte)."""
    m = Image.new('L', (W, H), 0)
    ImageDraw.Draw(m).rectangle([X0, Y0, X1, Y1], fill=255)
    a = layer.split()[3]
    from PIL import ImageChops
    layer.putalpha(ImageChops.multiply(a, m))
    im.alpha_composite(layer)
    return im


def layer():
    return Image.new('RGBA', (W, H), (0, 0, 0, 0))


def sprite(d, x, y, rows, pal):
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch != '.':
                d.point((x + i, y + j), fill=pal[ch])


def outline(img, col=INK):
    import numpy as np
    a = np.array(img)[:, :, 3] > 0
    dd = a.copy()
    dd[1:, :] |= a[:-1, :]
    dd[:-1, :] |= a[1:, :]
    dd[:, 1:] |= a[:, :-1]
    dd[:, :-1] |= a[:, 1:]
    arr = np.array(img)
    arr[dd & ~a] = col
    return Image.fromarray(arr)


def corners(im, rows, pal):
    """Index d'angle sans texte : un petit motif en haut à gauche, retourné en bas à droite."""
    lay = layer()
    sprite(ImageDraw.Draw(lay), 6, 6, rows, pal)
    flipped = lay.rotate(180)
    im.alpha_composite(lay)
    im.alpha_composite(flipped)
    return im


def up(im):
    return im.resize((W * SCALE, H * SCALE), Image.NEAREST)
