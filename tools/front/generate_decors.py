"""
Décors de la vue de face : un par donjon de l'Exploration (la Prairie et la Cale
sont dans generate_front_art.py).

Chaque décor fait 360 x 220 pixels, agrandi sans lissage par le jeu. L'ennemi se
tient au milieu, la table couvre le bas : ce qui compte se voit en haut et sur
les côtés. Les objets sont détourés de sombre, comme le reste du jeu. Les points
lumineux (torches, lave, projecteurs) sont listés dans GLOWS et recopiés dans
FrontScene.Decor, qui les fait vaciller.

Lancer depuis la racine du dépôt : python3 tools/front/generate_decors.py
"""
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generate_front_art import outline, ROOT  # noqa: E402

W, H = 360, 220

# Points lumineux de chaque décor (centre en pixels du décor), recopiés dans FrontScene.Decor.
GLOWS = {}


def base(color):
    im = Image.new('RGBA', (W, H), color + (255,))
    return im, ImageDraw.Draw(im)


def layer():
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)


def gradient(d, top, bottom, y0=0, y1=H):
    for y in range(y0, y1):
        t = (y - y0) / max(1, y1 - y0 - 1)
        d.line([(0, y), (W, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bottom)))


def vignette(im, strength=0.45, floor=0.45):
    a = np.array(im).astype(float)
    yy, xx = np.mgrid[0:H, 0:W]
    dist = np.sqrt(((xx - W / 2) / (W / 2)) ** 2 + ((yy - H * 0.45) / (H * 0.9)) ** 2)
    a[:, :, :3] *= np.clip(1.12 - strength * dist, floor, 1)[:, :, None]
    return Image.fromarray(a.astype(np.uint8)).convert('RGB')


def tint(im, color, k):
    """Mélange l'image avec {color} (eau du casino englouti, chaleur du volcan)."""
    a = np.array(im.convert('RGBA')).astype(float)
    a[:, :, :3] = a[:, :, :3] * (1 - k) + np.array(color) * k
    return Image.fromarray(a.astype(np.uint8))


def stars(d, rng, count, y1, colors=((255, 255, 255), (200, 210, 255))):
    for _ in range(count):
        x, y = int(rng.integers(0, W)), int(rng.integers(0, y1))
        d.point([(x, y)], fill=colors[int(rng.integers(0, len(colors)))])


def lantern(fg, x, y, glass=(255, 214, 120), body=(46, 40, 36)):
    """Petite lanterne suspendue, flamme centrée en (x, y)."""
    d = ImageDraw.Draw(fg)
    d.line([x, y - 12, x, y - 8], fill=(60, 60, 64))
    d.rectangle([x - 5, y - 7, x + 4, y + 7], fill=body)
    d.rectangle([x - 3, y - 5, x + 2, y + 5], fill=glass)
    d.rectangle([x - 2, y - 3, x + 1, y + 3], fill=(255, 246, 210))
    d.rectangle([x - 6, y + 7, x + 5, y + 9], fill=body)


def torch(fg, x, y):
    """Torche murale, flamme centrée en (x, y)."""
    d = ImageDraw.Draw(fg)
    d.polygon([(x - 3, y + 4), (x + 3, y + 4), (x + 1, y + 18), (x - 1, y + 18)], fill=(110, 70, 36))
    d.rectangle([x - 4, y + 3, x + 4, y + 6], fill=(80, 70, 66))
    d.polygon([(x - 4, y + 3), (x + 4, y + 3), (x + 2, y - 4), (x, y - 9), (x - 2, y - 4)], fill=(240, 120, 30))
    d.polygon([(x - 2, y + 3), (x + 2, y + 3), (x, y - 4)], fill=(255, 220, 90))


def bulbs_string(d, x0, y0, x1, y1, sag, step, colors):
    """Guirlande d'ampoules entre deux points."""
    n = max(2, int(math.hypot(x1 - x0, y1 - y0) / step))
    pts = []
    for i in range(n + 1):
        t = i / n
        pts.append((x0 + (x1 - x0) * t, y0 + (y1 - y0) * t + sag * math.sin(math.pi * t)))
    d.line(pts, fill=(40, 30, 30))
    for i, (x, y) in enumerate(pts[1:-1]):
        d.rectangle([x - 1, y + 1, x + 1, y + 3], fill=colors[i % len(colors)])


def planks(d, y0, y1, colors, line, x0=0, x1=W, height=8, seam=60, seed=0):
    for i, y in enumerate(range(y0, y1, height)):
        d.rectangle([x0, y, x1, y + height - 1], fill=colors[i % len(colors)])
        d.line([x0, y + height - 1, x1, y + height - 1], fill=line)
        for x in range(x0 + (i * 37 + seed) % seam, x1, seam):
            d.line([x, y, x, y + height - 2], fill=line)


