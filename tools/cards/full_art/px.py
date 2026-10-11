"""Mini toolkit de pixel art des cartes « pleine illustration » : grille 80x112, agrandie x6 en 480x672.
Pas de texte sur les illustrations : le jeu existe en plusieurs langues.

Lancement, depuis la racine du dépôt (Python 3, Pillow, numpy) :
    PYTHONPATH=tools/cards/full_art python3 tools/cards/full_art/joker.py
"""
import math
import numpy as np
from PIL import Image, ImageDraw

W, H = 80, 112
SCALE = 6
BAYER = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]) / 16.0


def hexc(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def layer():
    return Image.new('RGBA', (W, H), (0, 0, 0, 0))


def outline(img, col, diag=False):
    """Ajoute un contour d'1 px (4-voisins) autour des pixels opaques."""
    a = np.array(img)[:, :, 3] > 0
    d = a.copy()
    d[1:, :] |= a[:-1, :]
    d[:-1, :] |= a[1:, :]
    d[:, 1:] |= a[:, :-1]
    d[:, :-1] |= a[:, 1:]
    if diag:
        d[1:, 1:] |= a[:-1, :-1]
        d[:-1, :-1] |= a[1:, 1:]
        d[1:, :-1] |= a[:-1, 1:]
        d[:-1, 1:] |= a[1:, :-1]
    ring = d & ~a
    arr = np.array(img)
    arr[ring] = hexc(col) if isinstance(col, str) else col
    return Image.fromarray(arr)


def ramp(img, mask, value, cols, dither=0.45):
    """Remplit `mask` avec une rampe de couleurs selon `value` (0..1), bords tramés (Bayer)."""
    arr = np.array(img)
    n = len(cols)
    yy, xx = np.mgrid[0:H, 0:W]
    v = np.clip(value, 0, 0.9999) * n
    idx = np.floor(v + (BAYER[yy % 4, xx % 4] - 0.5) * dither).astype(int)
    idx = np.clip(idx, 0, n - 1)
    for i, c in enumerate(cols):
        m = mask & (idx == i)
        arr[m] = hexc(c)
    return Image.fromarray(arr)


def grid():
    yy, xx = np.mgrid[0:H, 0:W]
    return xx + 0.5, yy + 0.5


def sphere(img, cx, cy, rx, ry, cols, light=(-0.45, -0.55), mask=None, dither=0.45):
    """Ellipse ombrée : cols du plus clair au plus sombre, lumière en haut à gauche."""
    x, y = grid()
    nx, ny = (x - cx) / rx, (y - cy) / ry
    inside = nx ** 2 + ny ** 2 <= 1
    if mask is not None:
        inside &= mask
    d = np.sqrt((nx - light[0]) ** 2 + (ny - light[1]) ** 2) / 1.75
    return ramp(img, inside, d, cols, dither)


def round_corners(img, r=4):
    arr = np.array(img)
    for cy, cx in [(0, 0), (0, W - 1), (H - 1, 0), (H - 1, W - 1)]:
        for dy in range(r):
            for dx in range(r):
                yy = dy if cy == 0 else H - 1 - dy
                xx = dx if cx == 0 else W - 1 - dx
                ddx, ddy = r - dx - 0.5, r - dy - 0.5
                if ddx * ddx + ddy * ddy > r * r:
                    arr[yy, xx, 3] = 0
    return Image.fromarray(arr)


def frame(img, edge, light, r=4):
    """Bordure façon Balatro : liseré clair fin + contour sombre, coins arrondis."""
    img = round_corners(img, r)
    a = np.array(img)[:, :, 3] > 0
    arr = np.array(img)
    # contour extérieur = pixels opaques touchant un transparent ou le bord
    pad = np.pad(a, 1, constant_values=False)
    border = a & ~(pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:])
    arr[border] = hexc(edge)
    inner = a & ~border
    pad2 = np.pad(inner, 1, constant_values=False)
    ring2 = inner & ~(pad2[:-2, 1:-1] & pad2[2:, 1:-1] & pad2[1:-1, :-2] & pad2[1:-1, 2:])
    arr[ring2] = hexc(light)
    return Image.fromarray(arr)


def up(img, s=SCALE):
    return img.resize((img.width * s, img.height * s), Image.NEAREST)


def sparkle(d, x, y, col, big=False):
    c = hexc(col)
    d.point((x, y), fill=c)
    for k in ([1, 2] if big else [1]):
        for dx, dy in [(k, 0), (-k, 0), (0, k), (0, -k)]:
            d.point((x + dx, y + dy), fill=c)
