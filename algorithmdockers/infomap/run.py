#!/opt/conda/bin/python

import os
import sys
import argparse
import numpy as np
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
                        help='Edge file in tab delimited format')
    parser.add_argument('--directed', action='store_true',
                        help='If set, then generate directed graph')
    parser.add_argument('--enableoverlapping', action='store_true',
                        help='If set, disable infomap overlapping')
    parser.add_argument('--outdir', default='/tmp',
                        help='Sets directory where Infomap writes output')
    parser.add_argument('--markovtime', default=0.75, type=float,
                        help='Sets markov-time')
    parser.add_argument('--seed', default=None, type=int,
                        help='Sets seed for random generator')
    return parser.parse_args(args)


def run_infomap_cmd(args):
    """
    Runs docker

    :param cmd_to_run: command to run as list
    :return:
    """
    cmd = ['/home/rstudio/Infomap']
    cmd.extend(args)

    p = subprocess.Popen(cmd,
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE)

    out, err = p.communicate()

    return p.returncode, out, err


def check_if_file_contains_zero(edgelistfile):
    with open(edgelistfile, 'r') as f:
        lines = f.read().splitlines()

    for line in lines:
        elts = line.split()
        if int(elts[0]) == 0:
            return True
        if int(elts[1]) == 0:
            return True
    return False


def get_truncated_file(inputfile):
    """
    Finds last period in file name and strips
    it off and any text to right
    :param inputfile:
    :return:
    """
    inputfilename = os.path.basename(inputfile)
    period_index = inputfilename.rfind('.')
    if period_index == -1:
        return inputfilename

    return inputfilename[0:period_index]


def run_infomap(graph, outdir, markovtime=0.75,
                overlap=False, directed=False, seed=None):

    """
    :outdir: the output directory to comprehend the output link file
    :param graph: input file
    :param overlap: bool, whether to enable overlapping community detection
    :param directed
    :return
    """
    cmdargs = ['-i', 'link-list', '--inner-parallelization',
               '--markov-time', str(markovtime)]
    if check_if_file_contains_zero(graph) is True:
        cmdargs.append('-z')
    if overlap is True:
        cmdargs.append('--overlapping')
    if directed is True:
        cmdargs.append('-d')
    if seed != None:
        cmdargs.append('--seed')
        cmdargs.append(str(seed))
    cmdargs.append(graph)
    cmdargs.append(outdir)

    cmdecode, cmdout, cmderr = run_infomap_cmd(cmdargs)

    if cmdecode != 0:
        sys.stderr.write('Command failed with non-zero exit code: ' +
                         str(cmdecode) + ' : ' + str(cmderr) + '\n')
        return 1

    outfile = get_truncated_file(graph) + '.tree'

    tree_name = os.path.join(outdir, outfile)
    treef = open(tree_name, 'r')
    lines = treef.read().splitlines()
    non_zero_lines = []
    while '#' in lines[0]:
        lines.pop(0)
    for i in range(len(lines)):
        if 0 != float(lines[i].split()[1]):
            non_zero_lines.append(lines[i])
    lines = non_zero_lines
    nrow = len(lines)
    ncol_list = []
    for line in lines:
        ncol_list.append(len(line.split(':')))
    ncol = max(ncol_list)
    treef.close()

    os.unlink(tree_name)

    A = np.zeros((nrow, ncol))
    for i in range(len(lines)):
        Elts = lines[i].split()
        leaf = Elts[2][1:-1]
        links = Elts[0].split(':')
        for j in range(len(links) - 1):
            A[i, j] = int(links[j])
        A[i, -1] = int(leaf)
    maxElt = max(A[:, -1])
    for j in range(A.shape[1] - 1):
        k = A.shape[1] - 2 - j
        lastone = A[0, k]
        A[0, k] = A[0, k] + maxElt
        maxElt = A[0, k]
        for i in range(1, A.shape[0]):
            if A[i, k] == 0:
                continue
            if lastone != A[i, k]:
                maxElt = maxElt + 1
            lastone = A[i, k]
            A[i, k] = maxElt
    root = maxElt + 1

    edges = set()
    for i in range(A.shape[0]):
        edges.add((int(root), int(A[i, 0]), 'c-c'))
        last = int(A[i, A.shape[1] - 2])
        for j in range(0, A.shape[1] - 2):
            if A[i, j + 1] == 0:
                last = int(A[i, j])
                break
            else:
                edges.add((int(A[i, j]), int(A[i, j + 1]), 'c-c'))
        edges.add((last, int(A[i, A.shape[1] - 1]), 'c-m'))

    for edge in edges:
        sys.stdout.write(str(edge[0]) + ',' + str(edge[1]) + ',' + str(edge[2]) + ';')
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
    Runs infomap on command line, sending output to standard
    out 
    """

    theargs = _parse_arguments(desc, args[1:])

    try:
        inputfile = os.path.abspath(theargs.input)
        outdir = os.path.abspath(theargs.outdir)

        if theargs.directed is True:
            dval = True
        else:
            dval = False

        return run_infomap(inputfile, outdir,
                           markovtime=theargs.markovtime, seed=theargs.seed, 
                           overlap=theargs.enableoverlapping,
                           directed=dval)
    except Exception as e:
        sys.stderr.write('Caught exception: ' + str(e))
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))
