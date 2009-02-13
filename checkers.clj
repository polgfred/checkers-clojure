(ns checkers (:import (clojure.core)))

(def +size+ 8)
(def +black+ 1)
(def +red+ -1)

(def *side* +black+)
(def *board*)

(defn abs
  [n] (if (neg? n) (- n) n))

(defn avg
  [& values] (/ (reduce + values) (count values)))

(defn compact
  [coll]
  (filter #(not (nil? %)) coll))

(defn- reverse-vector
  [v] (apply vector (reverse v)))

(defmacro with-board
  [v & exprs] `(binding [*board* (reverse-vector ~v)] ~@exprs))

(let [char-map {0 \. 1 \b 2 \B -1 \r -2 \R}]
  (defn- dump-board
    ([] (dump-board *board*))
    ([board]
      (reduce str
        (concat
          (for [y (reverse (range 8))]
            (let [s (map (partial get char-map) (get board y))]
              (apply (partial format "%d %s %s %s %s %s %s %s %s\n" y) s)))
          '("  0 1 2 3 4 5 6 7\n"))))))

;; (println (dump-board))

(defn get-p
  ([x y] (get-p x y *board*))
  ([x y board] (get (get board y) x)))

(defn set-p
  ([x y p] (set-p x y p *board*))
  ([x y p board] (assoc board y (assoc (get board y) x p))))

;; (println (dump-board (set-p 0 0 2)))
;; (println (dump-board (set-p 0 0 2 (set-p 2 0 -2))))

(defmacro set-p*
  [this & more]
  (if more `(set-p ~@this (set-p* ~@more)) `(set-p ~@this)))

;; (println (dump-board))
;; (println (dump-board (set-p* [2 2 0] [3 3 0] [4 4 1])))

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

(defn promoted?
  [nx ny p] (or (and (= p 1) (= ny 7)) (and (= p -1) (= ny 0))))

(defn promote
  [nx ny p] (if (promoted? nx ny p) (* 2 p) p))

(defn squares
  []
  (for [x (range 8) y (range 8) :when (playable? x y)]
    [x y (get-p x y)]))

;; (squares)

(defn my-squares
  []
  (for [[x y p] (squares) :when (mine? p)] [x y p]))

;; (my-squares)

(defn directions
  [p]
  (let [ahead-back [*side* (- *side*)] ahead [*side*]]
    (for [dy (if (my-king? p) ahead-back ahead) dx ahead-back]
      [dx dy])))

;; (dirs 1)
;; (dirs 2)

(defn- try-jump
  [x y p [dx dy]]
  (let [mx (+  x dx) my (+  y dy) cp (get-p mx my)
        nx (+ mx dx) ny (+ my dy) lp (get-p nx ny)]
    (if (and (< -1 nx +size+)
             (< -1 ny +size+)
             (opp?  cp)
             (open? lp))
      (let [board (set-p* [x y 0] [mx my 0] [nx ny (promote nx ny p)])]
        [mx my nx ny cp board]))))

;; (try-jump 0 0 1 [1 1])
;; (try-jump 2 2 1 [1 1])

(defn- collect-jumps
  [x y p]
  (loop [dirs (directions p) acc nil]
    (if (empty? dirs)
      (reverse acc)
      (let [[mx my nx ny cp board] (try-jump x y p (first dirs))]
        (if board
          (let [this [nx ny cp]
                more (binding [*board* board] (collect-jumps nx ny p))]
            (recur (rest dirs) (cons (cons this more) acc)))
          (recur (rest dirs) acc))))))

;; (collect-jumps 2 2 1)

(defn jumps-from
  [x y]
  (let [more (collect-jumps x y (get-p x y))]
    (if more (cons [x y] more))))

;; (jumps-from 2 2)
;; (jumps-from 4 2)
;; (jumps-from 0 6)

(defn my-jumps
  []
  (compact (for [[x y] (my-squares)] (jumps-from x y))))

;; (my-jumps)

(defn- try-move
  [x y p [dx dy]]
  (let [nx (+ x dx) ny (+ y dy) lp (get-p nx ny)]
    (if (and (< -1 nx +size+)
             (< -1 ny +size+)
             (open? lp))
      (let [board (set-p* [x y 0] [nx ny (promote nx ny p)])]
        [nx ny board]))))

(defn- collect-moves
  [x y p]
  (loop [dirs (directions p) acc nil]
    (if (empty? dirs)
      (reverse acc)
      (let [[nx ny board] (try-move x y p (first dirs))]
        (recur (rest dirs) (if board (cons (list [nx ny]) acc) acc))))))

;; (collect-moves 4 0 1)

(defn moves-from
  [x y]
  (let [next (collect-moves x y (get-p x y))]
    (if next (cons [x y] next))))

;; (moves-from 4 0)

(defn my-moves
  []
  (compact (for [[x y] (my-squares)] (moves-from x y))))

;; (my-moves)
