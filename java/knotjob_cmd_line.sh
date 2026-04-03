#! /bin/bash
#
# Example:
#
#    % knotjob_cmd_line.sh -sqe < test_knots.txt
#    Knot
#    Even Sq^1 Invariant : (2, 2, 2, 2)
#    [...]

java -jar KnotJob.jar "$@"
