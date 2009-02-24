(ns checkers.player (:use checkers.rules))

(def *search-depth* 3) ;; root binding controls default search tree depth

(defmacro with-next-level
  [& exprs]
  `(binding [*search-depth* (dec *search-depth*)] ~@exprs))

(declare best-play)

(let [pb-vals
        [[ 100   0 104   0 104   0 100   0 ]
         [   0 100   0 102   0 102   0 100 ]
         [ 102   0 105   0 105   0 102   0 ]
         [   0 105   0 110   0 110   0 105 ]
         [ 110   0 120   0 120   0 110   0 ]
         [   0 120   0 130   0 130   0 120 ]
         [ 130   0 142   0 142   0 130   0 ]
         [   0   0   0   0   0   0   0   0 ]]
      pr-vals
        (vec (reverse (for [r pb-vals] (vec (reverse (map - r))))))
      kb-vals
        [[ 152   0 152   0 152   0 164   0 ]
         [   0 164   0 164   0 164   0 164 ]
         [ 152   0 180   0 180   0 164   0 ]
         [   0 164   0 180   0 180   0 152 ]
         [ 152   0 180   0 180   0 164   0 ]
         [   0 164   0 180   0 180   0 152 ]
         [ 164   0 164   0 164   0 164   0 ]
         [   0 164   0 152   0 152   0 152 ]]
      kr-vals
        (vec (reverse (for [r kb-vals] (vec (reverse (map - r))))))]

  (defn calculate-score
    []
    (reduce +
      (for [[x y p] (squares)]
        (cond (= p  1) (get-p x y pb-vals)
              (= p -1) (get-p x y pr-vals)
              (= p  2) (get-p x y kb-vals)
              (= p -2) (get-p x y kr-vals)
              (= p  0) 0)))))

;; (calculate-score)

(defn calculate-score-recursive
  []
  (if (or (pos? *search-depth*)
          (switch-sides (seq? (my-jumps))))
    (with-next-level (switch-sides (first (best-play))))
    (calculate-score)))

;; (calculate-score-recursive)

(defn compare-plays
  ([] [(if (black?) -99999 +99999)])
  ([p1 p2]
    (let [cmp (if (black?) > <)]
      (if (cmp (first p1) (first p2)) p1 p2))))

(defn best-play-from
  [play [from & more]]
  (if (empty? more)
    (let [play (reverse play) score (calculate-score-recursive)]
      ;; (println *search-depth* score play)
      [score play])
    (reduce compare-plays
      (for [tree more]
        (let [to (first tree)]
          (with-board [(do-play from to)]
            (best-play-from (cons to play) tree)))))))

;; (with-position [...] (best-play-from (list [4 2]) (jumps-from 4 2)))

(defn best-play
  []
  (let [plays (my-plays)]
    (reduce compare-plays
      (for [tree plays]
        (let [from (first tree)]
          (best-play-from (list from) tree))))))

;; (with-position [...] (best-play))
