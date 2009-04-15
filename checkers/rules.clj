(ns checkers.rules)

(defn avg
  "The arithmetic average of `values'."
  [& values] (/ (reduce + values) (count values)))

(def +size+   8)
(def +black+  1)
(def +red+   -1)

(let [chr-s {0 ". " 1 "b " 2 "B " -1 "r " -2 "R "}
      num-s (partial format "%d ")]
  (defn dump-board
    "The string representation of 2-d board vector `b'.

    For example, the starting position returns:
      7 . r . r . r . r
      6 r . r . r . r .
      5 . r . r . r . r
      4 . . . . . . . .
      3 . . . . . . . .
      2 b . b . b . b .
      1 . b . b . b . b
      0 b . b . b . b .
        0 1 2 3 4 5 6 7"
    [b]
    (reduce str
      (concat
        (for [y (reverse (range +size+))]
          (str (num-s y) (reduce str (map chr-s (get b y))) "\n"))
        (list (str "  " (reduce str (map num-s (range +size+))) "\n"))))))

(defn get-p
  "Given board `b', the value of the piece at (x,y)."
  [b x y] (get (get b y) x))

(defn set-p
  "Given board `b', a new board after setting (x,y) => p."
  [b x y p] (assoc b y (assoc (get b y) x p)))

(defmacro set-p*
  "Convenience macro to thread calls to set-p.

  For example:
    (set-p* b [0 0 0] [1 1 0] [2 2 1])
  expands to:
    (set-p (set-p (set-p b 2 2 1) 1 1 0) 0 0 0)."
  [b this & more]
  (if more
    `(set-p (set-p* ~b ~@more) ~@this)
    `(set-p ~b ~@this)))

(defn my-piece?
  "Whether piece `p' is the piece (not king) of side `s'."
  [s p] (= p s))

(defn my-king?
  "Whether piece `p' is the king of side `s'."
  [s p] (= p (* 2 s)))

(defn mine?
  "Whether piece `p' belongs to side `s'."
  [s p] (or (my-piece? s p) (my-king? s p)))

(defn opp?
  "Whether piece `p' belongs to the opponent of side `s'."
  [s p] (mine? s (- p)))

(defn open?
  "Whether piece `p' is the empty square."
  [p] (= p 0))

(defn playable?
  "Whether (x,y) is a playable square."
  [x y]
  (= (rem (+ x y) 2) 0))

(defn promoted?
  "Whether piece `p' is crowned by moving to (nx,ny)."
  [nx ny p]
  (or (and (= p  1) (= ny 7))
      (and (= p -1) (= ny 0))))

(defn promote
  "The promoted value of `p' after moving to (nx,ny)."
  [nx ny p]
  (if (promoted? nx ny p) (* 2 p) p))

(defn squares
  "Lazy sequence of all squares and piece values of board `b' as ([x y p]...).

  For example, the starting position returns:
    ([0 0 1] [2 0 1] [4 0 1] [6 0 1] ...)."
  [b]
  (for [x (range 8)
        y (range 8)
        :when (playable? x y)]
    [x y (get-p b x y)]))

(defn my-squares
  "Filter of (squares) containing only pieces belonging to side `s'."
  [b s]
  (for [[x y p :as xyp] (squares b)
        :when (mine? s p)]
    xyp))

(defn directions
  "Lazy sequence of directions piece `p' can move as ([dx dy] ...).

  For values of `p' in:
    1 (black piece) => ([1 1] [-1 1]);
    2 (black king)  => ([1 1] [-1 1] [1 -1] [-1 -1]);
   -1 (red piece)   => ([-1 -1] [1 -1]);
   -2 (red king)    => ([-1 -1] [1 -1] [-1 1] [1 1]).

  This function is memoized for performance."
  [p]
  (let [s (cond (> p 0) +black+ (< p 0) +red+)
        ahead-back [s (- s)]
        ahead [s]]
    (for [dy (if (my-king? s p) ahead-back ahead)
          dx ahead-back]
      [dx dy])))

(def directions (memoize directions)) ; will never change

(defn do-jump
  "Given board `b', determine whether side `s' can jump from (x,y) to (nx,ny),
  and if so, return the new board after jumping."
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
        (set-p* b [x y 0]
                  [mx my 0]
                  [nx ny (promote nx ny p)])))))

