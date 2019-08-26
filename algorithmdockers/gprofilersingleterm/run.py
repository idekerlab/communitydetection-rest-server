#!/usr/bin/env python

import os
import sys
import argparse
import json
from contextlib import redirect_stdout
from gprofiler import GProfiler
import pandas as pd


def _parse_arguments(desc, args):
    """
    Parses command line arguments
    :param desc:
    :param args:
    :return:
    """
    help_fm = argparse.ArgumentDefaultsHelpFormatter
    parser = argparse.ArgumentParser(description=desc,
                                     formatter_class=help_fm)
    parser.add_argument('input',
                        help='comma delimited list of genes in file')
    parser.add_argument('--maxpval', type=float, default=0.00001,
                        help='Max p value')
    parser.add_argument('--organism', default='hsapiens',
                        help='Organism to use')
    return parser.parse_args(args)


def read_inputfile(inputfile):
    """

    :param inputfile:
    :return:
    """
    with open(inputfile, 'r') as f:
        return f.read()


def run_gprofiler(inputfile, theargs):
    """
    todo
    :param inputfile:
    :return:
    """
    genes = read_inputfile(inputfile)
    gp = GProfiler(return_dataframe=True)
    genes = genes.strip(',').strip('\n').split(',')
    df_result = gp.profile(query=genes, organism=theargs.organism,
                           user_threshold=theargs.maxpval)
    if df_result.shape[0] == 0:
        sys.stderr.write('No terms found\n')
        return 0

    df_result['Jaccard'] = 1.0 / (1.0 / df_result['precision'] + 1.0 / df_result['recall'] - 1)
    df_result.sort_values(['Jaccard', 'p_value'], ascending=[False, True], inplace=True)
    df_result.reset_index(drop=True, inplace=True)
    top_hit = df_result['name'][0]
    sys.stdout.write(top_hit)
    return 0


def main(args):
    """
    Main entry point for program

    :param args: command line arguments usually :py:const:`sys.argv`
    :return: 0 for success otherwise failure
    :rtype: int
    """
    desc = """
        Running gprofiler-official 1.0.0, with python3!
        
        Takes file with comma delimited list of genes as input and
        outputs matching term if any
    """

    theargs = _parse_arguments(desc, args[1:])

    try:
        inputfile = os.path.abspath(theargs.input)

        return run_gprofiler(inputfile, theargs)
    except Exception as e:
        sys.stderr.write('Caught exception: ' + str(e))
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))
