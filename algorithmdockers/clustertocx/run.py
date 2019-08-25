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
                        help='Edge file in SOURCE,TARGET,INTERACTION; format')
    parser.add_argument('--directed', action='store_true',
                        help='If set, then generate directed graph')
    parser.add_argument('--style', help='Style file to use', default='/style.cx')
    return parser.parse_args(args)


def get_nice_cx_network_from_clusterfile(inputfile):
    """

    :param inputfile:
    :return:
    """
    network = NiceCXNetwork()
    network.set_name('clustertocx from ' + str(os.path.basename(os.path.dirname(inputfile))))
    with open(inputfile, 'r') as f:
        data = f.read()

    node_map = {}
    protein_map = {}
    for line in data.split(';'):
        slist = line.split(',')
        if len(slist) != 3:
            sys.stderr.write(line + ' does not have appropriate number of columns. skipping\n')
            continue

        if slist[2].startswith('c-c'):
            target_is_protein = False
        else:
            target_is_protein = True

        if slist[0] not in node_map:
            source_node_id = network.create_node(slist[0])
            node_map[slist[0]] = source_node_id
            network.add_node_attribute(property_of=source_node_id, name='suid', values=int(slist[0]), type='long')
            network.add_node_attribute(property_of=source_node_id, name='type', values='term', type='string')
        else:
            source_node_id = node_map[slist[0]]

        if target_is_protein:
            if slist[0] not in protein_map:
                protein_map[slist[0]] = set()
                protein_map[slist[0]].add(slist[1])
            else:
                if slist[1] not in protein_map[slist[0]]:
                    protein_map[slist[0]].add(slist[1])
        else:
            target_node_id = network.create_node(slist[1])
            network.create_edge(source_node_id, target_node_id, 'Child-Parent')
            network.add_node_attribute(property_of=target_node_id, name='suid', values=int(slist[1]), type='long')
            network.add_node_attribute(property_of=target_node_id, name='type', values='term', type='string')
            node_map[slist[1]] = target_node_id

    for nodename in protein_map:
        genelist = protein_map[nodename]
        if len(genelist) > 0:
            genesymbol_list = []
            for entry in genelist:
                genesymbol_list.append(entry)
            network.add_node_attribute(property_of=node_map[nodename], name='member', values=genesymbol_list,
                                       type='list_of_string')
            network.add_node_attribute(property_of=node_map[nodename], name='type', values='complex', type='string',
                                       overwrite=True)
    del node_map
    del protein_map

    return network


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


def apply_layout_to_network(network):
    """
    applies layout to network

    :param network:
    :return:
    """
    fac = DefaultNetworkXFactory()
    netx = fac.get_graph(network, None)
    num_nodes = len(network.get_nodes())
    netx.pos = nx.drawing.spring_layout(netx, scale=num_nodes,
                                        k=1.8, iterations=50)

    network.set_opaque_aspect("cartesianLayout", cartesian(netx))


def run_clustertocx(inputfile, style):
    """
    todo
    :param inputfile:
    :return:
    """
    net = get_nice_cx_network_from_clusterfile(inputfile)

    # apply_layout_to_network(net)

    if style is not None:
        net.apply_style_from_network(ndex2.create_nice_cx_from_file(style))
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
        style = os.path.abspath(theargs.style)

        return run_clustertocx(inputfile, style)
    except Exception as e:
        sys.stderr.write('Caught exception: ' + str(e))
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))