(defn collect-jumps
  "Given board `b' and side `s', the child nodes of the jump tree from (x,y) by piece `p'.

  This is a helper for `jumps-from', and is not meant to be invoked directly."
  [b s x y p]
  (remove nil?
    (for [[dx dy] (directions p)]
      (let [nx (+ x dx dx) ny (+ y dy dy)]
        (if-let [b (do-jump b s x y nx ny)]
          (let [more (seq (collect-jumps b s nx ny p))]
            (conj more ny nx)))))))

(defn jumps-from
  "Given board `b' and side `s', the tree of jumps from (x,y).

  For example, given this starting position, with black to play:
    7 . . . . . . . .
    6 . . . . . . . .
    5 . . . . . r . .
    4 . . . . . . . .
    3 . . . r . r . .
    2 . . . . . . . .
    1 . r . r . . . .
    0 . . b . . . . .
      0 1 2 3 4 5 6 7
  the jump tree from (2,0) is:
    `(2,0)
      `-(4,2)
      | `-(6,4)
      | | `-(4,6)
      | +-(2,4)
      +-(0,2)
  which is represented as:
    (2 0 (4 2 (6 4 (4 6)) (2 4)) (0 2))."
  [b s x y]
  (if-let [more (seq (collect-jumps b s x y (get-p b x y)))]
    (conj more y x)))

(defn my-jumps
  "Given board `b', the list of all jump trees for side `s', or the empty
  list if `s' cannot jump."
  [b s]
  (remove nil?
    (for [[x y] (my-squares b s)]
      (jumps-from b s x y))))

(defn do-move
  "Given board `b', determine whether side `s' can move from (x,y) to (nx,ny),
  and if so, return the new board after moving."
  [b s x y nx ny]
  (if (and (< -1  x +size+)
           (< -1  y +size+)
           (< -1 nx +size+)
           (< -1 ny +size+)
           (=  1 (Math/abs (- nx x)))
           (=  1 (Math/abs (- ny y))))
    (let [p (get-p b x y)]
      (if (open? (get-p b nx ny))
        (set-p* b [x y 0]
                  [nx ny (promote nx ny p)])))))

(defn collect-moves
  "Given board `b' and side `s', the child nodes of the move tree from (x,y) by piece `p'.

  This is a helper for `moves-from', and is not meant to be invoked directly."
  [b s x y p]
  (remove nil?
    (for [[dx dy] (directions p)]
      (let [nx (+ x dx) ny (+ y dy)]
        (if-let [b (do-move b s x y nx ny)]
          (list nx ny))))))

(defn moves-from
  "Given board `b' and side `s', the tree of moves from (x,y).

  For example, given the starting position, with black to play,
  the move tree from (2,2) is:
    `(2,2)
      `-(3,3)
      +-(1,3)
  which is represented as:
    (2 2 (3 3) (1 3))."
  [b s x y]
  (if-let [more (seq (collect-moves b s x y (get-p b x y)))]
    (conj more y x)))

(defn my-moves
  "Given board `b', the list of all move trees for side `s', or the empty
  list if `s' cannot move."
  [b s]
  (remove nil?
    (for [[x y] (my-squares b s)]
      (moves-from b s x y))))

(defn do-play
  "Forwards to either `do-jump' or `do-move', depending on whether the play
  (x,y) => (nx,ny) is a jump or a move."
  [b s x y nx ny]
  (let [diff (Math/abs (- nx x))]
    (if (= 2 diff)
      (do-jump b s x y nx ny)
      (do-move b s x y nx ny))))

(defn do-plays
  "Chains calls to `do-play' over a multi-jump sequence, and returns the
  new board after all plays have been made.

  For example:
    (do-plays b s [2 0 4 2 6 4 4 6])
  is equivalent to:
    (do-play (do-play (do-play b s 2 0 4 2) s 4 2 6 4) s 6 4 4 6)."
  [b s [x y nx ny & more]]
  (let [b (do-play b s x y nx ny)]
    (if more
      (recur b s (conj more ny nx))
      b)))

(defn my-plays
  "Given board `b', the list of jump trees for side `s' if `s' can jump,
  else the list of move trees for side `s' if `s' can move, else the empty list."
  [b s]
  (let [jumps (my-jumps b s)]
    (if (seq jumps) jumps (my-moves b s))))

(defn unwind
  "Unwinds a single jump or move tree into a list of plays.

  For example, the tree:
    (2 0 (4 2 (6 4 (4 6)) (2 4)) (0 2))
  unwinds as:
    ((2 0 4 2 6 4 4 6) (2 0 4 2 2 4) (2 0 0 2))."
  [[x y & more :as tree]]
  (if more
    (reduce concat
      (for [m more]
        (map #(conj % y x) (unwind m))))
    (list tree)))

(defn unwind-all
  "Unwinds a list of jump or move trees into a single list of plays."
  [plays]
  (reduce concat (map unwind plays)))
