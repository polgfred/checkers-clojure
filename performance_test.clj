(use 'checkers)
(use 'player)

(with-position
  [+black+
   [[  1  0  1  0  1  0  1  0 ]
    [  0  1  0  1  0  1  0  1 ]
    [  1  0  1  0  1  0  1  0 ]
    [  0  0  0  0  0  0  0  0 ]
    [  0  0  0  0  0  0  0  0 ]
    [  0 -1  0 -1  0 -1  0 -1 ]
    [ -1  0 -1  0 -1  0 -1  0 ]
    [  0 -1  0 -1  0 -1  0 -1 ]]]
  (time (dotimes [_ 50000] (calculate-score)))
  (time (best-play)))
