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
    [b]
    (reduce +
      (for [[x y p] (squares b)]
        (cond (= p  1) (get-p pb-vals x y)
              (= p -1) (get-p pr-vals x y)
              (= p  2) (get-p kb-vals x y)
              (= p -2) (get-p kr-vals x y)
              :else    0)))))

(defn calculate-score-recursive
  [b s]
  (if (or (pos? *search-depth*)
          (seq (my-jumps b (- s))))
    (with-next-level (first (best-play b (- s))))
    (calculate-score b)))

(def compare-plays-fn
  (memoize
    (fn [s]
      (if (= s +black+)
        (fn ([] [-99999])
            ([p1 p2] 
              (if (> (first p1) (first p2)) p1 p2)))
        (fn ([] [+99999])
            ([p1 p2]
              (if (< (first p1) (first p2)) p1 p2)))))))

(defn best-play-from
  [b s play [from & more]]
  (if (empty? more)
    (let [play  (reverse play)
          score (calculate-score-recursive b s)]
      [score play])
    (reduce (compare-plays-fn s)
      (for [tree more]
        (let [to (first tree)
              b  (do-play b s from to)]
          (best-play-from b s (cons to play) tree))))))

(defn best-play
  [b s]
  (let [plays (my-plays b s)]
    (reduce (compare-plays-fn s)
      (for [tree plays]
        (let [from (first tree)]
          (best-play-from b s (list from) tree))))))
