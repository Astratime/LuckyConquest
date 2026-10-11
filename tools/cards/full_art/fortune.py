import math
import random
import numpy as np
from PIL import Image, ImageDraw
from px import *

INK = '#0c1a12'
GOLD = ['#fff6c4', '#f7d76a', '#e0a83a', '#b0752a', '#6e4214']


def coin(img, cx, cy, w=4, h=2.4, edge=False):
    img = sphere(img, cx, cy, w, h, GOLD[:4], dither=0.2)
    d = ImageDraw.Draw(img)
    d.point((int(cx - 1), int(cy - 1)), fill=hexc('#fffbe6'))
    if edge:
        d.line([(cx - w + 1, cy + h), (cx + w - 1, cy + h)], fill=hexc('#8a5a1c'))
    return img


def build():
    random.seed(7)
    img = layer()
    x, y = grid()
    full = np.ones((H, W), bool)
    # velours royal vert à motif de losanges + rayons de lumière depuis la couronne
    u = np.floor(x / 8 + y / 8) + np.floor(x / 8 - y / 8)
    pat = (u % 2) == 0
    dist = np.sqrt((x - 40) ** 2 + ((y - 26) * 0.9) ** 2) / 70
    img = ramp(img, full & pat, dist, ['#3a8a5a', '#2a6e46', '#1d5435', '#123a24'], dither=0.3)
    img = ramp(img, full & ~pat, dist, ['#317a4e', '#235e3c', '#18482d', '#0e2f1d'], dither=0.3)
    ang = np.arctan2(y - 24, x - 40)
    rays = ((np.floor((ang + math.pi) / (2 * math.pi) * 20) % 2) == 0) & (dist < 0.75) & (y > 6)
    arr = np.array(img)
    yy, xx = np.mgrid[0:H, 0:W]
    lit = rays & (((xx + yy) % 2) == 0)
    arr[lit] = (np.array(arr[lit], float) * 0.55 + np.array(hexc('#f7d76a'), float) * 0.45).astype(np.uint8)
    img = Image.fromarray(arr)

    # pièces qui tombent
    for (cx, cy) in [(14, 44), (66, 40), (24, 56), (58, 54), (10, 66), (70, 62), (30, 62), (52, 64)]:
        c = layer()
        c = coin(c, cx, cy, 3.4, 2.2, edge=True)
        img.alpha_composite(outline(c, INK))

    # tas de pièces
    pile = layer()
    pd = ImageDraw.Draw(pile)
    pd.chord([2, 64, 78, 124], 180, 360, fill=hexc('#b0752a'))
    pd.rectangle([2, 94, 78, 112], fill=hexc('#b0752a'))
    pile = sphere(pile, 40, 96, 38, 30, GOLD[1:], light=(-0.2, -0.9), mask=(np.array(pile)[:, :, 3] > 0))
    pile = outline(pile, INK)
    # pièces posées sur le tas
    spots = []
    for row in range(11):
        yrow = 72 + row * 4
        half = min(36, 10 + row * 5.5)
        n = int(half / 4)
        for i in range(-n, n + 1):
            spots.append((40 + i * 7 + (3.5 if row % 2 else 0) + random.uniform(-1, 1), yrow + random.uniform(-0.5, 0.5)))
    for (cx, cy) in spots:
        if abs(cx - 40) > 34:
            continue
        c = layer()
        c = coin(c, cx, cy, 3.4, 2.0, edge=True)
        pile.alpha_composite(outline(c, '#6e4214'))
    # gemmes dans le tas
    for (gx, gy, col) in [(22, 82, ['#ff9a9a', '#e0303c', '#8e1420']), (58, 80, ['#9ad0ff', '#3a6fd8', '#1d3a8a']),
                          (44, 88, ['#c9ffb0', '#3fae4a', '#1f6a2a'])]:
        g = layer()
        gd = ImageDraw.Draw(g)
        gd.polygon([(gx, gy - 3), (gx + 3, gy), (gx, gy + 3), (gx - 3, gy)], fill=hexc(col[1]))
        gd.polygon([(gx, gy - 3), (gx, gy), (gx - 3, gy)], fill=hexc(col[0]))
        gd.polygon([(gx, gy + 3), (gx + 3, gy), (gx, gy)], fill=hexc(col[2]))
        pile.alpha_composite(outline(g, INK))

    # épée plantée dans le trésor : les gains deviennent de la force
    sw = layer()
    sd = ImageDraw.Draw(sw)
    sd.polygon([(38, 56), (42, 56), (42, 80), (40, 83), (38, 80)], fill=hexc('#c3c9d2'))
    sd.line([(38, 56), (38, 80)], fill=hexc('#ffffff'))
    sd.line([(42, 56), (42, 80)], fill=hexc('#7a828e'))
    sd.line([(40, 57), (40, 79)], fill=hexc('#eef1f5'))
    sd.rectangle([30, 52, 50, 55], fill=hexc('#e0a83a'))
    sd.line([(30, 52), (50, 52)], fill=hexc('#fff6c4'))
    sd.line([(30, 55), (50, 55)], fill=hexc('#8a5a1c'))
    sw = sphere(sw, 30, 53.5, 2, 2, GOLD[:4])
    sw = sphere(sw, 50, 53.5, 2, 2, GOLD[:4])
    sd = ImageDraw.Draw(sw)
    sd.rectangle([38, 44, 42, 51], fill=hexc('#6e3a1e'))
    for k in range(44, 51, 2):
        sd.line([(38, k), (42, k)], fill=hexc('#a0602e'))
    sw = sphere(sw, 40, 41.5, 2.6, 2.6, ['#ff9a9a', '#e0303c', '#8e1420'])
    sw = outline(sw, INK)

    # couronne
    cr = layer()
    cd = ImageDraw.Draw(cr)
    cd.polygon([(16, 38), (14, 14), (24, 24), (32, 10), (40, 20), (48, 10), (56, 24), (66, 14), (64, 38)],
               fill=hexc('#e0a83a'))
    cm = np.array(cr)[:, :, 3] > 0
    cr = ramp(cr, cm, np.clip((x - 12) / 60 + (y - 10) / 80, 0, 1), GOLD[:4], dither=0.35)
    cd = ImageDraw.Draw(cr)
    # bandeau
    cd.rectangle([16, 32, 64, 38], fill=hexc('#b0752a'))
    cd.line([(16, 32), (64, 32)], fill=hexc('#fff6c4'))
    cd.line([(16, 38), (64, 38)], fill=hexc('#6e4214'))
    # reflets verticaux
    cd.line([(24, 25), (24, 31)], fill=hexc('#fff6c4'))
    cd.line([(40, 21), (40, 31)], fill=hexc('#fff6c4'))
    cr = outline(cr, INK)
    # joyaux du bandeau et des pointes
    for (jx, jy, col) in [(22, 35, ['#ff9a9a', '#e0303c', '#8e1420']), (31, 35, ['#9ad0ff', '#3a6fd8', '#1d3a8a']),
                          (40, 35, ['#c9ffb0', '#3fae4a', '#1f6a2a']), (49, 35, ['#9ad0ff', '#3a6fd8', '#1d3a8a']),
                          (58, 35, ['#ff9a9a', '#e0303c', '#8e1420'])]:
        cr = sphere(cr, jx, jy, 2.2, 2.0, col, dither=0.1)
    for (jx, jy) in [(14, 13), (32, 9), (48, 9), (66, 13)]:
        cr = sphere(cr, jx, jy, 2.4, 2.4, ['#ffffff', '#e8e2f0', '#a89cb8'])
    cr = sphere(cr, 40, 18, 2.6, 2.6, ['#ff9a9a', '#e0303c', '#8e1420'])
    cr = outline(cr, INK)

    img.alpha_composite(pile)
    img.alpha_composite(sw)
    for (cx, cy) in [(35, 81), (45, 81), (40, 84)]:
        c = coin(layer(), cx, cy, 3.4, 2.0, edge=True)
        img.alpha_composite(outline(c, '#6e4214'))
    img.alpha_composite(cr)
    d = ImageDraw.Draw(img)
    for (sx, sy, big) in [(8, 24, True), (72, 28, True), (30, 46, False), (54, 46, False), (66, 72, False), (12, 78, False)]:
        sparkle(d, sx, sy, '#fffbe6', big)

    img = frame(img, '#06110b', '#fff1c2')
    return img


if __name__ == '__main__':
    up(build()).save('assets/cards/special_full/fortune_du_roi.png')
