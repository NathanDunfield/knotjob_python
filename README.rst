KnotJob Python
==============

A small Python interface to Dirk Schütz's `KnotJob
<https://www.maths.dur.ac.uk/users/dirk.schuetz/knotjob.html>`_.

Originally written for the experiments included in `[DG]
<https://arxiv.org/abs/2512.21825>`_ and `[DLS]
<https://arxiv.org/abs/2312.09114>`_, it doesn't wrap all of KnotJob's
features, but I welcome pull requests that add such.


Installation
============

You will need Java installed so that ``java`` and ``javac`` are in
your path.  On macOS, if you use ``brew`` you can install Java via::

    brew install openjdk

and then, as per the install instructions, making::

  /Library/Java/JavaVirtualMachines/openjdk.jdk

be a symlink into the cellar.

Now download the ``knotjob_python`` source code.  First, in the java
subdirectory, do::

  ./build.sh

Then, in this directory, do::

  sage -pip install .

Here, a copy of KnotJob is embedded as part of the Python
module. Finally, test with::

  sage -python -m knotjob.test

You should see "TestResults(failed=0, attempted=39)" or similar.  The
main function can take a raw PD code, but typically one uses SnapPy
links as input::

  >>> import knotjob, snappy
  >>> L = snappy.Link('K5a1')
  >>> knotjob.s_invariants(L)
  {'s': {0: 2, 2: 2, 3: 2}, 'sq^1': [2, 2, 2, 2], 'sq^1_odd': [2, 2, 2, 2]}

