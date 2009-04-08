(ns checkers.rules)

(defn avg
  [& values] (/ (reduce + values) (count values)))

(def +size+   8)
(def +black+  1)
(def +red+   -1)

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
  [b x y] (get (get b y) x))

(defn set-p
  [b x y p] (assoc b y (assoc (get b y) x p)))

(defmacro set-p*
  [b this & more]
  (if more
    `(set-p (set-p* ~b ~@more) ~@this)
    `(set-p ~b ~@this)))

(defn my-piece?
  [s p] (= p s))

(defn my-king?
  [s p] (= p (* 2 s)))

(defn mine?
  [s p] (or (my-piece? s p) (my-king? s p)))

(defn opp?
  [s p] (mine? s (- p)))

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
  [b]
  (for [x (range 8)
        y (range 8)
        :when (playable? x y)]
    [x y (get-p b x y)]))

(defn my-squares
  [b s]
  (for [[x y p :as xyp] (squares b)
        :when (mine? s p)]
    xyp))

(defn directions
  [s p]
  (let [ahead-back [s (- s)] ahead [s]]
    (for [dy (if (my-king? s p) ahead-back ahead)
          dx ahead-back]
      [dx dy])))

(defn do-jump
  [b s x y nx ny]
  (if (and (< -1  x +size+)
           (< -1  y +size+)
           (< -1 nx +size+)
           (< -1 ny +size+)
           (=  2 (Math/abs (- nx x)))
           (=  2 (Math/abs (- ny y))))
    (let [p (get-p b x y)
          mx (avg x nx)
          my (avg y ny)]
      (if (and (opp? s (get-p b mx my))
               (open? (get-p b nx ny)))
        (set-p* b [x y 0] [mx my 0] [nx ny (promote nx ny p)])))))

(defn try-jump
  [b s x y dx dy]
  (let [nx (+ x dx dx)
        ny (+ y dy dy)
        b (do-jump b s x y nx ny)]
    (if b [nx ny b])))

(defn collect-jumps
  [b s x y p]
  (remove nil?
    (for [[dx dy] (directions s p)]
      (let [[nx ny b] (try-jump b s x y dx dy)]
        (if b
          (let [more (seq (collect-jumps b s nx ny p))]
            (cons [nx ny] more)))))))

(defn jumps-from
  [b s x y]
  (if-let [more (seq (collect-jumps b s x y (get-p b x y)))]
    (cons [x y] more)))

(defn my-jumps
  [b s]
  (remove nil?
    (for [[x y] (my-squares b s)]
      (jumps-from b s x y))))

(defn do-move
  [b s x y nx ny]
  (if (and (< -1  x +size+)
           (< -1  y +size+)
           (< -1 nx +size+)
           (< -1 ny +size+)
           (=  1 (Math/abs (- nx x)))
           (=  1 (Math/abs (- ny y))))
    (let [p (get-p b x y)]
      (if (open? (get-p b nx ny))
        (set-p* b [x y 0] [nx ny (promote nx ny p)])))))

(defn try-move
  [b s x y dx dy]
  (let [nx (+ x dx)
        ny (+ y dy)
        b (do-move b s x y nx ny)]
    (if b [nx ny b])))

(defn collect-moves
  [b s x y p]
  (remove nil?
    (for [[dx dy] (directions s p)]
      (let [[nx ny b] (try-move b s x y dx dy)]
        (if b (list [nx ny]))))))

(defn moves-from
  [b s x y]
  (if-let [more (seq (collect-moves b s x y (get-p b x y)))]
    (cons [x y] more)))

(defn my-moves
  [b s]
  (remove nil?
    (for [[x y] (my-squares b s)]
      (moves-from b s x y))))

(defn do-play
  [b s x y nx ny]
  (let [diff (Math/abs (- nx x))]
    (if (= 2 diff)
      (do-jump b s x y nx ny)
      (do-move b s x y nx ny))))

(defn do-plays
  [b s [x y nx ny & more]]
  (let [b (do-play b s x y nx ny)]
    (if more
      (recur b s (concat [nx ny] more))
      b)))

(defn my-plays
  [b s]
  (let [jumps (my-jumps b s)]
    (if (seq jumps) jumps (my-moves b s))))

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
