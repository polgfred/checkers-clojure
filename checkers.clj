(ns checkers (:import (clojure.core)))

(def +size+ 8)
(def +red+ 1)
(def +white+ -1)

(def *side* +red+)

(defmacro reverse-vector
  [v] `(apply vector (reverse ~v)))

(def *board*
  (reverse-vector [
    [ 0 -1  0 -1  0 -1  0 -1 ]
    [ 0  0 -1  0  0  0 -1  0 ]
    [ 0 -1  0 -1  0 -1  0 -1 ]
    [ 0  0  0  0  0  0  0  0 ]
    [ 0 -1  0 -1  0 -1  0  0 ]
    [ 1  0  1  0  1  0  1  0 ]
    [ 0  1  0  0  0  0  0  1 ]
    [ 1  0  1  0  1  0  1  0 ]
  ]))

;(print *board*)

(defn get-p
  ([x y] (get-p x y *board*))
  ([x y board] (get (get board y) x)))

(defn set-p
  ([x y p] (set-p x y p *board*))
  ([x y p board] (assoc board y (assoc (get board y) x p))))

;(println (dump-board (set-p 0 0 2)))
;(println (dump-board (set-p 0 0 2 (set-p 2 0 -2))))

(defmacro set-p*
  [this & next]
  (if next `(set-p ~@this (set-p* ~@next)) `(set-p ~@this)))

;(println (dump-board))
;(println (dump-board (set-p* [2 2 0] [3 3 0] [4 4 1])))

(defn my-piece?
  [p] (= p *side*))

(defn my-king?
  [p] (= p (* 2 *side*)))

(defn mine?
  [p] (or (my-piece? p) (my-king? p)))

(defn opp?
  [p] (mine? (- p)))

(defn open?
  [p] (= p 0))

(defn playable?
  [x y] (= (rem (+ x y) 2) 0))

(defn crowned?
  [nx ny p]
  (or (and (= p 1) (= ny 7)) (and (= p -1) (= ny 0))))

(defn squares
  []
  (for [x (range 8) y (range 8) :when (playable? x y)]
    [x y (get-p x y)]))

;(squares)

(defn my-squares
  []
  (for [[x y p] (squares) :when (mine? p)] [x y p]))

;(my-squares)

(defn directions
  [p]
  (let [ahead-back [*side* (- *side*)] ahead [*side*]]
    (for [dy (if (my-king? p) ahead-back ahead) dx ahead-back]
      [dx dy])))

;(dirs 1)
;(dirs 2)

(defn- try-jump
  [x y p [dx dy]]
  (let [mx (+ x dx) my (+ y dy) cp (get-p mx my)]
    (let [nx (+ mx dx) ny (+ my dy) lp (get-p nx ny)]
      (if (and (< -1 nx +size+) (< -1 ny +size+) (opp? cp) (open? lp))
        (let [np (if (crowned? nx ny p) (* 2 p) p)
              board (set-p* [x y 0] [mx my 0] [nx ny np])]
          [mx my nx ny cp board])))))

;(try-jump 0 0 1 [1 1])
;(try-jump 2 2 1 [1 1])

(defn- collect-jumps
  [x y p]
  (loop [dirs (directions p) acc nil]
    (if (empty? dirs)
      (reverse acc)
      (let [[mx my nx ny cp board] (try-jump x y p (first dirs))]
        (if board
          (let [this [nx ny cp]
                next (binding [*board* board] (collect-jumps nx ny p))]
            (recur (rest dirs) (cons (cons this next) acc)))
          (recur (rest dirs) acc))))))

;(collect-jumps 2 2 1)

(defn jumps-from
  [x y]
  (let [next (collect-jumps x y (get-p x y))]
    (if next (cons [x y] next))))

;(jumps-from 2 2)
;(jumps-from 4 2)
;(jumps-from 0 6)

(defn- try-move
  [x y p [dx dy]]
  (let [nx (+ x dx) ny (+ y dy) lp (get-p nx ny)]
    (if (and (< -1 nx +size+) (< -1 ny +size+) (open? lp))
      (let [np (if (crowned? nx ny p) (* 2 p) p)
            board (set-p* [x y 0] [nx ny np])]
        [nx ny board]))))

(defn- collect-moves
  [x y p]
  (loop [dirs (directions p) acc nil]
    (if (empty? dirs)
      (reverse acc)
      (let [[nx ny board] (try-move x y p (first dirs))]
        (if board
          (recur (rest dirs) (cons (list [nx ny]) acc))
          (recur (rest dirs) acc))))))

;(collect-moves 4 0 1)

(defn moves-from
  [x y]
  (let [next (collect-moves x y (get-p x y))]
    (if next (cons [x y] next))))

;(moves-from 4 0)
