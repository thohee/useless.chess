# TODOs
* debug output for minmax search
    * number of computed possible successors
    * max reached depth
    * average reached depth
    * average branching factor
    * actually visited nodes
    * number of times of alpha/beta pruning
* evaluate victory with max value and defeat with min value
* skip siblings when max value has been found in max() and when min value has been found in min()
* coordinate as enum; lookup tables
* efficient data structure for actual board position: array of length 64 instead of hashmap, cached hash
* do not use complete BoardPosition as key for transposition table but only memory efficient array, the set of castling pieces, and a boolean indicating whether draw criteria repetitions or pawn moves have been reached
* do not differentiate w.r.t number of repetitions and pawn moves when looking up in transposition table, but only w.r.t. whether threshold for draw has been reached
* increase transposition table size
* prioritize/order possible moves
    * take priorities/ordering into account when ordering evaluated moves with same value of terminal position
    * maybe terminate search if worse value than for last sibling has been found during evaluation of ordered moves. Probably only for high enough search depth.
* heap analysis, save memory
* make king moves - except castlings - less probable
    * in particular, try to castle within first 10 moves, and hence do not move king or rook before doing so, unless absolutely necessary
* do not give chess to opponent using valuable piece, if it can be beaten right away -> unit test
* iterative deepening
    * discard least promising successors for next higher max depth? But only if min depth is high enough, so that an advantageous pawn sacrifice will be recognized.
* use flame graphs for profiling
* parallelization. With alpha-beta-pruning?
* use standard openings for first 3-4 plies
* follow the 7-10 opening rules
* do not foster specific pawn constellations, but only ensure covering of pawns
* more elaborate board position evaluation
* clever pruning, find good cut off conditions
* uci tournament mode
* use machine learning to prioritize and select moves
    * train model using publicly available game logs
* use machine learning to evaluate terminal position
* reinforcement learning
