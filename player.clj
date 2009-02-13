(load-file "checkers.clj")

(ns checkers)

(defn- indent
  [x] (apply str (map (fn [i] "  ") (range x))))

(defn- do-jump
  [[x y] [nx ny cp]]
  (let [mx (avg x nx) my (avg y ny) p (get-p x y)]
    (set-p* [x y 0] [mx my 0] [nx ny (promote nx ny p)])))

(defn- do-move
  [[x y] [nx ny]]
  (let [p (get-p x y)]
    (set-p* [x y 0] [nx ny (promote nx ny p)])))

(defn- do-play
  [from to]
  (let [[nx ny cp] to]
    (if cp (do-jump from to) (do-move from to))))

(defn my-plays
  [] (or (my-jumps) (my-moves)))

(def +max-depth+ 3)
(def *depth* +max-depth+)

(defmacro switch-sides
  [& exprs]
  `(binding [*side* (- *side*) *depth* (dec *depth*)] ~@exprs))

(defn run-plays  ; circular dependency
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
  (defn- calculate-score
    []
    (reduce +
      (for [[x y p] (squares)]
        (cond (= p  1) (get-p x y piece-vals-black)
              (= p -1) (get-p x y piece-vals-red)
              (= p  2) (get-p x y king-vals-black)
              (= p -2) (get-p x y king-vals-red)
              (= p  0) 0)))))

(defn- calculate-score-recursive
  []
  (if (or (pos? *depth*) (switch-sides (seq? (my-jumps))))
    (switch-sides (first (run-plays)))
    (calculate-score)))

(defn- best-play
  [plays]
  (let [cmp (if (= *side* +black+) > <)]
    (reduce
      (fn [play1 play2]
        (let [score1 (first play1) score2 (first play2)]
          (if (cmp score1 score2) play1 play2)))
      plays)))

(defn- run-plays-from
  [play [from & more]]
  (if more
    (best-play
      (for [subtree more]
        (let [to (first subtree)]
          (binding [*board* (do-play from to)]
            (run-plays-from (cons to play) subtree)))))
    (let [play (reverse play) score (calculate-score-recursive)]
      ; (let [d (- +max-depth+ *depth*)]
      ;   (println (indent d) d score play))
      [score play])))

(defn run-plays
  []
  (best-play
    (for [from (my-plays)]
      (let [[x y] (first from)]
        (run-plays-from (list [x y]) from)))))

;; (run-plays-from (list [4 2]) (jumps-from 4 2))

(println (run-plays))