def bricks(d, x0, y0, x1, y1, colors, mortar, bw=16, bh=8):
    d.rectangle([x0, y0, x1, y1], fill=mortar)
    for j, y in enumerate(range(y0, y1, bh)):
        off = (bw // 2) * (j % 2)
        for x in range(x0 - off, x1, bw):
            d.rectangle([max(x0, x + 1), y + 1, min(x1, x + bw - 1), min(y1, y + bh - 1)],
                        fill=colors[(x // bw + j) % len(colors)])


def rocks(d, rng, x0, y0, x1, y1, colors, count=60, rmax=14):
    for _ in range(count):
        x, y = int(rng.integers(x0, x1)), int(rng.integers(y0, y1))
        r = int(rng.integers(4, rmax))
        d.ellipse([x - r, y - r * 0.7, x + r, y + r * 0.7], fill=colors[int(rng.integers(0, len(colors)))])


def finish(im, fg):
    im.alpha_composite(outline(fg))
    return im


# ---------------------------------------------------------------------------- la Fête foraine

def chapiteau():
    """Sous le chapiteau : toile rayée qui monte vers le mât, gradins, projecteurs, piste."""
    im, d = base((30, 14, 22))
    top = (180, -60)
    for k in range(-12, 13):
        x0, x1 = 180 + k * 36, 180 + (k + 1) * 36
        c = (196, 34, 46) if k % 2 == 0 else (238, 226, 200)
        d.polygon([top, (x0, 150), (x1, 150)], fill=c)
    d.rectangle([0, 112, W, 150], fill=(60, 20, 40))
    fg, f = layer()
    for x0 in (0, 260):  # gradins pleins de silhouettes
        for row in range(5):
            y = 112 + row * 9
            f.rectangle([x0, y, x0 + 100, y + 8], fill=(70, 40, 60) if row % 2 else (84, 50, 72))
            for px in range(x0 + 4 + row * 3, x0 + 100, 9):
                f.ellipse([px, y - 5, px + 5, y], fill=(36, 22, 40))
    f.rectangle([174, 0, 186, 112], fill=(196, 160, 60))  # mât
    f.rectangle([174, 0, 177, 112], fill=(236, 206, 110))
    for x in (60, 300):  # projecteurs
        f.rectangle([x - 7, 10, x + 7, 22], fill=(60, 60, 70))
        f.ellipse([x - 5, 18, x + 5, 26], fill=(255, 250, 210))
    for i in range(0, W, 24):  # fanions
        f.polygon([(i, 60), (i + 12, 60), (i + 6, 70)], fill=[(240, 190, 40), (60, 150, 220), (220, 60, 90)][i // 24 % 3])
    f.line([(0, 60), (W, 60)], fill=(40, 30, 30))
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    for x, side in ((60, 1), (300, -1)):  # faisceaux
        d.polygon([(x - 4, 26), (x + 4, 26), (x + side * 120 + 30, 220), (x + side * 120 - 30, 220)],
                  fill=(255, 240, 190, 40))
    d.ellipse([90, 150, 270, 220], fill=(120, 30, 40))
    d.ellipse([100, 156, 260, 220], fill=(190, 150, 100))
    GLOWS['chapiteau'] = ((60, 22), (300, 22))
    return vignette(im, 0.5)


def train_fantome():
    """Le tunnel du train fantôme : arches de bois, rails, squelettes pendus, yeux verts."""
    im, d = base((22, 14, 34))
    gradient(d, (18, 10, 30), (46, 24, 60), 0, 160)
    for k, (w, c) in enumerate(((360, (40, 24, 50)), (290, (32, 20, 44)), (220, (26, 16, 38)), (160, (20, 12, 30)))):
        x0 = 180 - w // 2
        d.rectangle([x0, 20 + k * 14, x0 + w, 160], fill=c)
    fg, f = layer()
    for k, w in enumerate((350, 284, 214)):  # arches
        x0, y0 = 180 - w // 2, 20 + k * 14
        f.rectangle([x0, y0, x0 + 10 - k * 2, 160], fill=(84, 54, 40))
        f.rectangle([x0 + w - 10 + k * 2, y0, x0 + w, 160], fill=(84, 54, 40))
        f.rectangle([x0, y0, x0 + w, y0 + 8 - k * 2], fill=(98, 64, 46))
    for x in (40, 316):  # squelettes pendus
        f.line([x, 26, x, 44], fill=(150, 140, 120))
        f.ellipse([x - 7, 44, x + 7, 58], fill=(232, 228, 210))
        f.rectangle([x - 4, 50, x - 1, 53], fill=(20, 10, 20))
        f.rectangle([x + 1, 50, x + 4, 53], fill=(20, 10, 20))
        f.rectangle([x - 1, 58, x + 1, 84], fill=(232, 228, 210))
        for y in (62, 67, 72):
            f.line([x - 7, y, x + 7, y], fill=(232, 228, 210))
        f.line([x, 84, x - 6, 100], fill=(232, 228, 210))
        f.line([x, 84, x + 6, 100], fill=(232, 228, 210))
        f.line([x - 7, 62, x - 12, 80], fill=(232, 228, 210))
        f.line([x + 7, 62, x + 12, 80], fill=(232, 228, 210))
    for x, y in ((90, 96), (262, 104), (130, 74)):  # fantôme
        f.ellipse([x - 9, y - 12, x + 9, y + 6], fill=(214, 226, 236))
        f.polygon([(x - 9, y), (x + 9, y), (x + 9, y + 14), (x + 4, y + 9), (x, y + 14), (x - 4, y + 9), (x - 9, y + 14)],
                  fill=(214, 226, 236))
        f.rectangle([x - 5, y - 5, x - 2, y - 1], fill=(30, 20, 40))
        f.rectangle([x + 2, y - 5, x + 5, y - 1], fill=(30, 20, 40))
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    for k in range(10):  # rails en perspective
        y = 160 + k * 6
        d.line([(0, y), (W, y)], fill=(54, 36, 30) if k % 2 else (66, 44, 34))
    d.line([(150, 160), (40, 220)], fill=(150, 150, 160), width=3)
    d.line([(210, 160), (320, 220)], fill=(150, 150, 160), width=3)
    for x, y in ((24, 140), (336, 132), (60, 120)):  # yeux qui brillent dans le noir
        d.rectangle([x - 4, y, x - 2, y + 1], fill=(120, 255, 140))
        d.rectangle([x + 2, y, x + 4, y + 1], fill=(120, 255, 140))
    for x0, y0 in ((0, 0), (W - 40, 0)):  # toiles d'araignée
        for k in range(0, 40, 8):
            d.line([(x0 if x0 == 0 else W, 0), (x0 + k if x0 == 0 else W - k, 40 - k)], fill=(150, 140, 160))
    GLOWS['train_fantome'] = ((24, 140), (336, 132), (60, 120))
    return vignette(im, 0.6, 0.35)


def grande_roue():
    """Nuit sur la foire : la Grande Roue illuminée à gauche, tentes et guirlandes à droite."""
    im, d = base((0, 0, 0))
    gradient(d, (20, 18, 58), (110, 60, 120), 0, 150)
    rng = np.random.default_rng(3)
    stars(d, rng, 90, 110)
    d.ellipse([300, 12, 324, 36], fill=(250, 244, 210))
    fg, f = layer()
    cx, cy, r = 78, 82, 66
    f.polygon([(cx - 6, cy), (cx + 6, cy), (cx + 40, 160), (cx + 30, 160)], fill=(150, 150, 170))
    f.polygon([(cx - 6, cy), (cx + 6, cy), (cx - 30, 160), (cx - 40, 160)], fill=(150, 150, 170))
    f.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(220, 220, 236), width=3)
    f.ellipse([cx - r + 10, cy - r + 10, cx + r - 10, cy + r - 10], outline=(180, 180, 200), width=1)
    for k in range(12):
        a = k * math.pi / 6
        f.line([(cx, cy), (cx + r * math.cos(a), cy + r * math.sin(a))], fill=(200, 200, 220))
        gx, gy = cx + r * math.cos(a), cy + r * math.sin(a)
        f.rectangle([gx - 6, gy + 2, gx + 6, gy + 11], fill=[(220, 60, 80), (60, 160, 230), (250, 200, 50)][k % 3])
        f.line([(gx, gy), (gx, gy + 2)], fill=(40, 30, 30))
    f.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], fill=(250, 210, 60))
    for x0, c in ((230, (196, 40, 50)), (296, (50, 120, 200))):  # tentes
        f.polygon([(x0, 150), (x0 + 30, 100), (x0 + 60, 150)], fill=c)
        f.polygon([(x0 + 30, 100), (x0 + 45, 150), (x0 + 60, 150)], fill=(238, 226, 200))
        f.rectangle([x0 + 22, 126, x0 + 38, 150], fill=(40, 20, 30))
        f.polygon([(x0 + 30, 100), (x0 + 30, 90), (x0 + 40, 94)], fill=(250, 210, 60))
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    for k in range(24):  # ampoules du cercle
        a = k * math.pi / 12
        x, y = cx + r * math.cos(a), cy + r * math.sin(a)
        d.rectangle([x - 1, y - 1, x + 1, y + 1], fill=(255, 240, 160))
    bulbs_string(d, 150, 40, 360, 60, 18, 8, [(255, 220, 90), (255, 90, 110), (110, 200, 255)])
    d.rectangle([0, 150, W, H], fill=(40, 30, 50))
    GLOWS['grande_roue'] = ((cx, cy), (cx, cy - r), (cx - r, cy), (cx + r, cy))
    return vignette(im, 0.4)


def stands():
    """Les stands de la foire au crépuscule : pêche aux canards, tir aux cibles, chamboule-tout."""
    im, d = base((0, 0, 0))
    gradient(d, (248, 150, 90), (120, 60, 110), 0, 150)
    d.ellipse([250, 70, 300, 120], fill=(255, 200, 120))
    fg, f = layer()

    def booth(x0, x1, c):
        f.rectangle([x0, 70, x1, 150], fill=(110, 70, 44))
        f.rectangle([x0 + 4, 80, x1 - 4, 132], fill=(54, 30, 30))
        for k, x in enumerate(range(x0 - 6, x1 + 6, 12)):
            f.rectangle([x, 50, x + 11, 70], fill=c if k % 2 == 0 else (240, 230, 210))
            f.pieslice([x, 64, x + 11, 76], 0, 180, fill=c if k % 2 == 0 else (240, 230, 210))
        f.rectangle([x0 - 2, 132, x1 + 2, 150], fill=(150, 96, 56))
        f.rectangle([x0, 70, x0 + 4, 150], fill=(90, 56, 34))
        f.rectangle([x1 - 4, 70, x1, 150], fill=(90, 56, 34))

    booth(0, 110, (200, 40, 50))
    for x in range(14, 100, 16):  # canards
        f.ellipse([x, 118, x + 11, 128], fill=(250, 210, 40))
        f.ellipse([x + 6, 112, x + 13, 120], fill=(250, 210, 40))
        f.rectangle([x + 13, 115, x + 16, 117], fill=(240, 120, 30))
    f.rectangle([4, 126, 106, 132], fill=(60, 140, 210))
    for x, y in ((20, 90), (48, 96), (76, 88)):  # peluches
        f.ellipse([x, y, x + 14, y + 14], fill=(240, 170, 190))
        f.ellipse([x + 1, y - 4, x + 6, y + 1], fill=(240, 170, 190))
        f.ellipse([x + 8, y - 4, x + 13, y + 1], fill=(240, 170, 190))
    booth(250, 360, (40, 110, 200))
    for x, y in ((270, 92), (300, 104), (330, 90)):  # cibles
        for r, c in ((9, (230, 40, 50)), (6, (250, 250, 250)), (3, (230, 40, 50))):
            f.ellipse([x - r, y - r, x + r, y + r], fill=c)
    for row, n in enumerate((4, 3, 2)):  # chamboule-tout
        for k in range(n):
            x = 280 + k * 12 + row * 6
            y = 122 - row * 10
            f.rectangle([x, y, x + 9, y + 9], fill=(200, 200, 210) if (k + row) % 2 else (230, 80, 60))
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    bulbs_string(d, 0, 30, 180, 20, 14, 8, [(255, 220, 90), (255, 90, 110), (110, 200, 255)])
    bulbs_string(d, 180, 20, 360, 34, 14, 8, [(255, 220, 90), (255, 90, 110), (110, 200, 255)])
    d.rectangle([0, 150, W, H], fill=(96, 70, 52))
    GLOWS['stands'] = ((60, 60), (300, 60))
    return vignette(im, 0.4)


# ---------------------------------------------------------------------------- le Port

def taverne():
    """La taverne du port : planches, comptoir et tonneaux, étagère de chopes, cheminée."""
    im, d = base((40, 26, 18))
    planks(d, 0, 168, [(96, 62, 36), (86, 55, 32), (102, 68, 40)], (56, 34, 20))
    fg, f = layer()
    for x in (0, 96, 264, 352):
        f.rectangle([x, 0, x + 8, 168], fill=(62, 38, 22))
    f.rectangle([0, 0, W, 10], fill=(56, 34, 20))
    # étagère de chopes à gauche
    for y in (40, 70):
        f.rectangle([8, y, 92, y + 4], fill=(120, 80, 46))
        for x in range(12, 88, 14):
            f.rectangle([x, y - 12, x + 9, y - 1], fill=(170, 170, 180))
            f.rectangle([x, y - 13, x + 9, y - 10], fill=(250, 246, 230))
            f.rectangle([x + 9, y - 9, x + 12, y - 4], fill=(150, 150, 160))
    # tonneaux et comptoir
    for x, y in ((6, 112), (34, 116), (20, 92)):
        f.ellipse([x, y, x + 26, y + 36], fill=(120, 74, 40))
        f.rectangle([x + 2, y + 8, x + 24, y + 10], fill=(70, 70, 76))
        f.rectangle([x + 2, y + 26, x + 24, y + 28], fill=(70, 70, 76))
        f.ellipse([x + 10, y + 15, x + 15, y + 20], fill=(80, 46, 24))
    f.rectangle([60, 120, 130, 168], fill=(110, 68, 38))
    f.rectangle([56, 116, 134, 122], fill=(140, 92, 52))
    # cheminée à droite
    f.rectangle([280, 70, 352, 168], fill=(120, 110, 104))
    f.rectangle([292, 110, 340, 168], fill=(30, 18, 14))
    f.rectangle([276, 104, 356, 110], fill=(100, 70, 46))
    f.polygon([(298, 168), (334, 168), (326, 140), (316, 126), (306, 142)], fill=(240, 120, 30))
    f.polygon([(306, 168), (326, 168), (316, 142)], fill=(255, 220, 90))
    f.rectangle([300, 40, 332, 70], fill=(130, 120, 112))
    for y in range(76, 168, 10):
        f.line([(280, y), (292, y)], fill=(96, 88, 84))
        f.line([(340, y), (352, y)], fill=(96, 88, 84))
    lantern(fg, 140, 30)
    lantern(fg, 226, 30)
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 168, W, H], fill=(52, 34, 22))
    GLOWS['taverne'] = ((140, 30), (226, 30), (316, 150))
    return vignette(im)


def phare():
    """La nuit sur les récifs : le phare à gauche et son faisceau, la lune, la mer agitée."""
    rng = np.random.default_rng(5)
    fg, f = layer()
    f.polygon([(46, 150), (94, 150), (84, 48), (56, 48)], fill=(236, 232, 224))
    for y0 in (64, 96, 128):
        f.polygon([(56 - (y0 - 48) * 0.1, y0), (84 + (y0 - 48) * 0.1, y0), (84 + (y0 - 32) * 0.1, y0 + 14),
                   (56 - (y0 - 32) * 0.1, y0 + 14)], fill=(200, 40, 46))
    f.rectangle([52, 30, 88, 48], fill=(60, 60, 70))
    f.rectangle([56, 32, 84, 46], fill=(255, 236, 150))
    f.polygon([(48, 30), (92, 30), (70, 14)], fill=(170, 30, 40))
    f.rectangle([44, 46, 96, 50], fill=(60, 60, 70))
    rocks(f, rng, 0, 140, 140, 165, [(60, 60, 70), (80, 80, 92), (48, 48, 58)], 26, 16)
    rocks(f, rng, 280, 140, 360, 165, [(60, 60, 70), (80, 80, 92), (48, 48, 58)], 14, 14)
    # ciel, lune, et le faisceau translucide
    sky, s = base((0, 0, 0))
    gradient(s, (10, 16, 40), (40, 60, 100), 0, 130)
    stars(s, np.random.default_rng(5), 70, 100)
    s.ellipse([292, 18, 318, 44], fill=(240, 240, 220))
    s.ellipse([300, 16, 322, 40], fill=(10, 16, 40))
    beam = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(beam).polygon([(70, 38), (360, 0), (360, 76)], fill=(255, 246, 200, 70))
    sky.alpha_composite(beam)
    im = finish(sky, fg)
    d = ImageDraw.Draw(im)
    for y in range(130, H):  # mer
        k = (y - 130) / 90
        d.line([(0, y), (W, y)], fill=(int(20 + 20 * k), int(50 + 30 * k), int(90 + 30 * k)))
    for y in range(136, H, 8):
        for x in range((y * 7) % 30, W, 30):
            d.line([(x, y), (x + 8, y - 2), (x + 14, y)], fill=(140, 180, 220))
    rocks(d, rng, 0, 150, 120, 175, [(56, 56, 66), (74, 74, 86)], 18, 18)
    GLOWS['phare'] = ((70, 39),)
    return vignette(im, 0.35)


def galion():
    """Sur le pont du galion noir : mâts, voiles noires à tête de mort, cordages, lanternes."""
    im, d = base((0, 0, 0))
    gradient(d, (24, 20, 46), (70, 50, 80), 0, 140)
    rng = np.random.default_rng(9)
    stars(d, rng, 60, 90)
    fg, f = layer()
    for mx, top in ((60, 0), (300, 6)):
        f.rectangle([mx - 4, top, mx + 4, 150], fill=(70, 44, 26))
        for y0, w in ((16, 54), (66, 66)):
            f.polygon([(mx - w, y0), (mx + w, y0), (mx + w - 6, y0 + 44), (mx - w + 6, y0 + 44)], fill=(34, 30, 36))
            f.line([(mx - w, y0), (mx + w, y0)], fill=(90, 60, 36), width=3)
        sx, sy = mx, 88
        f.ellipse([sx - 9, sy - 9, sx + 9, sy + 7], fill=(236, 232, 220))
        f.rectangle([sx - 5, sy - 3, sx - 2, sy], fill=(34, 30, 36))
        f.rectangle([sx + 2, sy - 3, sx + 5, sy], fill=(34, 30, 36))
        f.line([(sx - 12, sy + 10), (sx + 12, sy + 18)], fill=(236, 232, 220), width=3)
        f.line([(sx + 12, sy + 10), (sx - 12, sy + 18)], fill=(236, 232, 220), width=3)
    for k in range(6):  # cordages
        f.line([(60, 10 + k * 4), (0 + k * 22, 150)], fill=(120, 96, 64))
        f.line([(300, 14 + k * 4), (360 - k * 22, 150)], fill=(120, 96, 64))
    f.polygon([(300, 6), (300, 0), (330, 2), (300, 12)], fill=(20, 18, 22))
    f.rectangle([0, 140, W, 152], fill=(80, 50, 30))  # bastingage
    for x in range(0, W, 16):
        f.rectangle([x, 128, x + 4, 140], fill=(96, 62, 36))
    f.rectangle([0, 124, W, 128], fill=(110, 72, 42))
    lantern(fg, 120, 40)
    lantern(fg, 240, 40)
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    planks(d, 152, H, [(70, 44, 26), (62, 38, 22)], (40, 24, 14), height=7)
    GLOWS['galion'] = ((120, 40), (240, 40))
    return vignette(im, 0.45)


# ---------------------------------------------------------------------------- les Mines

ROCK = [(76, 62, 54), (66, 54, 48), (88, 72, 62), (58, 48, 42)]


def cave(d, rng, color=(46, 38, 34)):
    d.rectangle([0, 0, W, H], fill=color)
    rocks(d, rng, -10, -10, W + 10, 170, ROCK, 220, 18)


def support(f, x0, x1, top=20, bottom=170):
    f.rectangle([x0, top, x0 + 10, bottom], fill=(120, 80, 42))
    f.rectangle([x1 - 10, top, x1, bottom], fill=(120, 80, 42))
    f.rectangle([x0 - 4, top - 10, x1 + 4, top], fill=(140, 94, 50))
    f.line([(x0 + 2, top + 2), (x0 + 2, bottom)], fill=(150, 104, 58))


def filon():
    """La galerie du filon : roche veinée d'or, étais de bois, wagonnet plein, lampes."""
    rng = np.random.default_rng(21)
    im, d = base((0, 0, 0))
    cave(d, rng)
    for _ in range(26):  # veines d'or
        x, y = int(rng.integers(0, W)), int(rng.integers(0, 160))
        pts = [(x + k * 4, y + int(4 * math.sin(k + x))) for k in range(int(rng.integers(3, 8)))]
        d.line(pts, fill=(230, 180, 40), width=2)
        d.point([(pts[0][0] + 1, pts[0][1] - 1)], fill=(255, 240, 150))
    fg, f = layer()
    support(f, 10, 100)
    support(f, 260, 350)
    f.rectangle([20, 128, 82, 160], fill=(90, 90, 100))  # wagonnet
    f.rectangle([24, 132, 78, 156], fill=(110, 110, 120))
    for x in (30, 70):
        f.ellipse([x - 6, 156, x + 6, 168], fill=(50, 50, 56))
    for k in range(9):
        x = 26 + (k % 5) * 11 + (k // 5) * 5
        y = 122 - (k // 5) * 6
        f.ellipse([x, y, x + 10, y + 8], fill=(240, 196, 50))
        f.point([(x + 2, y + 2)], fill=(255, 246, 180))
    for x, y in ((290, 120), (316, 132)):  # pioche et pelle posées
        f.line([(x, y), (x + 20, y + 36)], fill=(130, 90, 50), width=3)
    f.polygon([(286, 116), (300, 112), (310, 118), (296, 122)], fill=(170, 176, 186))
    lantern(fg, 55, 34)
    lantern(fg, 305, 34)
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 170, W, H], fill=(54, 44, 38))
    d.line([(0, 182), (W, 182)], fill=(120, 110, 110), width=2)
    d.line([(0, 196), (W, 196)], fill=(120, 110, 110), width=2)
    GLOWS['filon'] = ((55, 34), (305, 34))
    return vignette(im, 0.5, 0.35)


def puits():
    """Au bord du puits sans fond : trou noir au centre, échafaudages, poulie, foreuse, lampes."""
    rng = np.random.default_rng(22)
    im, d = base((0, 0, 0))
    cave(d, rng, (40, 34, 30))
    for k in range(10):  # le trou qui descend
        w = 170 - k * 14
        c = int(30 - k * 3)
        d.ellipse([180 - w, 30 + k * 6, 180 + w, 150 - k * 3], fill=(max(0, c), max(0, c - 4), max(0, c - 4)))
    fg, f = layer()
    for x in (20, 300):  # échafaudages
        for y in range(10, 170, 30):
            f.rectangle([x, y, x + 44, y + 3], fill=(130, 88, 46))
        f.rectangle([x, 0, x + 4, 170], fill=(110, 74, 40))
        f.rectangle([x + 40, 0, x + 44, 170], fill=(110, 74, 40))
        for y in range(10, 170, 30):
            f.line([(x + 4, y + 3), (x + 40, y + 30)], fill=(96, 64, 34))
    f.rectangle([150, 0, 210, 6], fill=(110, 74, 40))  # poulie et corde
    f.ellipse([172, 4, 188, 20], fill=(120, 120, 130))
    f.ellipse([177, 9, 183, 15], fill=(60, 60, 70))
    f.line([(180, 20), (180, 70)], fill=(190, 160, 110), width=2)
    f.rectangle([174, 70, 186, 80], fill=(110, 74, 40))
    f.rectangle([90, 120, 130, 150], fill=(200, 160, 40))  # foreuse
    f.rectangle([94, 124, 126, 140], fill=(160, 120, 30))
    f.polygon([(130, 128), (150, 132), (130, 140)], fill=(170, 176, 186))
    for x in range(132, 150, 4):
        f.line([(x, 129 + (x - 132) // 5), (x, 139 - (x - 132) // 5)], fill=(110, 116, 126))
    lantern(fg, 66, 40)
    lantern(fg, 290, 40)
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 168, W, H], fill=(52, 44, 38))
    GLOWS['puits'] = ((66, 40), (290, 40))
    return vignette(im, 0.5, 0.35)


def forge():
    """La forge sous la montagne : mur de pierre, grand fourneau rougeoyant, enclumes, armes."""
    rng = np.random.default_rng(23)
    im, d = base((0, 0, 0))
    bricks(d, 0, 0, W, 170, [(92, 80, 74), (80, 70, 66), (104, 90, 82)], (50, 40, 36), 20, 10)
    fg, f = layer()
    f.rectangle([10, 30, 120, 170], fill=(80, 70, 66))  # fourneau
    f.pieslice([30, 70, 100, 150], 180, 360, fill=(30, 14, 10))
    f.rectangle([30, 110, 100, 170], fill=(30, 14, 10))
    f.polygon([(36, 170), (94, 170), (86, 132), (76, 112), (66, 126), (56, 108), (46, 130)], fill=(240, 100, 30))
    f.polygon([(48, 170), (84, 170), (74, 134), (64, 124), (56, 138)], fill=(255, 210, 80))
    f.polygon([(0, 30), (130, 30), (100, 0), (30, 0)], fill=(70, 62, 58))
    for x in range(250, 350, 22):  # râtelier d'armes
        f.line([(x, 40), (x, 112)], fill=(190, 196, 206), width=3)
        f.rectangle([x - 6, 104, x + 6, 108], fill=(200, 160, 40))
        f.rectangle([x - 1, 108, x + 1, 120], fill=(110, 70, 40))
    f.rectangle([240, 120, 356, 126], fill=(110, 70, 40))
    for x, y in ((150, 136), (290, 140)):  # enclumes
        f.polygon([(x - 22, y), (x + 26, y), (x + 14, y + 8), (x - 12, y + 8)], fill=(70, 72, 80))
        f.rectangle([x - 8, y + 8, x + 8, y + 22], fill=(60, 62, 70))
        f.rectangle([x - 14, y + 22, x + 14, y + 30], fill=(54, 56, 62))
        f.line([(x - 22, y), (x + 26, y)], fill=(130, 134, 146))
    f.rectangle([158, 120, 162, 134], fill=(110, 70, 40))  # marteau posé
    f.rectangle([152, 114, 168, 122], fill=(90, 92, 100))
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 170, W, H], fill=(50, 40, 36))
    for _ in range(30):  # étincelles
        x, y = int(rng.integers(30, 200)), int(rng.integers(40, 160))
        d.point([(x, y)], fill=(255, 200, 80))
    GLOWS['forge'] = ((65, 140), (40, 100), (90, 100))
    return vignette(im, 0.5, 0.4)


