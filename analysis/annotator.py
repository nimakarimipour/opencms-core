import subprocess
import os
import shutil
from pathlib import Path

VERSION = '1.3.8-SNAPSHOT'
ANNOTATOR_JAR = "{}/.m2/repository/edu/ucr/cs/riple/annotator/annotator-core/{}/annotator-core-{}.jar".format(str(Path.home()), VERSION, VERSION)
REPO = subprocess.check_output(['git', 'rev-parse', '--show-toplevel']).strip().decode('utf-8')


def prepare():
    os.makedirs('/tmp/ucr-tainting/opencms', exist_ok=True)
    shutil.rmtree('/tmp/ucr-tainting/opencms/0', ignore_errors=True)
    with open('/tmp/ucr-tainting/opencms/paths.tsv', 'w') as o:
        o.write("{}\t{}\n".format('/tmp/ucr-tainting/opencms/taint.xml', '/tmp/ucr-tainting/opencms/scanner.xml'))


def run_annotator():
    prepare()
    commands = []
    commands += ["java", "-jar", ANNOTATOR_JAR]
    commands += ['-d', '/tmp/ucr-tainting/opencms']
    commands += ['-bc', 'cd {} && ./gradlew compileJava'.format(REPO)]
    commands += ['-cp', '/tmp/ucr-tainting/opencms/paths.tsv']
    commands += ['-i', 'edu.ucr.Initializer']
    commands += ['-n', 'edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted']
    commands += ['-cn', 'UCRTaint']
    # Uncomment to see build output
    # commands += ['-rboserr']
    # Uncomment to disable outer loop
    commands += ['-dol']
    # Uncomment to disable parallel processing
    commands += ['-dpp']

    subprocess.call(commands)


run_annotator()
