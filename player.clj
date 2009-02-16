(load-file "checkers.clj")

(ns checkers)

(defn my-plays
  [] (or (my-jumps) (my-moves)))

;; (my-plays)

(defn do-play
  [from to]
  (let [diff (- (first to) (first from))]
    (if (or (= 2 diff) (= -2 diff))
      (do-jump from to) 
      (do-move from to))))

;; (do-play [0 2] [2 4])
;; (do-play [6 2] [5 3])

(def +max-depth+ 3)
(def *depth* +max-depth+)

(defmacro switch-sides
  [& exprs]
  `(binding [*side* (- *side*) *depth* (dec *depth*)] ~@exprs))

(defn best-play  ; circular dependency
  [])

(let [piece-vals-black
        (reverse-vector
          [[   0   0   0   0   0   0   0   0 ]
           [ 130   0 142   0 142   0 130   0 ]
           [   0 120   0 130   0 130   0 120 ]
           [ 110   0 120   0 120   0 110   0 ]
           [   0 105   0 110   0 110   0 105 ]
           [ 102   0 105   0 105   0 102   0 ]
           [   0 100   0 102   0 102   0 100 ]
           [ 100   0 104   0 104   0 100   0 ]])
      piece-vals-red
        (reverse-vector
          (for [r piece-vals-black]
            (reverse-vector (map - r))))
      king-vals-black (reverse-vector
          [[   0 164   0 152   0 152   0 152 ]
           [ 164   0 164   0 164   0 164   0 ]
           [   0 164   0 180   0 180   0 152 ]
           [ 152   0 180   0 180   0 164   0 ]
           [   0 164   0 180   0 180   0 152 ]
           [ 152   0 180   0 180   0 164   0 ]
           [   0 164   0 164   0 164   0 164 ]
           [ 152   0 152   0 152   0 164   0 ]])
      king-vals-red
        (reverse-vector
          (for [r king-vals-black]
            (reverse-vector (map - r))))]
  (defn calculate-score
    []
    (reduce +
      (for [[x y p] (squares)]
        (cond (= p  1) (get-p x y piece-vals-black)
              (= p -1) (get-p x y piece-vals-red)
              (= p  2) (get-p x y king-vals-black)
              (= p -2) (get-p x y king-vals-red)
              (= p  0) 0)))))

;; (calculate-score)

(defn calculate-score-recursive
  []
  (if (or (pos? *depth*) (switch-sides (seq? (my-jumps))))
    (switch-sides (first (best-play)))
    (calculate-score)))

;; (calculate-score-recursive)

(defn compare-plays
  [p1 p2]
  (let [score1 (first p1) score2 (first p2) cmp (if (= *side* +black+) > <)]
    (if (cmp score1 score2) p1 p2)))

(defn best-play-from
  [play [from & more]]
  (if (empty? more)
    (let [play (reverse play) score (calculate-score-recursive)]
      ;; (let [d (- +max-depth+ *depth*)]
      ;;   (println (indent d) d score play))
      [score play])
    (reduce compare-plays
      (for [tree more]
        (let [to (first tree)]
          (binding [*board* (do-play from to)]
            (best-play-from (cons to play) tree)))))))

;; (with-board [...] (best-play-from (list [4 2]) (jumps-from 4 2)))

(defn best-play
  []
  (reduce compare-plays
    (for [tree (my-plays)]
      (let [from (first tree)]
        (best-play-from (list from) tree)))))

;; (with-board [...] (best-play))

;; ---------------------------------------------------------

(with-board [[  0 -1  0 -1  0 -1  0 -1 ]  ; 7
             [ -1  0 -1  0 -1  0 -1  0 ]  ; 6
             [  0 -1  0 -1  0 -1  0 -1 ]  ; 5
             [  0  0  0  0  0  0  0  0 ]  ; 4
             [  0  0  0  0  0  0  0  0 ]  ; 3
             [  1  0  1  0  1  0  1  0 ]  ; 2
             [  0  1  0  1  0  1  0  1 ]  ; 1
             [  1  0  1  0  1  0  1  0 ]] ; 0
             ;  0  1  2  3  4  5  6  7
  (println (best-play)))

(with-board [[  0 -1  0 -1  0 -1  0 -1 ]  ; 7
             [  0  0 -1  0  0  0 -1  0 ]  ; 6
             [  0 -1  0 -1  0 -1  0 -1 ]  ; 5
             [  0  0  0  0  0  0  0  0 ]  ; 4
             [  0 -1  0 -1  0 -1  0  0 ]  ; 3
             [  1  0  1  0  1  0  1  0 ]  ; 2
             [  0  1  0  0  0  0  0  1 ]  ; 1
             [  1  0  1  0  1  0  1  0 ]] ; 0
             ;  0  1  2  3  4  5  6  7
 (println (best-play)))
