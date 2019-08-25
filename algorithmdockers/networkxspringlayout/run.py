#!/usr/bin/env python

import os
import sys
import argparse
import json
from contextlib import redirect_stdout
import ndex2
from ndex2.nice_cx_network import NiceCXNetwork
from ndex2.nice_cx_network import DefaultNetworkXFactory
import networkx as nx

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
                        help='Input CX file')
    parser.add_argument('--iterations', type=int, default=50,
                        help='Number of iterations default 50')
    parser.add_argument('--weight', default='weight',
                        help='Name of edge with weight, default weight')
    parser.add_argument('--scale', default=1, type=float,
                        help='Scale factor for positions default 1')
    parser.add_argument('--seed', type=int, default=None,
                        help='Random number seed, default none')
    parser.add_argument('--k', type=float, default=None,
                        help='Optimal distance between nodes. '
                             'If None the distance is set '
                             'to 1/sqrt(n) '
                             'where n is the number of '
                             'nodes. Increase this value '
                             'to move nodes farther apart. default none.')
    return parser.parse_args(args)


def cartesian(netx):
    """
    Converts node coordinates from a :py:class:`networkx.Graph` object
    to a list of dicts with following format:
    [{'node': <node id>,
      'x': <x position>,
      'y': <y position>}]
    :param G:
    :return: coordinates
    :rtype: list
    """
    return [{'node': n,
             'x': float(netx.pos[n][0]),
             'y': float(netx.pos[n][1])} for n in netx.pos]


def get_targetdegree_map(network):
    """
    builds edge map
    :param network:
    :return:
    """
    factor = 1000
    target_degree_map = {}
    for edgeid, edge in network.get_edges():
        if edge['s'] not in target_degree_map:
            target_degree_map[edge['s']] = factor
            continue
        target_degree_map[edge['s']] = target_degree_map[edge['s']] + factor
    return target_degree_map


def weight_edges_by_node_degree(network):
    """
    weights edges
    :param network:
    :return:
    """
    target_degree_map = get_targetdegree_map(network)

    for edgeid, edge in network.get_edges():
        if edge['s'] not in target_degree_map:
            weight = 1
        else:
            weight = target_degree_map[edge['s']]
        network.set_edge_attribute(edgeid, 'weight', weight, type='integer')


def apply_layout_to_network(network, theargs):
    """
    applies layout to network

    :param network:
    :return:
    """
    fac = DefaultNetworkXFactory()
    netx = fac.get_graph(network, None)
    num_nodes = len(network.get_nodes())
    netx.pos = nx.drawing.spring_layout(netx, scale=theargs.scale,
                                        k=theargs.k, iterations=theargs.iterations,
                                        weight=theargs.weight,
                                        seed=theargs.seed)
    network.set_opaque_aspect("cartesianLayout", cartesian(netx))


def run_springlayout(inputfile, theargs):
    """
    todo
    :param inputfile:
    :return:
    """
    net = ndex2.create_nice_cx_from_file(inputfile)

    weight_edges_by_node_degree(net)

    apply_layout_to_network(net, theargs)
    # the ndex2 python client versions 3.2 and earlier outputs debugging messages
    # to standard out and there is no way to disable that so we
    # are piping that output to standard error here
    with redirect_stdout(sys.stderr):
        data = json.dumps(net.to_cx())

    sys.stdout.write(data)
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
    Takes output from clustering algorithms in SOURCE,TARGET,INTERACTION; format
    and generates CX with a basic style

    """

    theargs = _parse_arguments(desc, args[1:])

    try:
        inputfile = os.path.abspath(theargs.input)

        return run_springlayout(inputfile, theargs)
    except Exception as e:
        sys.stderr.write('Caught exception: ' + str(e))
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))
