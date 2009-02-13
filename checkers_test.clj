(load-file "checkers.clj")

(ns checkers)

(defmacro assert*
  [& exprs] `(do ~@(for [expr exprs] `(assert ~expr))))

(defn- in?
  [v coll] (some (partial = v) coll))

(defn- not-in?
  [v coll] (not (in? v coll)))

(defn- unwind
  [tree]
  (let [[this & next] tree]
    (if next
      (reduce concat (for [n next] (map #(cons this %) (unwind n))))
      (list tree))))

(defmacro with-board
  [v & exprs] `(binding [*board* (reverse-vector ~v)] ~@exprs))

(with-board [[ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0 -1  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0 -1  0 -1  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0 -1  0 -1  0  0  0  0 ]
             [ 0  0  1  0  0  0  0  0 ]]

  ;;; basic get/set tests

  (assert* (== 0 (get-p 0 0)) (== 1 (get-p 2 0)) (== -1 (get-p 1 1)))

  (binding [*board* (set-p 0 0 2)]
    (assert (== 2 (get-p 0 0))))

  (binding [*board* (set-p* [4 0 1] [5 1 -1] [6 2 0])]
    (assert* (== 1 (get-p 4 0)) (== -1 (get-p 5 1)) (== 0 (get-p 6 2))))

  ;;; square enumerations

  (assert* (in? [0 0 0] (squares))
           (in? [2 0 1] (squares))
           (in? [1 1 -1] (squares))
           (not-in? [0 1 0] (squares)))

  (binding [*side* +black+]
    (assert* (not-in? [0 0 0] (my-squares))
             (in? [2 0 1] (my-squares))
             (not-in? [1 1 -1] (my-squares))
             (not-in? [0 1 0] (my-squares))))

  (binding [*side* +red+]
    (assert* (not-in? [0 0 0] (my-squares))
             (not-in? [2 0 1] (my-squares))
             (in? [1 1 -1] (my-squares))
             (not-in? [0 1 0] (my-squares))))

  ;;; direction enumerations

  (binding [*side* +black+]
    (assert* (= (directions 1) '([1 1] [-1 1]))
             (= (directions 2) '([1 1] [-1 1] [1 -1] [-1 -1]))))

  (binding [*side* +red+]
    (assert* (= (directions -1) '([-1 -1] [1 -1]))
             (= (directions -2) '([-1 -1] [1 -1] [-1 1] [1 1])))))

;;; jump logic

(with-board [[ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0 -1  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0 -1  0 -1  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0 -1  0 -1  0  0  0  0 ]
             [ 0  0  1  0  0  0  0  0 ]]

  (let [jumps (unwind (jumps-from 2 0))]
    (assert* (in? '([2 0] [0 2 -1]) jumps)
             (in? '([2 0] [4 2 -1] [2 4 -1]) jumps)
             (in? '([2 0] [4 2 -1] [6 4 -1] [4 6 -1]) jumps)
             (not-in? '([2 0] [4 2 -1]) jumps)
             (not-in? '([2 0] [4 2 -1] [6 4 -1]) jumps)))

  (let [[mx my nx ny cp board] (try-jump 2 0 1 [-1 1])]
    (assert* (= [mx my] [1 1]) (= [nx ny] [0 2]) (= cp -1))
    (binding [*board* board]
      (assert* (= 0 (get-p 2 0)) (= 0 (get-p 1 1)) (= 1 (get-p 0 2))))))

(with-board [[ 0  0  0  0  0  0  0  0 ]
             [ 0  0 -1  0 -1  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0 -1  0 -1  0  0  0 ]
             [ 0  0  0  1  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]]

  (let [jumps (unwind (jumps-from 3 3))]
    (assert* (in? '([3 3] [5 5 -1] [3 7 -1]) jumps)
             (in? '([3 3] [1 5 -1] [3 7 -1]) jumps)
             (not-in? '([3 3] [5 5 -1]) jumps)
             (not-in? '([3 3] [1 5 -1]) jumps)
             (not-in? '([3 3] [5 5 -1] [3 7 -1] [1 5 -1] [3 3 -1]) jumps)
             (not-in? '([3 3] [1 5 -1] [3 7 -1] [5 5 -1] [3 3 -1]) jumps)))

  (let [[mx my nx ny cp board] (try-jump 3 3 1 [1 1])]
    (binding [*board* board]
      (assert* (= 0 (get-p 3 3)) (= 0 (get-p 4 4)) (= 1 (get-p 5 5)))
      (let [[mx my nx ny cp board] (try-jump 5 5 1 [-1 1])]
        (binding [*board* board]
          (assert* (= 0 (get-p 5 5)) (= 0 (get-p 4 6)) (= 2 (get-p 3 7))))))))

(with-board [[ 0  0  0  0  0  0  0  0 ]
             [ 0  0 -1  0 -1  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0 -1  0 -1  0  0  0 ]
             [ 0  0  0  2  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]]

  (let [jumps (unwind (jumps-from 3 3))]
    (assert* (in? '([3 3] [5 5 -1] [3 7 -1] [1 5 -1] [3 3 -1]) jumps)
             (in? '([3 3] [1 5 -1] [3 7 -1] [5 5 -1] [3 3 -1]) jumps)
             (not-in? '([3 3] [5 5 -1] [3 7 -1] [1 5 -1] [3 3 -1] [5 5 -1]) jumps)
             (not-in? '([3 3] [5 5 -1]) jumps)
             (not-in? '([3 3] [1 5 -1]) jumps)
             (not-in? '([3 3] [5 5 -1] [3 7 -1]) jumps)
             (not-in? '([3 3] [1 5 -1] [3 7 -1]) jumps)))

  (let [[mx my nx ny cp board] (try-jump 3 3 2 [1 1])]
    (binding [*board* board]
      (assert* (= 0 (get-p 3 3)) (= 0 (get-p 4 4)) (= 2 (get-p 5 5)))
      (let [[mx my nx ny cp board] (try-jump 5 5 2 [-1 1])]
        (binding [*board* board]
          (assert* (= 0 (get-p 5 5)) (= 0 (get-p 4 6)) (= 2 (get-p 3 7)))
          (let [[mx my nx ny cp board] (try-jump 3 7 2 [-1 -1])]
            (binding [*board* board]
              (assert* (= 0 (get-p 3 7)) (= 0 (get-p 2 6)) (= 2 (get-p 1 5)))
              (let [[mx my nx ny cp board] (try-jump 1 5 2 [1 -1])]
                (binding [*board* board]
                  (assert* (= 0 (get-p 1 5)) (= 0 (get-p 2 4)) (= 2 (get-p 3 3))))))))))))

;;; move logic

(with-board [[ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0 -1  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  1  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  2  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]
             [ 0  0  0  0  0  0  0  0 ]]

  (binding [*side* +black+]
    (let [moves (unwind (moves-from 2 4))]
      (assert* (in? '([2 4] [3 5]) moves)
               (in? '([2 4] [1 5]) moves)
               (not-in? '([2 4] [3 3]) moves)
               (not-in? '([2 4] [1 3]) moves)))

    (let [[nx ny board] (try-move 2 4 1 [1 1])]
      (assert (= [nx ny] [3 5]))
      (binding [*board* board]
        (assert* (= 0 (get-p 2 4)) (= 1 (get-p 3 5)))))

    (let [moves (unwind (moves-from 4 2))]
      (assert* (in? '([4 2] [5 3]) moves)
               (in? '([4 2] [3 3]) moves)
               (in? '([4 2] [5 1]) moves)
               (in? '([4 2] [3 1]) moves))))

  (binding [*side* +red+]
    (let [moves (unwind (moves-from 4 6))]
      (assert* (in? '([4 6] [3 5]) moves)
               (in? '([4 6] [5 5]) moves)
               (not-in? '([4 6] [3 7]) moves)
               (not-in? '([4 6] [3 7]) moves)))))
