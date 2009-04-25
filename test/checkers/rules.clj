(ns test.checkers.rules
  (:use checkers.rules)
  (:use fact)
  (:use fact.utils.random)
  (:use fact.output.verbose))

(def b  [[  0  0  1  0  0  0  0  0  ]
         [  0 -1  0 -1  0  0  0  0  ]
         [  0  0  0  0  0  0  0  0  ]
         [  0  0  0 -1  0 -1  0  0  ]
         [  0  0  0  0  0  0  0  0  ]
         [  0  0  0  0  0 -1  0  0  ]
         [  0  0  0  0  0  0  0  0  ]
         [  0  0  0  0  0  0  0  0  ]])

(defn- random-coords
  []
  (random-seq #(random-int 0 (dec +size+))))

(defn- random-pieces
  []
  (random-seq #(random-int -2 2)))

(defn- in-groups-of
  [n coll]
  (partition n 1 coll))

(defn- in-unique-groups-of
  [n coll]
  (remove (fn [& values]
            (not= n (count (set values))))
    (in-groups-of n coll)))

(fact "get-p returns the correct piece value"
  [x (random-coords)
   y (random-coords)]
  (let [p ((b y) x)]
    (= p (get-p b x y))))

(fact "set-p correctly sets the piece value"
  [x (random-coords)
   y (random-coords)
   p (random-pieces)]
   (let [bb (set-p b x y p)]
     (= p (get-p bb x y))))

(fact "set-ps correctly sets multiple piece values"
  [[x1 x2 x3] (in-unique-groups-of 3 (random-coords))
   [y1 y2 y3] (in-unique-groups-of 3 (random-coords))
   [p1 p2 p3] (in-groups-of 3 (random-pieces))]
   (let [bb (set-ps b [x1 y1 p1 x2 y2 p2 x3 y3 p3])]
     (and (= p1 (get-p bb x1 y1))
          (= p2 (get-p bb x2 y2))
          (= p3 (get-p bb x3 y3)))))

(print-color-results (verify-facts))
