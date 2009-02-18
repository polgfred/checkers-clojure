(use 'checkers)
(use 'player)

(def board
  (ref [[  1  0  1  0  1  0  1  0 ]     ; 0
        [  0  1  0  1  0  1  0  1 ]     ; 1
        [  1  0  1  0  1  0  1  0 ]     ; 2
        [  0  0  0  0  0  0  0  0 ]     ; 3
        [  0  0  0  0  0  0  0  0 ]     ; 4
        [  0 -1  0 -1  0 -1  0 -1 ]     ; 5
        [ -1  0 -1  0 -1  0 -1  0 ]     ; 6
        [  0 -1  0 -1  0 -1  0 -1 ]] )) ; 7 
        ;  0  1  2  3  4  5  6  7

(def side (ref +black+))

(defn human-move
  []
  (println (dump-board))
  (print "Your move -> ")
  (flush)
  (read))

(defn computer-move
  []
  (println (dump-board))
  (print "My move -> ")
  (flush)
  (let [move (second (best-play))]
    (println move)
    move))

(defn do-plays
  [[from to & more]]
  (with-position @side (do-play from to)
    (if more
      (do-plays (cons to more))
      (get-position))))

(loop []
  (with-position @side @board
    (let [your-move (human-move) b (do-plays your-move)]
      (dosync (ref-set board b) (alter side -))
      (with-position @side @board
        (let [my-move (computer-move) b (do-plays my-move)]
          (dosync (ref-set board b) (alter side -))
          (recur))))))
