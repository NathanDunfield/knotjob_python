import os
import sys
import time
from subprocess import Popen, PIPE

jar_path = os.path.join(os.path.dirname(__file__), 'KnotJob.jar')

computable = [
    ['Kh', ('reduced', 'unreduced', 'odd'), ('Z', 'Q', 'F_p')],
    ['sl_3', ('reduced', 'unreduced'), ('Z')],
    ['s',  ('Z', 'Q', 'F_p')],  # p prime and <= 211.
    ['sl_3_s',  ('Q', 'F_p')],  # p prime and <= 211.
    ['Sq^1', ('even', 'odd', 'sum')],
    ['Sq^2', ('even', 'odd_eps0', 'odd_eps1')],
    ['BLS', ('odd_n')],  # n integer between 3 and 15
    ['CLS'], # reduced, which is enough
    ['xtortion', ('Z', 'Q', 'F_p')]
]

ring_name = {'Z': 'integral', 'Q': 'rational'}

allowed_prime_powers = [2, 3, 4, 5, 7, 8, 9, 11, 13, 16, 17, 19, 23, 25, 27,
                        29, 31, 32, 37, 41, 43, 47, 49, 53, 59, 61, 64, 67, 71,
                        73, 79, 81, 83, 89, 97, 101, 103, 107, 109, 113, 121,
                        125, 127, 128, 131, 137, 139, 149, 151, 157, 163, 167,
                        169, 173, 179, 181, 191, 193, 197, 199, 211]

def ring_to_flag(ring):
    if ring == 'Z':
        return '0'
    if ring == 'Q':
        return '1'
    if ring.startswith('F_'):
        p = ring.split('_')[-1]
        assert p.isdecimal()
        if int(p) not in allowed_prime_powers:
            raise ValueError('p must be a prime power < 212')
        return p
    raise ValueError('Ring not Z, Q, or F_p')


def flag_and_output_prefix(prop):
    """
    >>> flag_and_output_prefix(('Kh', 'odd', 'Z'))
    ('-ko0', 'odd integral khovanov homology')
    >>> flag_and_output_prefix(('Kh', 'reduced', 'Q'))
    ('-kr1', 'rational reduced khovanov homology')
    >>> flag_and_output_prefix(('Kh', 'unreduced', 'F_9'))
    ('-ku9', 'unreduced khovanov homology mod 9')
    >>> flag_and_output_prefix(('s', 'F_2'))
    ('-s2', 's-invariant mod 2')
    >>> flag_and_output_prefix(('sl_3_s', 'F_2'))
    ('-sl2', 'sl_3 s-invariant mod 2')
    >>> flag_and_output_prefix(('Sq^1', 'even'))
    ('-sqe', 'even sq^1 invariant')
    >>> flag_and_output_prefix(('Sq^2', 'odd_eps0'))
    ('-sq2o0', 'odd sq^2 invariant (eps=0)')
    >>> flag_and_output_prefix(('BLS', 'odd_15'))
    ('-sbls15', 'odd bls 32768 invariant')
    >>> flag_and_output_prefix(('xtortion', 'F_5'))
    ('-kx5', 'extortion order mod 5')
    >>> flag_and_output_prefix(('sl_3', 'reduced', 'Z'))
    ('-ks0', 'reduced integral sl_3 homology')
    """
    if prop[0] == 'Kh':
        kind, ring = prop[1:]
        if ring in ('Z', 'Q'):
            prefix = ' khovanov homology'
            if kind == 'odd':
                prefix = 'odd ' + ring_name[ring] + prefix
                return '-ko' + ring_to_flag(ring), prefix
            else:
                if kind not in ['reduced', 'unreduced']:
                    raise ValueError('Kh kind not reduced, unreduced, or odd')
                prefix = ring_name[ring] + ' ' + kind + prefix
                return '-k' + kind[0] + ring_to_flag(ring), prefix
        else:
            p = ring_to_flag(ring)
            prefix = kind + ' khovanov homology mod ' + p
            return '-k' + kind[0] + p, prefix

    if prop[0] == 'sl_3':
        kind, ring = prop[1:]
        if ring != 'Z':
            raise ValueError('sl_3 homology only available over Z')
        if kind not in ['reduced', 'unreduced']:
            raise ValueError('sl_3 homology kind not reduced or unreduced')
        return '-ks0', kind + ' integral sl_3 homology'

    if prop[0] == 's':
        ring = prop[1]
        if ring == 'Z':
            return '-sgr', 'graded s-invariant'
        p = '0' if ring == 'Q' else ring_to_flag(ring)
        if int(p) > 211:
            raise ValueError('Field larger than KnotJob accepts')
        return '-s' + p, 's-invariant mod ' + p

    if prop[0] == 'sl_3_s':
        ring = prop[1]
        p = '0' if ring == 'Q' else ring_to_flag(ring)
        if int(p) > 211:
            raise ValueError('Field larger than KnotJob accepts')
        return '-sl' + p, 'sl_3 s-invariant mod ' + p

    if prop[0] == 'Sq^1':
        kind = prop[1]
        if kind not in ['even', 'odd', 'sum']:
            raise ValueError('Sq^1 knot not even, odd, or sum')
        return '-sq' + kind[0], kind + ' sq^1 invariant'

    if prop[0] == 'Sq^2':
        kind = prop[1]
        flags = {'even':'-sq2e', 'odd_eps0': '-sq2o0', 'odd_eps1': '-sq2o1'}
        if kind not in flags:
            raise ValueError('Sq^1 knot not even or odd_eps0 or odd_eps1')
        if kind == 'even':
            prefix = 'even sq^2 invariant'
        else:
            prefix = 'odd sq^2 invariant (eps=' + kind[-1] + ')'
        return flags[kind], prefix

    if prop[0] == 'BLS':
        kind, exponent = prop[1].split('_')
        if kind != 'odd':
            raise ValueError('Only odd variant supported')
        exponent = int(exponent)
        prefix = f'odd bls {2**exponent} invariant'
        return f'-sbls{exponent}', prefix

    if prop[0] == 'CLS':
        return '-scr', 'complete reduced ls-invariant'

    if prop[0] == 'xtortion':
        p = ring_to_flag(prop[1])
        # For characteristic 0, KnotJob's convention does not match
        # that of the Khovanov homology flags.
        if p == '1':
            p = '0'
        prefix = 'extortion order mod ' + p
        return '-kx' + p, prefix


