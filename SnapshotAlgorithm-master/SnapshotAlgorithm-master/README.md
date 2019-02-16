# SnapshotAlgorithm

1) Uses Google’s protocol buffers to implement Chandy-Lamport snapshot algorithm.
2) This system implements a distributed bank application which manages many bank branches. Once initiated, the transactions between branches starts along with the marker messages which handles the channel state at each branch. A snapshot of the distributed bank can be retrieved at any time and state of each branch can be analysed.
3) Uses Java and Google’s protocol buffers.

Compiling:
type "make java"

Executing:
1)start branch using "./branch branch_name port_number

2)start controller using "./controller total_amount file.txt
