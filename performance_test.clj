(use 'checkers)
(use 'player)

(with-position +black+
  [[  1  0  1  0  1  0  1  0 ]  ; 0
   [  0  1  0  1  0  1  0  1 ]  ; 1
   [  1  0  1  0  1  0  1  0 ]  ; 2
   [  0  0  0  0  0  0  0  0 ]  ; 3
   [  0  0  0  0  0  0  0  0 ]  ; 4
   [  0 -1  0 -1  0 -1  0 -1 ]  ; 5
   [ -1  0 -1  0 -1  0 -1  0 ]  ; 6
   [  0 -1  0 -1  0 -1  0 -1 ]] ; 7 
   ;  0  1  2  3  4  5  6  7
  
  (time (dotimes [_ 50000] (calculate-score)))
  (time (best-play)))