def parse_Kh(homology_string):
    """
    Return as a dictionary of the form::

      (q-grading, t-grading): rank

    >>> parse_Kh('t^-2 q^-2 + t^-1 q^0 + 2 t^0 q^2'
    ...          ' + 2 t^2 q^6 + 2 t^3 q^8 + t^4 q^10')
    {(-2, -2): 1, (0, -1): 1, (2, 0): 2, (6, 2): 2, (8, 3): 2, (10, 4): 1}

    >>> parse_Kh('t^-2 q^-2 + t^-1 + 2 q^2'
    ...          ' + 2 t^2 q^6 + 2 t^3 q^8 + t^4 q^10')
    {(-2, -2): 1, (0, -1): 1, (2, 0): 2, (6, 2): 2, (8, 3): 2, (10, 4): 1}
    >>> parse_Kh('3 t + q + q^3 + t^2 q^5 + t^3 q^9')
    {(0, 1): 3, (1, 0): 1, (3, 0): 1, (5, 2): 1, (9, 3): 1}

    This convention is the reverse of KnotJobs, but matches
    knot_floer_homology.
    """
    ans = {}
    for term in homology_string.split(' + '):
        parts = term.strip().split(' ')
        c = 1
        t_exp, q_exp = 0, 0
        for part in parts:
            if part.startswith('t^'):
                t_exp = int(part[2:])
            elif part.startswith('q^'):
                q_exp = int(part[2:])
            elif part == 't':
                t_exp = 1
            elif part == 'q':
                q_exp = 1
            else:
                c = int(part)

        ans[q_exp, t_exp] = c
    return ans


def parse_graded(spec):
    """
    >>> parse_graded('2')
    (2,)
    >>> parse_graded('2 (3)')
    (2, 3)
    >>> parse_graded('2 (3,1)')
    (2, 3, 1)
    """

    parts = spec.split()
    if len(parts) == 1:
        return (int(spec),)

    assert len(parts) == 2
    s, tor = parts
    assert tor[0] == '(' and tor[-1] == ')'
    return (int(s),) + eval(tor[:-1] + ',)')

