(load-file "checkers.clj")

(ns checkers)

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
  (if (let [[nx ny cp] to] cp) (do-jump from to) (do-move from to)))

(defn my-plays
  [] (or (my-jumps) (my-moves)))

(def *depth* 5)

(defmacro switch-sides
  [& exprs]
  `(binding [*side* (- *side*) *depth* (dec *depth*)] ~@exprs))

(defn run-plays  ; circular dependency
  [])

(let [piece-vals-red
        (reverse-vector
          [[   0   0   0   0   0   0   0   0 ]
           [ 130   0 142   0 142   0 130   0 ]
           [   0 120   0 130   0 130   0 120 ]
           [ 110   0 120   0 120   0 110   0 ]
           [   0 105   0 110   0 110   0 105 ]
           [ 102   0 105   0 105   0 102   0 ]
           [   0 100   0 102   0 102   0 100 ]
           [ 100   0 104   0 104   0 100   0 ]])
      piece-vals-white
        (reverse-vector
          (for [r piece-vals-red]
            (reverse-vector (map - r))))
      king-vals-red (reverse-vector
          [[   0 164   0 152   0 152   0 152 ]
           [ 164   0 164   0 164   0 164   0 ]
           [   0 164   0 180   0 180   0 152 ]
           [ 152   0 180   0 180   0 164   0 ]
           [   0 164   0 180   0 180   0 152 ]
           [ 152   0 180   0 180   0 164   0 ]
           [   0 164   0 164   0 164   0 164 ]
           [ 152   0 152   0 152   0 164   0 ]])
      king-vals-white
        (reverse-vector
          (for [r king-vals-red]
            (reverse-vector (map - r))))]
  (defn- calculate-score
    []
    (if (or (pos? *depth*) (switch-sides (seq (my-jumps))))
      (switch-sides (first (run-plays)))
      (apply +
        (for [[x y p] (squares)]
          (cond (= p  1) (get-p x y piece-vals-red)
                (= p -1) (get-p x y piece-vals-white)
                (= p  2) (get-p x y king-vals-red)
                (= p -2) (get-p x y king-vals-white)
                (= p  0) 0))))))

(defn- best-play
  [plays]
  (let [cmp (if (= *side* +red+) > <)]
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
    (let [play (reverse play)]
      [(calculate-score) play])))

(defn run-plays
  []
  (best-play
    (for [from (my-plays)]
      (let [[x y] (first from)]
        (run-plays-from (list [x y]) from)))))

;; (run-plays-from (list [4 2]) (jumps-from 4 2))

(run-plays)

;; (defn best-play
;;   []
;;   (loop [squares (my-squares) best nil score nil]
;;     (if (empty? squares)
;;       (reverse acc)
;;       (let [[x y p] (first squares)]
;;         (for [[board score] (run-plays-from x y)]
;;           (run-play))
