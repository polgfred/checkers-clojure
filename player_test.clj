(use 'checkers)
(use 'player)

(defmacro assert*
  [& exprs]
  `(do ~@(for [expr exprs] `(assert ~expr))))

(defn- in?
  [v coll] (contains? (set coll) v))

(defn- not-in?
  [v coll] (not (in? v coll)))

(with-position
  [+black+
   [[  0  0  1  0  0  0  0  0  ]
    [  0 -1  0 -1  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0 -1  0 -1  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0  0  0 -1  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]]]
  (let [[score play] (best-play)]
    (assert (= play '([2 0] [4 2] [6 4] [4 6])))))

(with-position
  [+black+
   [[  0  0  1  0  0  0  0  0  ]
    [  0  0  0 -1  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]
    [  0  0  0  0  0  0  0  0  ]]]
  (let [[score play] (best-play)]
    (assert* (= score 99999)
             (= play '([2 0] [4 2])))))