class KnotJob:
    """
    An interface for interacting with the Java program KnotJob.  Each
    instance computes a particular fixed list of Khovanov invariants
    which are passed as flags to KnotJob.

    >>> kj = KnotJob([('Kh', 'odd', 'Z'), ('Kh', 'reduced', 'Q'), ('s', 'F_13')])
    >>> pd = [(6,4,1,3), (2,6,3,5), (4,2,5,1)]
    >>> kj.compute_one_link(pd)   # doctest: +NORMALIZE_WHITESPACE
    {('s', 'F_13'): 2,
     ('Kh', 'reduced', 'Q'): {(2, 0): 1, (6, 2): 1, (8, 3): 1},
     ('Kh', 'odd', 'Z'): {0: {(2, 0): 1, (6, 2): 1, (8, 3): 1}}}
    >>> kgr = KnotJob([('s', 'Z')])
    >>> double = [(28,5,1,6),(1,10,2,11),(9,2,10,3),(16,4,17,3),(4,16,5,15),
    ...           (6,11,7,12),(24,8,25,7),(8,24,9,23),(19,13,20,12),(13,26,14,27),
    ...           (21,14,22,15),(17,22,18,23),(25,18,26,19),(27,21,28,20)]
    >>> kgr.compute_one_link(double)
    {('s', 'Z'): ((0, 2), (0,))}

    Here's one where there is 3-torsion in Kh:
    >>> pd =  [(3,1,4,26),(1,8,2,9),(7,2,8,3),(9,5,10,4),(5,14,6,15),(13,6,14,7)]
    >>> pd += [(10,20,11,19),(24,12,25,11),(12,17,13,18),(22,16,23,15)]
    >>> pd += [(16,22,17,21),(18,26,19,25),(20,23,21,24)]
    >>> kj.compute_one_link(pd)['Kh', 'odd', 'Z'][3]
    {(-2, -1): 1, (0, 0): 3, (2, 1): 4, (4, 2): 3, (6, 3): 1}

    We can do the sl_3 homology, but we need to ask for both reduced
    and unreduced:

    >>> pd = [(8,14,9,13),(3,15,4,14),(15,11,16,10),(4,10,5,9),
    ...       (16,6,1,5),(11,7,12,6),(7,3,8,2),(12,2,13,1)]
    >>> invs = [('sl_3', 'reduced', 'Z'), ('sl_3', 'unreduced', 'Z')]
    >>> kj.compute_one_link(pd, invs)['sl_3', 'unreduced', 'Z'][3]
    {(28, -7): 1, (30, -7): 1, (24, -6): 1, (26, -6): 1, (20, -4): 1, (22, -4): 1, (18, -2): 1}
    """
    def __init__(self, default_invariants=None, max_heap_size=None):
        self.default_invariants = default_invariants
        cmd = ['java', '-XX:+ExitOnOutOfMemoryError']
        if max_heap_size:
            cmd.append('-Xmx' + max_heap_size)
        cmd += ['-jar', jar_path, '-print-dones']
        self.popen = Popen(cmd, universal_newlines=True,  # same as text=True in Python >= 3.7
                           stdin=PIPE, stdout=PIPE, stderr=PIPE)

    def __del__(self):
        if self.popen.poll() is None:
            # Process still running, try to shut it down.
            self.popen.stdin.write('\n')
            self.popen.stdin.flush()
            if self.popen.poll() is None:
                self.popen.kill()

    def _run_computation(self, input_data):
        kj_in, kj_out = self.popen.stdin, self.popen.stdout
        kj_in.write(input_data.strip() + '\n')
        kj_in.flush()
        ans = []
        while True:
            line = kj_out.readline()
            if len(line) > 0:
                if line.startswith('computation done'):
                    break
                elif line.find('[warning][os,container]') > -1:
                    # Java prints useless warnings to stdout.
                    continue
                else:
                    line = line.lower().strip()
                    if line != 'knot':
                        ans.append(line)
            else:
                # KnotJob likely hit an exception and terminated
                time.sleep(5)
                retcode = self.popen.poll()
                err = self.popen.stderr.read()
                if retcode is None and not err:
                    raise RuntimeError('KnotJob returned empty line')
                if not err and ans:
                    err = ans[-1]
                else:
                    err = f'return code was {retcode}'
                raise RuntimeError('KnotJob terminated: ' + err)
        return ans

    def compute_one_link(self, pd_code, invariants=None):
        if invariants is None:
            invariants = self.default_invariants
        if invariants is None:
            raise ValueError('No invariants for KnotJob to compute were given')
            
        if not isinstance(pd_code, str):
            if hasattr(pd_code, 'PD_code'):
                pd_code = repr(pd_code.PD_code(min_strand_index=1))
            else:
                pd_code = repr(pd_code)


        cmd = ['-print-dones']
        prefix_to_invariant = dict()
        for inv in invariants:
            flag, prefix = flag_and_output_prefix(inv)
            cmd.append(flag)
            prefix_to_invariant[prefix] = inv

        cmd_with_pd_code = ' '.join(cmd) + '\n' + pd_code

        ans = dict()
        for line in self._run_computation(cmd_with_pd_code):
            prefix, output = line.split(' : ')
            if prefix not in prefix_to_invariant:
                if prefix.startswith('torsion of order'):
                    assert inv[0] in ['Kh', 'sl_3'] and inv[2] == 'Z'
                    torsion_order = int(prefix.strip().split()[-1])
                    ans[inv][torsion_order] = parse_Kh(output)
                    continue
                else:
                    raise RuntimeError('Cannot parse KnotJob output')

            inv = prefix_to_invariant[prefix]
            if inv == ('s', 'Z'):
                a, b = output.split('. ')
                output = (parse_graded(a), parse_graded(b))
            elif inv == ('CLS', 'unreduced'):
                output = output.strip()
            elif inv[0] not in ['Kh', 'sl_3']:
                output = eval(output)
            elif inv[2] == 'Z':
                output = {0:parse_Kh(output)}
            else:
                output = parse_Kh(output)
            ans[inv] = output

        if len(ans) != len(invariants):
            raise RuntimeError('Did not compute all requested invariants')

        return ans


