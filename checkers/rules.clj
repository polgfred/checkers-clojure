(ns checkers.rules)

(defn avg
  [& values] (/ (reduce + values) (count values)))

(def +size+   8)
(def +black+  1)
(def +red+   -1)

(defstruct Game :side :board)

; (def *board*)     ;; set root bindings to manage
; (def *side*)      ;; a single game globally
; 
; (defmacro with-board
;   [[board] & exprs]
;   `(binding [*board* ~board] ~@exprs))
; 
; (defmacro with-position
;   [[side board] & exprs]
;   `(binding [*side* ~side *board* ~board] ~@exprs))
; 
; (defmacro switch-sides
;   [& exprs]
;   `(binding [*side* (- *side*)] ~@exprs))

(let [chr-s {0 ". " 1 "b " 2 "B " -1 "r " -2 "R "}
      num-s (partial format "%d ")]
  (defn dump-board
    [board]
      (reduce str
        (concat
          (for [y (reverse (range +size+))]
            (str (num-s y) (reduce str (map chr-s (get board y))) "\n"))
          (list (str "  " (reduce str (map num-s (range +size+))) "\n"))))))

(defn get-p
  [board x y] (get (get board y) x))

(defn set-p
  [board x y p] (assoc board y (assoc (get board y) x p)))

(defmacro set-p*
  [board this & more]
  (if more
    `(set-p (set-p* ~board ~@more) ~@this)
    `(set-p ~board ~@this)))

(defn my-piece?
  [side p] (= p side))

(defn my-king?
  [side p] (= p (* 2 side)))

(defn mine?
  [side p] (or (my-piece? side p) (my-king? side p)))

(defn opp?
  [side p] (mine? side (- p)))

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
  [board]
  (for [x (range 8)
        y (range 8)
        :when (playable? x y)]
    [x y (get-p board x y)]))

(defn my-squares
  [board side]
  (for [[x y p :as sq] (squares board)
        :when (mine? side p)]
    sq))

(defn directions
  [side p]
  (let [ahead-back [side (- side)] ahead [side]]
    (for [dy (if (my-king? side p) ahead-back ahead)
          dx ahead-back]
      [dx dy])))

(defn do-jump
  [board side [x y] [nx ny]]
  (if (and (< -1  x +size+)
           (< -1  y +size+)
           (< -1 nx +size+)
           (< -1 ny +size+)
           (=  2 (Math/abs (- nx x)))
           (=  2 (Math/abs (- ny y))))
    (let [p (get-p board x y)
          mx (avg x nx)
          my (avg y ny)]
      (if (and (opp? side (get-p board mx my))
               (open? (get-p board nx ny)))
        (set-p* board [x y 0] [mx my 0] [nx ny (promote nx ny p)])))))

(defn try-jump
  [board side x y [dx dy]]
  (let [nx (+ x dx dx)
        ny (+ y dy dy)
        board (do-jump board side [x y] [nx ny])]
    (if board [nx ny board])))

(defn collect-jumps
  [board side x y p]
  (remove nil?
    (for [dir (directions side p)]
      (let [[nx ny board] (try-jump board side x y dir)]
        (if board
          (let [more (seq (collect-jumps board side nx ny p))]
            (cons [nx ny] more)))))))

(defn jumps-from
  [board side x y]
  (if-let [more (seq (collect-jumps board side x y (get-p board x y)))]
    (cons [x y] more)))

(defn my-jumps
  [board side]
  (remove nil?
    (for [[x y] (my-squares board side)]
      (jumps-from board side x y))))

(defn do-move
  [board side [x y] [nx ny]]
  (if (and (< -1  x +size+)
           (< -1  y +size+)
           (< -1 nx +size+)
           (< -1 ny +size+)
           (=  1 (Math/abs (- nx x)))
           (=  1 (Math/abs (- ny y))))
    (let [p (get-p board x y)]
      (if (open? (get-p board nx ny))
        (set-p* board [x y 0] [nx ny (promote nx ny p)])))))

(defn try-move
  [board side x y [dx dy]]
  (let [nx (+ x dx)
        ny (+ y dy)
        board (do-move board side [x y] [nx ny])]
    (if board [nx ny board])))

(defn collect-moves
  [board side x y p]
  (remove nil?
    (for [dir (directions side p)]
      (let [[nx ny board] (try-move board side x y dir)]
        (if board (list [nx ny]))))))

(defn moves-from
  [board side x y]
  (if-let [more (seq (collect-moves board side x y (get-p board x y)))]
    (cons [x y] more)))

(defn my-moves
  [board side]
  (remove nil?
    (for [[x y] (my-squares board side)]
      (moves-from board side x y))))

(defn do-play
  [board side [x y :as from] [nx ny :as to]]
  (let [diff (Math/abs (- nx x))]
    (if (= 2 diff)
      (do-jump board side from to)
      (do-move board side from to))))

(defn do-plays
  [board side [from & [to :as more]]]
  (if more
    (let [board (do-play board side from to)]
      (do-plays board side more))
    board))

(defn my-plays
  [board side]
  (let [jumps (my-jumps board side)]
    (if (seq jumps) jumps (my-moves board side))))

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
