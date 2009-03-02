(ns checkers.rules)

(defn avg
  [& values] (/ (reduce + values) (count values)))

(def +size+   8)
(def +black+  1)
(def +red+   -1)

(def *board*)     ;; set root bindings to manage
(def *side*)      ;; a single game globally

(defmacro with-board
  [[board] & exprs]
  `(binding [*board* ~board] ~@exprs))

(defmacro with-position
  [[side board] & exprs]
  `(binding [*side* ~side *board* ~board] ~@exprs))

(defmacro switch-sides
  [& exprs]
  `(binding [*side* (- *side*)] ~@exprs))

(let [chr-s {0 ". " 1 "b " 2 "B " -1 "r " -2 "R "}
      num-s (partial format "%d ")]
  (defn dump-board
    ([] (dump-board *board*))
    ([board]
      (reduce str
        (concat
          (for [y (reverse (range +size+))]
            (str (num-s y) (reduce str (map chr-s (get board y))) "\n"))
          (list (str "  " (reduce str (map num-s (range +size+))) "\n")))))))

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
  (if more
    `(set-p ~@this (set-p* ~@more))
    `(set-p ~@this)))

;; (println (dump-board))
;; (println (dump-board (set-p* [2 2 0] [3 3 0] [4 4 1])))

(defn black?
  [] (= *side* +black+))

(defn red?
  [] (= *side* +red+))

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
  [x y]
  (= (rem (+ x y) 2) 0))

(defn promoted?
  [nx ny p]
  (or (and (= p  1) (= ny 7))
      (and (= p -1) (= ny 0))))

(defn promote
  [nx ny p]
  (if (promoted? nx ny p) (* 2 p) p))

(defn squares
  []
  (for [x (range 8)
        y (range 8)
        :when (playable? x y)]
    [x y (get-p x y)]))

;; (squares)

(defn my-squares
  []
  (for [[x y p :as sq] (squares) :when (mine? p)]
    sq))

;; (my-squares)

(defn directions
  [p]
  (let [ahead-back [*side* (- *side*)] ahead [*side*]]
    (for [dy (if (my-king? p) ahead-back ahead)
          dx ahead-back]
      [dx dy])))

;; (dirs 1)
;; (dirs 2)

(defn do-jump
  [[x y] [nx ny]]
  (if (and (< -1  x +size+)
           (< -1  y +size+)
           (< -1 nx +size+)
           (< -1 ny +size+)
           (=  2 (Math/abs (- nx x)))
           (=  2 (Math/abs (- ny y))))
    (let [p (get-p x y)
          mx (avg x nx)
          my (avg y ny)]
      (if (and (opp? (get-p mx my))
               (open? (get-p nx ny)))
        (set-p* [x y 0] [mx my 0] [nx ny (promote nx ny p)])))))

;; (do-jump [0 2] [2 4])

(defn try-jump
  [x y [dx dy]]
  (let [nx (+ x dx dx)
        ny (+ y dy dy)
        board (do-jump [x y] [nx ny])]
    (if board [nx ny board])))

;; (try-jump 0 0 [1 1])
;; (try-jump 2 2 [1 1])

(defn collect-jumps
  [x y p]
  (remove nil?
    (for [dir (directions p)]
      (let [[nx ny board] (try-jump x y dir)]
        (if board
          (let [more (binding [*board* board]
                       (seq (collect-jumps nx ny p)))]
            (cons [nx ny] more)))))))

;; (collect-jumps 2 2 1)

(defn jumps-from
  [x y]
  (if-let [more (seq (collect-jumps x y (get-p x y)))]
    (cons [x y] more)))

;; (jumps-from 2 2)
;; (jumps-from 4 2)
;; (jumps-from 0 6)

(defn my-jumps
  []
  (remove nil?
    (for [[x y] (my-squares)]
      (jumps-from x y))))

;; (my-jumps)

(defn do-move
  [[x y] [nx ny]]
  (if (and (< -1  x +size+)
           (< -1  y +size+)
           (< -1 nx +size+)
           (< -1 ny +size+)
           (=  1 (Math/abs (- nx x)))
           (=  1 (Math/abs (- ny y))))
    (let [p (get-p x y)]
      (if (open? (get-p nx ny))
        (set-p* [x y 0] [nx ny (promote nx ny p)])))))

;; (do-move [4 0] [3 1])

(defn try-move
  [x y [dx dy]]
  (let [nx (+ x dx)
        ny (+ y dy)
        board (do-move [x y] [nx ny])]
    (if board [nx ny board])))

;; (try-move 4 0 [-1 1])

(defn collect-moves
  [x y p]
  (remove nil?
    (for [dir (directions p)]
      (let [[nx ny board] (try-move x y dir)]
        (if board (list [nx ny]))))))

;; (collect-moves 4 0 1)

(defn moves-from
  [x y]
  (if-let [more (seq (collect-moves x y (get-p x y)))]
    (cons [x y] more)))

;; (moves-from 4 0)

(defn my-moves
  []
  (remove nil?
    (for [[x y] (my-squares)]
      (moves-from x y))))

;; (my-moves)

(defn do-play
  [[x y :as from] [nx ny :as to]]
  (let [diff (Math/abs (- nx x))]
    (if (= 2 diff) (do-jump from to)
                   (do-move from to))))

;; (do-play [0 2] [2 4])
;; (do-play [6 2] [5 3])

(defn do-plays
  [from & [to & _ :as more]]
  (if more (binding [*board* (do-play from to)]
             (apply do-plays more)
           *board*)))

;; (do-plays ([0 2] [2 4] [4 6]))

(defn my-plays
  []
  (let [jumps (my-jumps)]
    (if (seq jumps) jumps (my-moves))))

;; (my-plays)

(defn unwind-plays
  [[this & more :as tree]]
  (if more
    (reduce concat
      (for [m more]
        (map #(cons this %) (unwind-plays m))))
    (list tree)))

(defn unwind-all
  [plays]
  (reduce concat (map unwind-plays plays)))
