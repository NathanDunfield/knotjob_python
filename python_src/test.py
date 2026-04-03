from . import core
import doctest
import sys

if __name__ == '__main__':
    results = doctest.testmod(core)
    print(results)
    sys.exit(results[0])