default_knot_job = None


def get_default_knot_job():
    global default_knot_job
    if default_knot_job is None:
        default_knot_job = KnotJob()
    return default_knot_job


def s_invariants(pd_code):
    """
    Computes Rasmussen's s-invariant over Q, F_2, and F_3, and the
    Lipshitz-Sarkar Sq^1-invariants for both the even and odd Khovanov
    homologies.

    >>> pd =  [(3,1,4,28),(1,16,2,17),(15,2,16,3),(4,9,5,10),(12,5,13,6)]
    >>> pd += [(25,7,26,6),(7,25,8,24),(8,13,9,14),(21,11,22,10),(11,21,12,20)]
    >>> pd += [(17,15,18,14),(18,23,19,24),(26,19,27,20),(22,27,23,28)]
    >>> s_invariants(pd)
    {'s': {0: 0, 2: -2, 3: 0}, 'sq^1': (0, 0, -2, -2), 'sq^1_odd': (0, -2)}
    """
    invs = [('s', 'Q'), ('s', 'F_2'), ('s', 'F_3'), ('Sq^1', 'even'), ('Sq^1', 'odd')]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    s_inv = {0: result['s', 'Q'], 2: result['s', 'F_2'],  3: result['s', 'F_3']}
    return {'s': s_inv, 'sq^1': result['Sq^1', 'even'], 'sq^1_odd': result['Sq^1', 'odd']}


def s_invariants_lite(pd_code):
    """
    Computes Rasmussen's s-invariant over F_2 and F_3

    >>> pd =  [(3,1,4,28),(1,16,2,17),(15,2,16,3),(4,9,5,10),(12,5,13,6)]
    >>> pd += [(25,7,26,6),(7,25,8,24),(8,13,9,14),(21,11,22,10),(11,21,12,20)]
    >>> pd += [(17,15,18,14),(18,23,19,24),(26,19,27,20),(22,27,23,28)]
    >>> s_invariants_lite(pd)
    {'s': {2: -2, 3: 0}}
    """
    invs = [('s', 'F_2'), ('s', 'F_3')]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    s_inv = {2: result['s', 'F_2'],  3: result['s', 'F_3']}
    return {'s': s_inv}


