import subprocess
import os
import shutil
from pathlib import Path

ANNOTATOR_JAR = "{}/.m2/repository/edu/ucr/cs/riple/annotator/annotator-core/1.3.7-SNAPSHOT/annotator-core-1.3.7-SNAPSHOT.jar".format(str(Path.home()))
REPO = subprocess.check_output(['git', 'rev-parse', '--show-toplevel']).strip().decode('utf-8')


def prepare():
    os.makedirs('/tmp/ucr-tainting', exist_ok=True)
    shutil.rmtree('/tmp/ucr-tainting/0', ignore_errors=True)
    with open('/tmp/ucr-tainting/paths.tsv', 'w') as o:
        o.write("{}\t{}\n".format('/tmp/ucr-tainting/taint.xml', '/tmp/ucr-tainting/scanner.xml'))


def run_annotator():
    prepare()
    commands = []
    commands += ["java", "-jar", ANNOTATOR_JAR]
    commands += ['-d', '/tmp/ucr-tainting']
    commands += ['-bc', 'cd {} && ./gradlew compileJava'.format(REPO)]
    commands += ['-cp', '/tmp/ucr-tainting/paths.tsv']
    commands += ['-i', 'edu.ucr.Initializer']
    commands += ['-n', 'edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted']
    commands += ['-cn', 'UCRTaint']
    # Uncomment to see build output
    # commands += ['-rboserr']

    subprocess.call(commands)


run_annotator()
