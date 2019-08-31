#!/usr/bin/env python

import os
import sys
import time
import argparse

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
                        help='datafile')
    parser.add_argument('--sleeptime', type=int, default=180,
                        help='Number of seconds to sleep')
    parser.add_argument('--exitcode', type=int, default=0,
                        help='Exit code returned by this command')
    return parser.parse_args(args)


def main(args):
    """
    Main entry point for program

    :param args: command line arguments usually :py:const:`sys.argv`
    :return: 0 for success otherwise failure
    :rtype: int
    """
    desc = """
    
    sleeps for --sleeptime defined seconds and then outputs
    
    sleep: X
    
    and exits
    """

    theargs = _parse_arguments(desc, args[1:])

    try:
        sys.stdout.write("sleep: " + str(theargs.sleeptime) + '\n')
        sys.stdout.flush()
        time.sleep(theargs.sleeptime)
        return theargs.exitcode
    except Exception as e:
        sys.stderr.write('Caught exception: ' + str(e))
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))
