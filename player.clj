(ns player)

(use 'checkers)

(def *search-depth* 3) ;; root binding controls default search tree depth

(defmacro switch-sides
  [& exprs]
  `(binding [*side* (- *side*) *search-depth* (dec *search-depth*)]
    ~@exprs))

(defn best-play ;; circular dependency
  [])

(let [piece-vals-black
        [[ 100   0 104   0 104   0 100   0 ]
         [   0 100   0 102   0 102   0 100 ]
         [ 102   0 105   0 105   0 102   0 ]
         [   0 105   0 110   0 110   0 105 ]
         [ 110   0 120   0 120   0 110   0 ]
         [   0 120   0 130   0 130   0 120 ]
         [ 130   0 142   0 142   0 130   0 ]
         [   0   0   0   0   0   0   0   0 ]]
      piece-vals-red
        (vec (reverse
          (for [r piece-vals-black]
            (vec (reverse (map - r))))))
      king-vals-black
        [[ 152   0 152   0 152   0 164   0 ]
         [   0 164   0 164   0 164   0 164 ]
         [ 152   0 180   0 180   0 164   0 ]
         [   0 164   0 180   0 180   0 152 ]
         [ 152   0 180   0 180   0 164   0 ]
         [   0 164   0 180   0 180   0 152 ]
         [ 164   0 164   0 164   0 164   0 ]
         [   0 164   0 152   0 152   0 152 ]]
      king-vals-red
        (vec (reverse
          (for [r king-vals-black]
            (vec (reverse (map - r))))))]

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

(defn minimum-score
  []
  (if (= *side* +black+) -99999 +99999))

;; (minimum-score)

(defn calculate-score-recursive
  []
  (if (or (pos? *search-depth*)
          (switch-sides (seq? (my-jumps))))
    (switch-sides (first (best-play)))
    (calculate-score)))

;; (calculate-score-recursive)

(defn compare-plays
  [p1 p2]
  (let [cmp (if (= *side* +black+) > <)
        score1 (first p1)
        score2 (first p2)]
    (if (cmp score1 score2) p1 p2)))

(defn best-play-from
  [play [from & more]]
  (if (empty? more)
    (let [play (reverse play) score (calculate-score-recursive)]
      ;; (println *search-depth* score play)
      [score play])
    (reduce compare-plays
      (for [tree more]
        (let [to (first tree)]
          (binding [*board* (do-play from to)]
            (best-play-from (cons to play) tree)))))))

;; (with-position [...] (best-play-from (list [4 2]) (jumps-from 4 2)))

(defn best-play
  []
  (let [plays (my-plays)]
    (if (empty? plays)
      [(minimum-score)]
      (reduce compare-plays
        (for [tree plays]
          (let [from (first tree)]
            (best-play-from (list from) tree)))))))

;; (with-position [...] (best-play))
