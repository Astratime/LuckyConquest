"""Les 5 Joker validés par Astra (dessins de jokers_src.py). Le Joker de base prend le dessin « souriant »."""
import jokers_src as J

_FN = {'joker': J.souriant, 'masque_du_joker': J.masque, 'joker_de_cour': J.cour,
       'joker_malveillant': J.malveillant, 'marotte_du_joker': J.marotte}


def _wrap(fn):
    def draw(pal):
        J.colors = lambda name: pal
        return fn()
    return draw


DESIGNS = {cid: _wrap(fn) for cid, fn in _FN.items()}