def sq_two_invariants(pd_code):
    """
    Computes the Lipshitz-Sarkar Sq^2 invariants for the even theory
    and two versions of the odd theory (with epsilon=0 and epsilon=1).

    >>> pd = [(5,1,6,10),(1,7,2,6),(9,3,10,2),(3,9,4,8),(7,5,8,4)]
    >>> sq_two_invariants(pd)
    {'sq^2': (2, 2, 2, 2), 'sq^2_odd0': (2, 2, 2, 2), 'sq^2_odd1': (2, 2, 2, 2)}
    """
    invs = [('Sq^2', 'even'), ('Sq^2', 'odd_eps0'), ('Sq^2', 'odd_eps1')]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    return {'sq^2': result['Sq^2', 'even'],
            'sq^2_odd0': result['Sq^2', 'odd_eps0'],
            'sq^2_odd1': result['Sq^2', 'odd_eps1']}


def bls_invariants(pd_code):
    """
    Computes the BLS invariant from odd Khovanov homology with parameter 2^15

    >>> pd = [(5,1,6,10),(1,7,2,6),(9,3,10,2),(3,9,4,8),(7,5,8,4)]
    >>> bls_invariants(pd)
    {'bls_odd': (2, 2)}
    """
    invs = [('BLS', 'odd_15')]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    return {'bls_odd': result['BLS', 'odd_15']}


def sq_one_sum(pd_code):
    """
    Computes the BLS invariant from odd Khovanov homology with parameter 2^15

    >>> pd = [(5,1,6,10),(1,7,2,6),(9,3,10,2),(3,9,4,8),(7,5,8,4)]
    >>> sq_one_sum(pd)
    {'sq1_sum': (2, 2, 2, 2)}
    """
    invs = [('Sq^1', 'sum')]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    return {'sq1_sum': result['Sq^1', 'sum']}


def complete_ls(pd_code):
    """
    Computes the complete reduced Lipshitz-Sarkar invariant

    >>> pd = [(5,1,6,10),(1,7,2,6),(9,3,10,2),(3,9,4,8),(7,5,8,4)]
    >>> complete_ls(pd)
    {'complete_ls': (2, 2)}
    """
    invs = [('CLS',)]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    return {'complete_ls': result[('CLS',)]}


def sl_3_s_invariants(pd_code):
    """
    Computes sl_3 s-invariant over Q, F_2, and F_3,

    >>> pd =  [(3,1,4,28),(1,16,2,17),(15,2,16,3),(4,9,5,10),(12,5,13,6)]
    >>> pd += [(25,7,26,6),(7,25,8,24),(8,13,9,14),(21,11,22,10),(11,21,12,20)]
    >>> pd += [(17,15,18,14),(18,23,19,24),(26,19,27,20),(22,27,23,28)]
    >>> sl_3_s_invariants(pd)
    {0: 0, 2: -2, 3: 0}
    """
    invs = [('sl_3_s', 'Q'), ('sl_3_s', 'F_2'), ('sl_3_s', 'F_3')]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    return {0: result['sl_3_s', 'Q'], 2: result['sl_3_s', 'F_2'],  3: result['sl_3_s', 'F_3']}


def sl_3_s_invariants_lite(pd_code):
    """
    Computes sl_3 s-invariant over Q, F_2, and F_3,

    >>> pd =  [(3,1,4,28),(1,16,2,17),(15,2,16,3),(4,9,5,10),(12,5,13,6)]
    >>> pd += [(25,7,26,6),(7,25,8,24),(8,13,9,14),(21,11,22,10),(11,21,12,20)]
    >>> pd += [(17,15,18,14),(18,23,19,24),(26,19,27,20),(22,27,23,28)]
    >>> sl_3_s_invariants_lite(pd)
    {2: -2, 3: 0}
    """
    invs = [('sl_3_s', 'F_2'), ('sl_3_s', 'F_3')]
    result = get_default_knot_job().compute_one_link(pd_code, invs)
    return {2: result['sl_3_s', 'F_2'],  3: result['sl_3_s', 'F_3']}


if __name__ == '__main__':
    import doctest
    doctest.testmod()
