(use 'checkers)
(use 'player)

(defmacro assert*
  [& exprs]
  `(do ~@(for [expr exprs] `(assert ~expr))))

(defn- in?
  [v coll] (some (partial = v) coll))

(defn- not-in?
  [v coll] (not (in? v coll)))

(with-position +black+
  [[  0  0  1  0  0  0  0  0  ]  ; 0
   [  0 -1  0 -1  0  0  0  0  ]  ; 1
   [  0  0  0  0  0  0  0  0  ]  ; 2
   [  0  0  0 -1  0 -1  0  0  ]  ; 3
   [  0  0  0  0  0  0  0  0  ]  ; 4
   [  0  0  0  0  0 -1  0  0  ]  ; 5
   [  0  0  0  0  0  0  0  0  ]  ; 6
   [  0  0  0  0  0  0  0  0  ]] ; 7
   ;  0  1  2  3  4  5  6  7

  (let [[score play] (best-play)]
    (assert (= play '([2 0] [4 2] [6 4] [4 6])))))

(with-position +black+
  [[  0  0  1  0  0  0  0  0  ]  ; 0
   [  0  0  0 -1  0  0  0  0  ]  ; 1
   [  0  0  0  0  0  0  0  0  ]  ; 2
   [  0  0  0  0  0  0  0  0  ]  ; 3
   [  0  0  0  0  0  0  0  0  ]  ; 4
   [  0  0  0  0  0  0  0  0  ]  ; 5
   [  0  0  0  0  0  0  0  0  ]  ; 6
   [  0  0  0  0  0  0  0  0  ]] ; 7
   ;  0  1  2  3  4  5  6  7

  (let [[score play] (best-play)]
    (assert* (= score 99999)
             (= play '([2 0] [4 2])))))
