from setuptools import setup
import re

# Get version number from module
version = re.search("__version__ = '(.*)'",
                    open('python_src/__init__.py').read()).group(1)

setup(
    name='knotjob',
    version=version,
    author='Dirk Schütz with Nathan Dunfield and Sherry Gong',
    author_email='nathan@dunfield.info',
    license='GPLv3',
    packages=['knotjob'],
    package_dir={'knotjob':'python_src'},
    package_data={'knotjob':['KnotJob.jar']},
    zip_safe = False
)
                
