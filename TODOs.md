# TODOs
* do not give chess to opponent using valuable piece, if it can be beaten right away -> unit test
* make king moves - except castlings - less probable
    * in particular, try to castle within first 10 moves, and hence do not move king or rook before doing so, unless absolutely necessary
* use standard openings for first 3-4 plies or
* follow the 7-10 opening rules
* do not foster specific pawn constellations, but only ensure covering of pawns
* more elaborate board position evaluation
* clever pruning, find good cut off conditions
* debug output for minmax search
    * number of computed possible successors
    * max reached depth
    * average reached depth
    * average branching factor
    * actually visited nodes
    * number of times of alpha/beta pruning
* prioritize/order possible moves
    * take priorities/ordering into account when ordering evaluated moves with same value of terminal position
    * maybe terminate search if worse value than for last sibling has been found during evaluation of ordered moves. Probably only for high enough search depth.
* iterative deepening
    * discard least promising successors for next higher max depth? But only if min depth is high enough, so that an advantageous pawn sacrifice will be recognized.
* use flame graphs for profiling
* heap analysis, save memory
* parallelization. With alpha-beta-pruning?
* uci tournament mode
* use machine learning to prioritize and select moves
    * train model using publicly available game logs
* use machine learning to evaluate terminal position
* reinforcement learning
