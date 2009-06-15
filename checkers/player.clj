(ns checkers.player (:use checkers.rules))

(def *search-depth* 3) ; initial depth for tree search

(let [pb-vals ; black pieces
        [[ 100   0 104   0 104   0 100   0 ]
         [   0 100   0 102   0 102   0 100 ]
         [ 102   0 105   0 105   0 102   0 ]
         [   0 105   0 110   0 110   0 105 ]
         [ 110   0 120   0 120   0 110   0 ]
         [   0 120   0 130   0 130   0 120 ]
         [ 130   0 142   0 142   0 130   0 ]
         [   0   0   0   0   0   0   0   0 ]]
      pr-vals ; red pieces
        (vec (reverse (for [r pb-vals] (vec (reverse (map - r))))))
      kb-vals ; black kings
        [[ 152   0 152   0 152   0 164   0 ]
         [   0 164   0 164   0 164   0 164 ]
         [ 152   0 180   0 180   0 164   0 ]
         [   0 164   0 180   0 180   0 152 ]
         [ 152   0 180   0 180   0 164   0 ]
         [   0 164   0 180   0 180   0 152 ]
         [ 164   0 164   0 164   0 164   0 ]
         [   0 164   0 152   0 152   0 152 ]]
      kr-vals ; red kings
        (vec (reverse (for [r kb-vals] (vec (reverse (map - r))))))]

  (defn calculate-score
    "Given board `b', the sum of the scores of each piece's position in the above tables.

    The red tables are defined to be the negated mirror image of the black tables."
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
  "Given board `b' and side `s', find the score of the current position.  If either the
  maximum search depth has not been reached, or there are counter-jumps from this position,
  return the score of the opposing side's best play; otherwise, calculate the score from
  the value tables."
  [b s v]
  (if (or (pos? v) (seq (my-jumps b (- s))))
    (first (best-play b (- s) (dec v)))
    (calculate-score b)))

(def #^{
  :doc
  "Given side `s', create a reducer that finds the best play from a list of plays.

  This function is memoized for performance."
  :arglists '([s])}

  compare-plays-fn
  (letfn [(compare-plays-fn
            [s]
            (if (= s +black+)
              (fn ([] [-99999])
                  ([& plays]
                    (first (sort-by first > plays))))
              (fn ([] [+99999])
                  ([& plays]
                    (first (sort-by first < plays))))))]
    (memoize compare-plays-fn)))

(defn best-play-from
  "Given board `b', the best play for side `s' from (x,y).  If `more' is non-empty,
  then (x,y) is not a terminal position, so `best-play-from' must be invoked
  recursively, and the best play selected from among its children.  The entire play
  is accumulated into the value `play' as [x1 y1 x2 y2 ...]."
  [b s v play [x y & more]]
  (if (empty? more)
    (let [play (vec (reverse play))
          score (calculate-score-recursive b s v)]
      [score play])
    (reduce (compare-plays-fn s)
      (for [tree more]
        (let [[nx ny] tree
              b (do-play b s x y nx ny)]
          (best-play-from b s v (conj play nx ny) tree))))))

(defn best-play
  "Given board `b', the `best' play for side `s' as [score (x1 y1 x2 y2 ...)]."
  ([b s] (best-play b s *search-depth*))
  ([b s v]
    (let [plays (my-plays b s)]
      (reduce (compare-plays-fn s)
        (for [tree plays]
          (let [[x y] tree]
            (best-play-from b s v (conj () x y) tree)))))))
