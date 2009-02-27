(use 'checkers.rules)
(use 'checkers.player)

(defn human-move
  []
  (println)
  (println (dump-board))
  (let [allowed (set (unwind-all (my-plays)))]
    (loop []
      (print "Your move -> ")
      (flush)
      (let [move (read)]
        (if (allowed move) move (recur))))))

(defn computer-move
  []
  (println)
  (println (dump-board))
  (print "My move -> ")
  (flush)
  (let [move (second (best-play))]
    (println move)
    move))

(loop [side +black+
       board [[  1  0  1  0  1  0  1  0  ]   ; 0
              [  0  1  0  1  0  1  0  1  ]   ; 1
              [  1  0  1  0  1  0  1  0  ]   ; 2
              [  0  0  0  0  0  0  0  0  ]   ; 3
              [  0  0  0  0  0  0  0  0  ]   ; 4
              [  0 -1  0 -1  0 -1  0 -1  ]   ; 5
              [ -1  0 -1  0 -1  0 -1  0  ]   ; 6
              [  0 -1  0 -1  0 -1  0 -1  ]]] ; 7 
              ;  0  1  2  3  4  5  6  7]

  (with-position [side board]
    (let [mover (if (black?) human-move computer-move)
          board (do-plays (mover))]
      (recur (- side) board))))
