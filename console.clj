(use 'checkers)
(use 'player)

(defn- unwind
  [[this & more :as tree]]
  (if more
    (reduce concat
      (for [m more] (map #(cons this %) (unwind m))))
    (list tree)))

(defn human-move
  []
  (println)
  (println (dump-board))
  (let [allowed (set (reduce concat (map unwind (my-plays))))]
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

(defn do-plays
  [[from to & more]]
  (with-board (do-play from to)
    (if more
      (do-plays (cons to more))
      *board*)))

(loop [side +black+
       board [[  1  0  1  0  1  0  1  0 ]   ; 0
              [  0  1  0  1  0  1  0  1 ]   ; 1
              [  1  0  1  0  1  0  1  0 ]   ; 2
              [  0  0  0  0  0  0  0  0 ]   ; 3
              [  0  0  0  0  0  0  0  0 ]   ; 4
              [  0 -1  0 -1  0 -1  0 -1 ]   ; 5
              [ -1  0 -1  0 -1  0 -1  0 ]   ; 6
              [  0 -1  0 -1  0 -1  0 -1 ]]] ; 7 
              ;  0  1  2  3  4  5  6  7]

  (with-position side board
    (let [mover (if (black?) human-move computer-move)
          board (do-plays (mover))]
      (recur (- side) board))))