def gouffre():
    """Le gouffre : un abîme, un pont de corde, des cristaux d'or qui luisent, des stalactites."""
    rng = np.random.default_rng(24)
    im, d = base((0, 0, 0))
    cave(d, rng, (34, 28, 26))
    for k in range(12):  # l'abîme
        w = 120 - k * 8
        c = 22 - k * 2
        d.rectangle([180 - w, 60 + k * 5, 180 + w, H], fill=(max(0, c), max(0, c - 2), max(0, c + 4)))
    fg, f = layer()
    for x in range(0, W, 26):  # stalactites
        h = int(rng.integers(14, 40))
        f.polygon([(x, 0), (x + 14, 0), (x + 7, h)], fill=(70, 58, 52))
    for k in range(-6, 7):  # pont de corde
        x = 180 + k * 18
        y = 70 + 10 * math.cos(k / 6 * math.pi / 2) ** 0 * (1 - (k / 6) ** 2) * 1.5
        f.rectangle([x - 7, y, x + 7, y + 4], fill=(130, 90, 50))
    f.line([(60, 66), (180, 82), (300, 66)], fill=(190, 160, 110))
    f.line([(60, 58), (180, 74), (300, 58)], fill=(190, 160, 110))
    crystals = ((36, 120), (78, 140), (300, 118), (330, 140), (20, 60), (340, 70))
    for x, y in crystals:
        for dx, h in ((-6, 18), (0, 26), (6, 16)):
            f.polygon([(x + dx - 4, y), (x + dx + 4, y), (x + dx, y - h)], fill=(250, 200, 50))
            f.line([(x + dx - 1, y - 2), (x + dx, y - h + 4)], fill=(255, 244, 180))
    im = finish(im, fg)
    GLOWS['gouffre'] = crystals[:4]
    return vignette(im, 0.5, 0.35)


