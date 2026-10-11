import math
import numpy as np
from PIL import Image, ImageDraw
from px import *

INK = '#140a1c'


def build():
    img = layer()
    x, y = grid()
    yy, xx = np.mgrid[0:H, 0:W]
    full = np.ones((H, W), bool)
    # fond arlequin : losanges violets
    u = (x / 10 + y / 14)
    v = (x / 10 - y / 14)
    harl = ((np.floor(u) + np.floor(v)) % 2) == 0
    dist = np.sqrt((x - 40) ** 2 + ((y - 44) * 0.75) ** 2) / 55
    img = ramp(img, full & harl, dist, ['#7b3fb3', '#5e2c8f', '#43206b', '#2a1446'], dither=0.3)
    img = ramp(img, full & ~harl, dist, ['#64309a', '#4a2275', '#341858', '#200e36'], dither=0.3)

    # chapeau à trois pointes
    hat = layer()
    hd = ImageDraw.Draw(hat)
    # pointe gauche (violette), centre (verte), droite (rouge)
    hd.polygon([(22, 35), (31, 33), (20, 18), (8, 12), (12, 20)], fill=hexc('#a855e0'))
    hd.polygon([(30, 34), (50, 34), (46, 18), (40, 6), (34, 18)], fill=hexc('#3fae4a'))
    hd.polygon([(49, 33), (58, 35), (68, 20), (72, 12), (60, 18)], fill=hexc('#d8333f'))
    # ombres des pointes
    hd.polygon([(22, 35), (31, 33), (21, 22)], fill=hexc('#7a32b0'))
    hd.polygon([(40, 34), (50, 34), (46, 18), (40, 6)], fill=hexc('#2a7f34'))
    hd.polygon([(49, 33), (58, 35), (62, 24)], fill=hexc('#9e1f2b'))
    # reflets
    hd.line([(12, 15), (20, 21)], fill=hexc('#d29cf5'))
    hd.line([(39, 9), (35, 20)], fill=hexc('#8ee58f'))
    hd.line([(69, 15), (62, 21)], fill=hexc('#ff8a8f'))
    # bandeau en damier
    for i in range(0, 34, 3):
        c = '#f4ead8' if (i // 3) % 2 == 0 else '#20182a'
        hd.rectangle([23 + i, 33, 25 + i, 36], fill=hexc(c))
    hat = outline(hat, INK)
    # grelots
    for (bx, by) in [(8, 11), (40, 5), (72, 11)]:
        hat = sphere(hat, bx, by, 3.2, 3.2, ['#fff1a8', '#f2c14e', '#c8892a', '#7a4a16'])
    hat = outline(hat, INK)

    # visage
    face = layer()
    face = sphere(face, 40, 50, 15, 16, ['#fff8ee', '#f4e4d0', '#dcc0a4', '#b8957a'])
    fd = ImageDraw.Draw(face)
    face = outline(face, INK)
    fd = ImageDraw.Draw(face)
    # losanges peints sous les yeux
    for ex, col in [(33, '#a855e0'), (47, '#d8333f')]:
        fd.polygon([(ex, 47), (ex + 2, 51), (ex, 55), (ex - 2, 51)], fill=hexc(col))
    # yeux plissés de rire
    for ex in (33, 47):
        fd.line([(ex - 3, 45), (ex - 1, 43), (ex + 1, 43), (ex + 3, 45)], fill=hexc(INK))
    # sourcils malicieux
    fd.line([(29, 40), (35, 39)], fill=hexc(INK))
    fd.line([(45, 39), (51, 40)], fill=hexc(INK))
    # nez rouge
    face = sphere(face, 40, 51, 2.6, 2.4, ['#ff9a9a', '#e0303c', '#8e1420'])
    fd = ImageDraw.Draw(face)
    # grand sourire
    fd.chord([29, 50, 51, 62], 0, 180, fill=hexc('#8e1420'))
    fd.chord([31, 53, 49, 60], 0, 180, fill=hexc('#fff8ee'))
    fd.line([(31, 56), (49, 56)], fill=hexc('#b8957a'))
    fd.arc([29, 50, 51, 62], 0, 180, fill=hexc(INK))
    fd.line([(28, 55), (30, 56)], fill=hexc(INK))
    fd.line([(52, 55), (50, 56)], fill=hexc(INK))
    # joues
    for cx_ in (30, 50):
        fd.point((cx_, 52), fill=hexc('#f08a8a'))
        fd.point((cx_ + 1, 52), fill=hexc('#f08a8a'))

    # collerette en pointes
    ruff = layer()
    rd = ImageDraw.Draw(ruff)
    cols = ['#a855e0', '#3fae4a', '#d8333f', '#f2c14e']
    pts = 8
    for i in range(pts):
        t0 = i / pts
        x0 = 14 + i * 6.5
        rd.polygon([(x0, 64), (x0 + 6.5, 64), (x0 + 3.2, 76 + (2 if i % 2 else 0))],
                   fill=hexc(cols[i % 4]))
        rd.line([(x0 + 1, 65), (x0 + 3.2, 72)], fill=hexc('#ffffff', 120))
    rd.rectangle([16, 62, 64, 65], fill=hexc('#f4ead8'))
    ruff = outline(ruff, INK)
    for i in range(pts):
        x0 = 14 + i * 6.5 + 3.2
        ruff = sphere(ruff, x0, 77 + (2 if i % 2 else 0), 1.8, 1.8, ['#fff1a8', '#f2c14e', '#a8691e'])

    # buste en losanges
    body = layer()
    bd = ImageDraw.Draw(body)
    bd.polygon([(4, 112), (18, 70), (62, 70), (76, 112)], fill=hexc('#2b1b3d'))
    bm = np.array(body)[:, :, 3] > 0
    bu = np.floor(x / 6 + y / 8) + np.floor(x / 6 - y / 8)
    arr = np.array(body)
    arr[bm & (bu % 2 == 0)] = hexc('#3a2552')
    arr[bm & (bu % 4 == 1)] = hexc('#f2c14e')
    body = outline(Image.fromarray(arr), INK)

    # symboles jonglés (le Joker renforce un symbole)
    sym = layer()
    sd = ImageDraw.Draw(sym)
    # cerise à gauche
    sd.line([(10, 52), (13, 46), (16, 50)], fill=hexc('#3fae4a'))
    sym = sphere(sym, 10, 54, 2.6, 2.6, ['#ff9a9a', '#e0303c', '#8e1420'])
    sym = sphere(sym, 16, 52, 2.6, 2.6, ['#ff9a9a', '#e0303c', '#8e1420'])
    sd = ImageDraw.Draw(sym)
    # 7 doré à droite
    sd.polygon([(61, 44), (70, 44), (70, 46), (66, 56), (63, 56), (67, 47), (61, 47)], fill=hexc('#f2c14e'))
    sd.line([(62, 45), (69, 45)], fill=hexc('#fff1a8'))
    sym = outline(sym, INK)

    img.alpha_composite(body)
    img.alpha_composite(ruff)
    img.alpha_composite(face)
    img.alpha_composite(hat)
    img.alpha_composite(sym)
    d = ImageDraw.Draw(img)
    sparkle(d, 72, 40, '#fff1a8')
    sparkle(d, 8, 62, '#fff1a8')
    sparkle(d, 58, 60, '#ffffff')

    img = frame(img, '#0a0510', '#f4ead8')
    return img


if __name__ == '__main__':
    up(build()).save('assets/cards/special/joker.png')
