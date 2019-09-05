#!/opt/conda/bin/python

import os
import sys
import argparse
import subprocess


def _parse_arguments(desc, args):
    """
    Parses command line arguments
    :param desc:
    :param args:
    :return:
    """
    help_fm = argparse.RawDescriptionHelpFormatter
    parser = argparse.ArgumentParser(description=desc,
                                     formatter_class=help_fm)
    parser.add_argument('input',
                        help='Edge file in tab delimited format of '
                             'node1 node2 edgeWeight')

    parser.add_argument('--alpha', default=0.1, type=float,
                        help='Sets alpha parameter')
    parser.add_argument('--beta', default=0.5, type=float,
                        help='Sets beta parameter')
    return parser.parse_args(args)


def run_clixo_cmd(args):
    """
    Runs docker

    :param cmd_to_run: command to run as list
    :return:
    """
    cmd = ['/clixo/clixo']
    cmd.extend(args)

    p = subprocess.Popen(cmd,
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE)

    out, err = p.communicate()

    return p.returncode, out, err


def run_clixo(graph, alpha=0.1, beta=0.5):

    """
    :outdir: the output directory to comprehend the output link file
    :param graph: input file
    :param overlap: bool, whether to enable overlapping community detection
    :param directed
    :return
    """
    cmdargs = ['-i', graph, '-a', str(alpha),
               '-b', str(beta)]

    cmdecode, cmdout, cmderr = run_clixo_cmd(cmdargs)

    if cmdecode != 1:
        sys.stderr.write('Command failed with non-zero exit code: ' +
                         str(cmdecode) + ' : ' + str(cmderr) + '\n')
        return 1

    termtype = 'c-c'
    lines = cmdout.decode('utf-8').splitlines()
    for line in lines:
        if '#' == line[0]:
            continue
        elts = line.split()
        termcol = elts[2]
        if termcol == 'gene' and termtype == 'c-c':
            termtype = 'c-m'
        if termcol == 'default' and termtype == 'c-m':
            termtype = 'c-c'
        sys.stdout.write(elts[0] + ',' + elts[1] + ',' + termtype + ';')
    sys.stdout.flush()
    return 0


def main(args):
    """
    Main entry point for program

    :param args: command line arguments usually :py:const:`sys.argv`
    :return: 0 for success otherwise failure
    :rtype: int
    """
    desc = """
    Runs clixo on command line, sending output to standard
    out 
    """

    theargs = _parse_arguments(desc, args[1:])

    try:
        inputfile = os.path.abspath(theargs.input)

        return run_clixo(inputfile, alpha=theargs.alpha,
                         beta=theargs.beta)
    except Exception as e:
        sys.stderr.write('Caught exception: ' + str(e))
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))
