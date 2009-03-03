(use 'checkers.rules)
(use 'checkers.player)

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
  (time (best-play)))
