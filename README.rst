KnotJob Python
==============

A small Python interface to Dirk Schütz's `KnotJob
<https://www.maths.dur.ac.uk/users/dirk.schuetz/knotjob.html>`_.

Originally written for the experiments included in `[DG]
<https://arxiv.org/abs/2512.21825>`_ and `[DLS]
<https://arxiv.org/abs/2312.09114>`_, it doesn't wrap all of KnotJob's
features, but I welcome pull requests that add more.


Installation
============

You will need Java installed so that ``java`` and ``javac`` are in
your path.  On macOS, if you use ``brew`` you can install Java via::

    brew install openjdk

and then, as per the install instructions, making::

  /Library/Java/JavaVirtualMachines/openjdk.jdk

be a symlink into the brew cellar.

Now download the ``knotjob_python`` source code. In the resulting
directory do::

  java/build.sh

You should see something like::

  Using Java: javac 25.0.2
  Compiling...
  Building jar file...
  Testing...
  Knot
  S-Invariant mod 0 : 2
  [...]
  Reduced integral sl_3 Homology : t^-8 q^30 + t^-7 q^26 + t^-7 q^28 + t^-6 q^24 + t^-5 q^22 + t^-5 q^24 + t^-4 q^18 + t^-4 q^20 + t^-3 q^20 + t^-2 q^16 + q^12
  Copying to python_src...
  Build complete!

Then do::

  python -m pip install .
  python -m knotjob.test

You should see ``TestResults(failed=0, attempted=53)`` or similar.  If
you are using SageMath, replace ``python`` with ``sage -python`` in
the above.


Usage
=====

The main function can take a raw PD code, but typically one uses
SnapPy links as input::

  >>> import knotjob, snappy
  >>> L = snappy.Link('K5a1')
  >>> knotjob.s_invariants(L)
  {'s': {0: 2, 2: 2, 3: 2}, 'sq^1': [2, 2, 2, 2], 'sq^1_odd': [2, 2, 2, 2]}

Here's the invariant from Proposition 6.28 of `[DLS]
<https://arxiv.org/abs/2312.09114>`_,

  >>> knotjob.complete_ls(L)
  {'complete_ls': (2, 2)}

KnotJob runs in subprocess and Python communicates with it via
pipes. You can control which invariants are computed more directly::

  >>> kj = knotjob.KnotJob()
  >>> pd = [(6,4,1,3), (2,6,3,5), (4,2,5,1)]
  >>> invs = [('Kh', 'odd', 'Z'), ('Kh', 'reduced', 'Q'), ('s', 'F_13')]
  >>> kj.compute_one_link(pd, invs)
  {('s', 'F_13'): 2,
   ('Kh', 'reduced', 'Q'): {(2, 0): 1, (6, 2): 1, (8, 3): 1},
   ('Kh', 'odd', 'Z'): {0: {(2, 0): 1, (6, 2): 1, (8, 3): 1}}}

For more, see the docstrings the `core.py <https://github.com/NathanDunfield/knotjob_python/blob/main/python_src/core.py>`_ file.


Version
=======

You can check which version of KnotJob is being used as follows::

  >>> import knotjob
  >>> knotjob.knotjob_version
  'aquamarine (2025)'
