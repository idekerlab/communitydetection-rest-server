#!/usr/bin/env python

import os
import sys
import argparse
import json
from ndex2.nice_cx_network import NiceCXNetwork


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
    return parser.parse_args(args)


def get_nice_cx_network_from_clusterfile(inputfile):
    """

    :param inputfile:
    :return:
    """
    network = NiceCXNetwork()
    with open(inputfile, 'r') as f:
        data = f.read()

    for line in data.split(';'):
        slist = line.split(',')
        if len(slist) != 3:
            sys.stderr.write(line + ' does not have appropriate number of columns. skipping\n')
            continue
        if slist[2].startswith('c-c'):
            target_node_type = 'term'
        elif slist[2].startswith('c-m'):
            target_node_type = 'protein'

        source_node = network.get_node_by_name(slist[0])

        if source_node is None:
            print('creating node ' + slist[0])
            source_node_id = network.create_node(slist[0])
            network.add_node_attribute(property_of=source_node_id, name='suid', values=int(slist[0]), type='long')
            network.add_node_attribute(property_of=source_node_id, name='type', values='term', type='string')
        else:
            print('node ' + str(source_node) + ' already exists')
            source_node_id = source_node['@id']

        if target_node_type == 'term':
            target_node = network.get_node_by_name(slist[1])
            if target_node is None:
                target_node_id = network.create_node(slist[1])
                network.add_node_attribute(property_of=source_node_id, name='suid', values=int(slist[1]), type='int')
                network.add_node_attribute(property_of=source_node_id, name='type', values=target_node_type, type='string')
            else:
                target_node_id = target_node['@id']
            network.create_edge(edge_source=source_node_id, edge_target=target_node_id, edge_interaction='childof')
        else:
            # target node is a gene so lets add it to member list
            # will need to get existing list and append this entry
            gene_attrib = network.get_node_attribute(source_node_id, 'genes')
            if gene_attrib is None:
                genelist = ''
            else:
                genelist = gene_attrib['v'] + slist[1]
            network.set_node_attribute(source_node_id, 'genes', genelist, type='string')

    return network


def run_clustertocx(inputfile):
    """
    todo
    :param inputfile:
    :return:
    """
    net = get_nice_cx_network_from_clusterfile(inputfile)
    json.dump(net.to_cx, sys.stdout)
    return 0


def main(args):
    """
    Main entry point for program

    :param args: command line arguments usually :py:const:`sys.argv`
    :return: 0 for success otherwise failure
    :rtype: int
    """
    desc = """
    Runs louvain on command line, sending output to standard
    out 
    """

    theargs = _parse_arguments(desc, args[1:])

    try:
        inputfile = os.path.abspath(theargs.input)

        return run_clustertocx(inputfile)
    except Exception as e:
        sys.stderr.write('Caught exception: ' + str(e))
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))