# ---------------------------------------------------------------------------- le Casino Englouti

WATER = (20, 90, 120)


def water(im, rng, k=0.35):
    im = tint(im, WATER, k)
    d = ImageDraw.Draw(im)
    for x in (40, 130, 220, 310):  # rais de lumière
        ray = Image.new('RGBA', (W, H), (0, 0, 0, 0))
        ImageDraw.Draw(ray).polygon([(x, 0), (x + 24, 0), (x - 30, H), (x - 60, H)], fill=(200, 240, 255, 26))
        im.alpha_composite(ray)
    d = ImageDraw.Draw(im)
    for _ in range(40):  # bulles
        x, y, r = int(rng.integers(0, W)), int(rng.integers(0, 170)), int(rng.integers(1, 4))
        d.ellipse([x - r, y - r, x + r, y + r], outline=(200, 240, 255))
    return im


def seaweed(f, rng, x, h):
    pts = [(x + 4 * math.sin(k / 3), 170 - k * 3) for k in range(h // 3)]
    f.line(pts, fill=(40, 140, 80), width=4)
    f.line(pts, fill=(70, 180, 100), width=1)


def bar():
    """Le bar englouti : comptoir, bouteilles qui flottent, algues, rais de lumière, bulles."""
    rng = np.random.default_rng(31)
    im, d = base((0, 0, 0))
    gradient(d, (60, 30, 40), (40, 20, 30), 0, 170)
    for x in range(0, W, 24):  # papier peint
        for y in range(0, 170, 24):
            d.polygon([(x + 12, y + 4), (x + 18, y + 12), (x + 12, y + 20), (x + 6, y + 12)], fill=(74, 40, 50))
    fg, f = layer()
    for y in (40, 76):  # étagères de bouteilles
        f.rectangle([10, y, 110, y + 4], fill=(120, 80, 46))
        f.rectangle([250, y, 350, y + 4], fill=(120, 80, 46))
        for x in list(range(16, 106, 12)) + list(range(256, 346, 12)):
            c = [(60, 140, 70), (150, 50, 50), (190, 150, 60), (80, 100, 170)][(x // 12 + y) % 4]
            f.rectangle([x, y - 16, x + 7, y - 1], fill=c)
            f.rectangle([x + 2, y - 22, x + 5, y - 16], fill=c)
    f.rectangle([0, 130, W, 170], fill=(110, 62, 40))  # comptoir
    f.rectangle([0, 124, W, 132], fill=(150, 96, 56))
    for x, y, a in ((140, 30, 30), (214, 50, -20)):  # bouteilles qui flottent
        f.polygon([(x, y), (x + 8, y - 4), (x + 18, y + 14), (x + 10, y + 18)], fill=(60, 140, 70))
    for x in (20, 60, 300, 340):
        seaweed(f, rng, x, 90)
    im = finish(im, fg)
    im = water(im, rng)
    GLOWS['bar'] = ((60, 20), (300, 20))
    return vignette(im, 0.45)


def machines():
    """La salle des machines engloutie : rangées de machines à sous, coraux, voyants qui clignotent."""
    rng = np.random.default_rng(32)
    im, d = base((24, 20, 36))
    gradient(d, (30, 24, 44), (20, 16, 30), 0, 170)
    fg, f = layer()
    lights = []
    for row, (y0, w, gap) in enumerate(((40, 36, 6), (70, 44, 8))):
        for x in list(range(0 if row else 4, 120, w + gap)) + list(range(240, W, w + gap)):
            c = [(150, 30, 50), (40, 80, 160), (110, 40, 150), (30, 120, 90)][(x // w + row) % 4]
            f.rectangle([x, y0, x + w, y0 + 90], fill=c)
            f.rectangle([x + 4, y0 + 6, x + w - 4, y0 + 14], fill=(250, 210, 60))
            f.rectangle([x + 4, y0 + 20, x + w - 4, y0 + 40], fill=(240, 236, 220))
            for k in range(3):
                f.line([(x + 4 + k * (w - 8) / 3, y0 + 20), (x + 4 + k * (w - 8) / 3, y0 + 40)], fill=(60, 50, 60))
            f.rectangle([x + 6, y0 + 50, x + w - 6, y0 + 56], fill=(20, 14, 20))
            f.line([(x + w, y0 + 24), (x + w + 4, y0 + 18)], fill=(170, 170, 180), width=2)
            if row == 1:
                lights.append((x + w // 2, y0 + 10))
    for x, c in ((140, (240, 110, 110)), (210, (250, 160, 90)), (90, (230, 120, 170))):  # coraux
        for k in range(5):
            f.line([(x, 170), (x + (k - 2) * 7, 140 - k % 2 * 10)], fill=c, width=3)
    im = finish(im, fg)
    im = water(im, rng, 0.3)
    GLOWS['machines'] = tuple(lights[:4])
    return vignette(im, 0.45)


def coffres():
    """La salle des coffres : porte de coffre-fort, coffres ouverts débordant d'or, requins qui tournent."""
    rng = np.random.default_rng(33)
    im, d = base((0, 0, 0))
    bricks(d, 0, 0, W, 170, [(70, 76, 86), (60, 66, 76), (80, 86, 96)], (40, 44, 52), 24, 12)
    fg, f = layer()
    f.ellipse([10, 30, 110, 130], fill=(150, 156, 166))  # porte du coffre-fort
    f.ellipse([20, 40, 100, 120], fill=(120, 126, 136))
    f.ellipse([48, 68, 72, 92], fill=(200, 160, 40))
    for k in range(6):
        a = k * math.pi / 3
        f.line([(60, 80), (60 + 22 * math.cos(a), 80 + 22 * math.sin(a))], fill=(200, 160, 40), width=3)
    for x in (250, 300):  # coffres ouverts
        f.rectangle([x, 130, x + 44, 160], fill=(130, 70, 34))
        f.rectangle([x - 2, 108, x + 46, 116], fill=(100, 52, 24))
        f.ellipse([x + 2, 120, x + 42, 136], fill=(250, 200, 50))
        f.rectangle([x + 18, 136, x + 26, 146], fill=(200, 160, 40))
        f.rectangle([x, 130, x + 44, 134], fill=(200, 160, 40))
    for x, y, s in ((150, 40, 1), (240, 70, -1)):  # requins
        body = [(x - 40 * s, y), (x - 10 * s, y - 10), (x + 30 * s, y - 4), (x + 40 * s, y), (x + 30 * s, y + 6),
                (x - 10 * s, y + 8)]
        f.polygon(body, fill=(110, 130, 150))
        f.polygon([(x - 2 * s, y - 9), (x + 8 * s, y - 22), (x + 12 * s, y - 6)], fill=(110, 130, 150))
        f.polygon([(x - 40 * s, y), (x - 54 * s, y - 12), (x - 50 * s, y), (x - 54 * s, y + 10)], fill=(110, 130, 150))
        f.line([(x - 30 * s, y + 3), (x + 32 * s, y + 3)], fill=(220, 230, 236))
        f.rectangle([x + 24 * s - 1, y - 3, x + 24 * s + 1, y - 1], fill=(20, 20, 30))
    im = finish(im, fg)
    im = water(im, rng, 0.3)
    GLOWS['coffres'] = ((272, 126), (322, 126))
    return vignette(im, 0.45)


def vip():
    """La salle VIP : velours sombre, lustre, rideaux, et des tentacules qui sortent de l'ombre."""
    rng = np.random.default_rng(34)
    im, d = base((0, 0, 0))
    gradient(d, (30, 16, 40), (16, 8, 24), 0, 170)
    fg, f = layer()
    for x0, s in ((0, 1), (W, -1)):  # rideaux
        for k in range(6):
            x = x0 + s * k * 10
            f.rectangle([min(x, x + s * 10), 0, max(x, x + s * 10), 170], fill=(120, 20, 40) if k % 2 else (150, 30, 50))
    f.line([(180, 0), (180, 20)], fill=(200, 160, 40))  # lustre
    f.polygon([(150, 20), (210, 20), (196, 40), (164, 40)], fill=(220, 180, 60))
    for x in range(154, 210, 8):
        f.rectangle([x, 40, x + 2, 48], fill=(200, 230, 255))
        f.rectangle([x, 14, x + 2, 20], fill=(255, 240, 180))
    for x0, s in ((64, 1), (296, -1)):  # tentacules
        pts = [(x0 + s * (k * 2 + 14 * math.sin(k / 6)), 170 - k * 4) for k in range(30)]
        for i, (x, y) in enumerate(pts):
            r = 9 - i * 0.25
            f.ellipse([x - r, y - r, x + r, y + r], fill=(120, 50, 130))
        for i, (x, y) in enumerate(pts[2::4]):
            f.ellipse([x - 2 + s * 3, y - 2, x + 2 + s * 3, y + 2], fill=(220, 160, 210))
    im = finish(im, fg)
    im = water(im, rng, 0.22)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 170, W, H], fill=(24, 40, 40))
    GLOWS['vip'] = ((180, 30),)
    return vignette(im, 0.55, 0.35)


# ---------------------------------------------------------------------------- le Désert de Braise

SAND = [(232, 190, 120), (214, 168, 100), (246, 210, 146)]


def dunes(d, y0, colors):
    for k, (amp, freq, c) in enumerate(((12, 46, colors[2]), (14, 34, colors[0]), (10, 24, colors[1]))):
        base_y = y0 + k * 16
        for x in range(W):
            y = base_y - int(amp * (0.5 + 0.5 * math.sin(x / freq + k * 2)))
            d.line([(x, y), (x, H)], fill=c)


def caravane():
    """Les dunes sous le soleil : une caravane de chameaux chargés d'or passe sur la crête."""
    im, d = base((0, 0, 0))
    gradient(d, (250, 170, 90), (255, 222, 160), 0, 120)
    d.ellipse([280, 14, 330, 64], fill=(255, 244, 200))
    dunes(d, 100, SAND)
    fg, f = layer()
    for k, x in enumerate((14, 74, 270, 330)):  # chameaux
        y = 104 - (k % 2) * 4
        f.ellipse([x - 18, y - 12, x + 14, y + 4], fill=(170, 116, 60))
        f.ellipse([x - 12, y - 20, x - 2, y - 8], fill=(170, 116, 60))
        f.ellipse([x, y - 18, x + 10, y - 8], fill=(170, 116, 60))
        f.line([(x + 12, y - 4), (x + 22, y - 22)], fill=(170, 116, 60), width=5)
        f.ellipse([x + 18, y - 28, x + 30, y - 20], fill=(170, 116, 60))
        for lx in (x - 14, x - 8, x + 4, x + 10):
            f.line([(lx, y + 2), (lx, y + 22)], fill=(150, 100, 52), width=3)
        f.rectangle([x - 14, y - 26, x + 8, y - 18], fill=(150, 40, 50))  # sacs d'or
        f.ellipse([x - 12, y - 32, x - 2, y - 22], fill=(240, 196, 60))
        f.ellipse([x - 2, y - 32, x + 8, y - 22], fill=(240, 196, 60))
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    for y in range(124, H, 12):  # traces de vent
        for x in range((y * 5) % 50, W, 50):
            d.line([(x, y), (x + 14, y)], fill=(250, 220, 170))
    GLOWS['caravane'] = ()
    return vignette(im, 0.3)


def tombeau():
    """Le couloir du tombeau : murs de grès gravés, piliers, torches, sarcophages debout."""
    im, d = base((0, 0, 0))
    bricks(d, 0, 0, W, 170, [(190, 150, 96), (176, 138, 86), (204, 164, 108)], (130, 96, 56), 28, 14)
    fg, f = layer()
    for x in (0, 110, 230, 340):  # piliers
        f.rectangle([x, 0, x + 20, 170], fill=(170, 130, 80))
        f.rectangle([x + 14, 0, x + 20, 170], fill=(140, 104, 62))
        f.rectangle([x - 4, 0, x + 24, 10], fill=(150, 112, 66))
    glyphs = ["X.X", ".X.", "XXX"]
    for gx in range(30, 100, 14):  # hiéroglyphes
        for gy in (30, 60, 90):
            pattern = [glyphs, ["XXX", "X.X", "XXX"], [".X.", "XXX", ".X."]][(gx + gy) % 3]
            for j, row in enumerate(pattern):
                for i, ch in enumerate(row):
                    if ch == 'X':
                        f.rectangle([gx + i * 2, gy + j * 2, gx + i * 2 + 1, gy + j * 2 + 1], fill=(110, 70, 40))
                        f.rectangle([W - gx - 6 + i * 2, gy + j * 2, W - gx - 5 + i * 2, gy + j * 2 + 1],
                                    fill=(110, 70, 40))
    for x in (40, 290):  # sarcophages
        f.rounded_rectangle([x, 96, x + 30, 168], 10, fill=(220, 170, 50))
        f.ellipse([x + 6, 100, x + 24, 120], fill=(240, 210, 160))
        f.rectangle([x + 4, 100, x + 26, 108], fill=(40, 70, 140))
        f.rectangle([x + 10, 112, x + 13, 114], fill=(20, 20, 30))
        f.rectangle([x + 17, 112, x + 20, 114], fill=(20, 20, 30))
        for y in range(126, 166, 8):
            f.line([(x + 4, y), (x + 26, y)], fill=(40, 70, 140), width=2)
    for x in (122, 242):
        torch(fg, x, 50)
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 170, W, H], fill=(150, 112, 66))
    GLOWS['tombeau'] = ((122, 46), (242, 46))
    return vignette(im, 0.55, 0.3)


def oasis():
    """L'oasis : bassin bleu, palmiers, ciel clair et une brume de mirage sur les dunes."""
    im, d = base((0, 0, 0))
    gradient(d, (110, 190, 240), (230, 236, 220), 0, 120)
    d.ellipse([40, 16, 70, 46], fill=(255, 250, 220))
    dunes(d, 104, SAND)
    fg, f = layer()

    def palm(x, y, lean):
        pts = [(x + lean * (k / 20) ** 2 * 30, y - k * 4) for k in range(21)]
        for i, (px, py) in enumerate(pts):
            f.rectangle([px - 4, py - 3, px + 4, py + 1], fill=(150, 100, 52) if i % 2 else (126, 82, 40))
        tx, ty = pts[-1]
        for a in (-150, -110, -70, -30, 10, 200, 230):
            r = math.radians(a)
            leaf = [(tx, ty), (tx + 30 * math.cos(r), ty + 30 * math.sin(r) + 8),
                    (tx + 36 * math.cos(r), ty + 36 * math.sin(r) + 16)]
            f.line(leaf, fill=(40, 130, 60), width=6)
            f.line(leaf, fill=(70, 170, 80), width=2)
        f.ellipse([tx - 5, ty, tx + 1, ty + 6], fill=(120, 70, 30))
        f.ellipse([tx, ty + 1, tx + 6, ty + 7], fill=(120, 70, 30))

    palm(40, 150, 1)
    palm(76, 156, -0.6)
    palm(310, 152, -1)
    palm(282, 158, 0.5)
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    d.ellipse([90, 150, 270, 210], fill=(60, 150, 200))  # bassin
    d.ellipse([100, 156, 260, 204], fill=(90, 180, 220))
    for y in range(160, 204, 7):
        d.line([(130 + (y % 3) * 8, y), (150 + (y % 3) * 8, y)], fill=(200, 236, 250))
    for x in range(0, W, 4):  # mirage
        d.point([(x, 100 + int(2 * math.sin(x / 5)))], fill=(250, 250, 240))
    GLOWS['oasis'] = ()
    return vignette(im, 0.3)


def volcan():
    """Le coeur du volcan : ciel rouge de cendre, cratère, coulées de lave, l'or de la comète."""
    rng = np.random.default_rng(41)
    im, d = base((0, 0, 0))
    gradient(d, (40, 10, 14), (150, 40, 20), 0, 130)
    for _ in range(80):  # cendres
        x, y = int(rng.integers(0, W)), int(rng.integers(0, 130))
        d.point([(x, y)], fill=(120, 100, 96))
    fg, f = layer()
    f.polygon([(-60, 150), (40, 26), (100, 26), (200, 150)], fill=(54, 34, 34))  # le volcan, à gauche
    f.polygon([(40, 26), (100, 26), (90, 34), (50, 34)], fill=(255, 120, 30))
    f.polygon([(60, 34), (80, 34), (100, 90), (86, 150), (68, 150), (76, 90)], fill=(240, 90, 20))
    f.line([(70, 36), (82, 90), (76, 150)], fill=(255, 210, 80), width=2)
    f.polygon([(220, 150), (300, 70), (340, 80), (380, 150)], fill=(44, 28, 28))  # rochers sombres à droite
    for k in range(5):  # panache de fumée
        r = 10 + k * 5
        f.ellipse([70 - r + k * 6, 10 - k * 12 - r / 2, 70 + r + k * 6, 10 - k * 12 + r / 2], fill=(70, 56, 56))
    rocks(f, rng, 0, 120, W, 165, [(50, 32, 30), (64, 40, 36), (40, 26, 24)], 40, 16)
    for x, y in ((300, 130), (322, 136), (310, 122)):  # or de la comète
        f.ellipse([x - 10, y - 6, x + 10, y + 6], fill=(250, 200, 50))
        f.point([(x - 4, y - 3)], fill=(255, 246, 190))
    f.polygon([(296, 116), (306, 104), (316, 116)], fill=(160, 220, 255))
    im = finish(im, fg)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 160, W, H], fill=(40, 24, 22))
    for pts in (((0, 176), (60, 168), (120, 182), (180, 172)), ((200, 190), (260, 178), (320, 188), (360, 178))):
        d.line(pts, fill=(240, 90, 20), width=7)
        d.line(pts, fill=(255, 210, 80), width=2)
    for _ in range(16):  # braises
        x, y = int(rng.integers(0, W)), int(rng.integers(40, 160))
        d.rectangle([x, y, x + 1, y + 1], fill=(255, 160, 60))
    GLOWS['volcan'] = ((70, 30), (80, 110), (60, 172), (300, 184))
    return vignette(im, 0.45, 0.4)


DECORS = {
    'chapiteau': chapiteau, 'train_fantome': train_fantome, 'grande_roue': grande_roue, 'stands': stands,
    'taverne': taverne, 'phare': phare, 'galion': galion,
    'filon': filon, 'puits': puits, 'forge': forge, 'gouffre': gouffre,
    'bar': bar, 'machines': machines, 'coffres': coffres, 'vip': vip,
    'caravane': caravane, 'tombeau': tombeau, 'oasis': oasis, 'volcan': volcan,
}


def main():
    for name, draw in DECORS.items():
        draw().convert('RGB').save(os.path.join(ROOT, 'decor_' + name + '.png'))
    for name in DECORS:
        print(name, GLOWS.get(name, ()))


if __name__ == '__main__':
    main()
