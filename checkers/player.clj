(ns checkers.player (:use checkers.rules))

(def *search-depth* 3)

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

(declare best-play)

(defn calculate-score-recursive
  [b s v]
  (if (or (pos? v) (seq (my-jumps b (- s))))
    (first (best-play b (- s) (dec v)))
    (calculate-score b)))

(def compare-plays-fn
  (memoize
    (fn [s]
      (if (= s +black+)
        (fn ([] [-99999])
            ([& plays]
              (first (sort-by first > plays))))
        (fn ([] [+99999])
            ([& plays]
              (first (sort-by first < plays))))))))

(defn best-play-from
  [b s v play [[x y] & more]]
  (if (empty? more)
    (let [play (reverse play)
          score (calculate-score-recursive b s v)]
      [score play])
    (reduce (compare-plays-fn s)
      (for [tree more]
        (let [[nx ny :as nxy] (first tree)
              b (do-play b s x y nx ny)]
          (best-play-from b s v (cons nxy play) tree))))))

(defn best-play
  ([b s] (best-play b s *search-depth*))
  ([b s v]
    (let [plays (my-plays b s)]
      (reduce (compare-plays-fn s)
        (for [tree plays]
          (let [[x y :as xy] (first tree)]
            (best-play-from b s v (list xy) tree)))))))
