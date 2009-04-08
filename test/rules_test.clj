(use 'checkers.rules)

(defmacro assert*
  [& exprs]
  `(do ~@(for [expr exprs] `(assert ~expr))))

(defn- in?
  [v coll] (contains? (set coll) v))

(defn- not-in?
  [v coll] (not (in? v coll)))

(let [b  [[  0  0  1  0  0  0  0  0  ]
          [  0 -1  0 -1  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0 -1  0 -1  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0 -1  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]]]

  ;;; basic get/set tests

  (assert* (==  0 (get-p b 0 0))
           (==  1 (get-p b 2 0))
           (== -1 (get-p b 1 1)))

  (let [b2 (set-p b 0 0 2)]
    (assert (== 2 (get-p b2 0 0))))

  (let [b2 (set-p* b [4 0 1] [5 1 -1] [6 2 0])]
    (assert* (==  1 (get-p b2 4 0))
             (== -1 (get-p b2 5 1))
             (==  0 (get-p b2 6 2))))

  ;;; square enumerations

  (assert* (in? [0 0 0] (squares b))
           (in? [2 0 1] (squares b))
           (in? [1 1 -1] (squares b))
           (not-in? [0 1 0] (squares b)))

  (assert* (not-in? [0 0 0] (my-squares b +black+))
           (in? [2 0 1] (my-squares b +black+))
           (not-in? [1 1 -1] (my-squares b +black+))
           (not-in? [0 1 0] (my-squares b +black+)))

  (assert* (not-in? [0 0 0] (my-squares b +red+))
           (not-in? [2 0 1] (my-squares b +red+))
           (in? [1 1 -1] (my-squares b +red+))
           (not-in? [0 1 0] (my-squares b +red+)))

  ;;; direction enumerations

  (assert* (= (directions +black+ 1) '([1 1] [-1 1]))
           (= (directions +black+ 2) '([1 1] [-1 1] [1 -1] [-1 -1])))

  (assert* (= (directions +red+ -1) '([-1 -1] [1 -1]))
           (= (directions +red+ -2) '([-1 -1] [1 -1] [-1 1] [1 1]))))

;;; jump logic

(let [b  [[  0  0  1  0  0  0  0  0  ]
          [  0 -1  0 -1  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0 -1  0 -1  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0 -1  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]]]

  (let [jumps (unwind-plays (jumps-from b +black+ 2 0))]
    (assert* (in? '([2 0] [0 2]) jumps)
             (in? '([2 0] [4 2] [2 4]) jumps)
             (in? '([2 0] [4 2] [6 4] [4 6]) jumps)
             (not-in? '([2 0] [4 2]) jumps)
             (not-in? '([2 0] [4 2] [6 4]) jumps)))

  (let [[nx ny b2] (try-jump b +black+ 2 0 [-1 1])]
    (assert* (= [nx ny] [0 2])
             (= 0 (get-p b2 2 0))
             (= 0 (get-p b2 1 1))
             (= 1 (get-p b2 0 2)))))

(let [b  [[  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  1  0  0  0  0  ]
          [  0  0 -1  0 -1  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0 -1  0 -1  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]]]

  (let [jumps (unwind-plays (jumps-from b +black+ 3 3))]
    (assert* (in? '([3 3] [5 5] [3 7]) jumps)
             (in? '([3 3] [1 5] [3 7]) jumps)
             (not-in? '([3 3] [5 5]) jumps)
             (not-in? '([3 3] [1 5]) jumps)
             (not-in? '([3 3] [5 5] [3 7] [1 5]) jumps)
             (not-in? '([3 3] [5 5] [3 7] [1 5] [3 3]) jumps)
             (not-in? '([3 3] [1 5] [3 7] [5 5]) jumps)
             (not-in? '([3 3] [1 5] [3 7] [5 5] [3 3]) jumps)))

  (let [[nx ny b2] (try-jump b +black+ 3 3 [1 1])]
    (assert* (= 0 (get-p b2 3 3))
             (= 0 (get-p b2 4 4))
             (= 1 (get-p b2 5 5)))

    (let [[nx ny b3] (try-jump b2 +black+ 5 5 [-1 1])]
      (assert* (= 0 (get-p b3 5 5))
               (= 0 (get-p b3 4 6))
               (= 2 (get-p b3 3 7))))))

(let [b  [[  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  2  0  0  0  0  ]
          [  0  0 -1  0 -1  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0 -1  0 -1  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]]]

  (let [jumps (unwind-plays (jumps-from b +black+ 3 3))]
    (assert* (in? '([3 3] [5 5] [3 7] [1 5] [3 3]) jumps)
             (in? '([3 3] [1 5] [3 7] [5 5] [3 3]) jumps)
             (not-in? '([3 3] [5 5] [3 7] [1 5] [3 3] [5 5]) jumps)
             (not-in? '([3 3] [5 5]) jumps)
             (not-in? '([3 3] [1 5]) jumps)
             (not-in? '([3 3] [5 5] [3 7]) jumps)
             (not-in? '([3 3] [1 5] [3 7]) jumps)))

  (let [[nx ny b2] (try-jump b +black+ 3 3 [1 1])]
    (assert* (= 0 (get-p b2 3 3))
             (= 0 (get-p b2 4 4))
             (= 2 (get-p b2 5 5)))

    (let [[nx ny b3] (try-jump b2 +black+ 5 5 [-1 1])]
      (assert* (= 0 (get-p b3 5 5))
               (= 0 (get-p b3 4 6))
               (= 2 (get-p b3 3 7)))

      (let [[nx ny b4] (try-jump b3 +black+ 3 7 [-1 -1])]
        (assert* (= 0 (get-p b4 3 7))
                 (= 0 (get-p b4 2 6))
                 (= 2 (get-p b4 1 5)))

        (let [[nx ny b5] (try-jump b4 +black+ 1 5 [1 -1])]
          (assert* (= 0 (get-p b5 1 5))
                   (= 0 (get-p b5 2 4))
                   (= 2 (get-p b5 3 3))))))))

;;; move logic

(let [b  [[  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  2  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  1  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0 -1  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]]]

  (let [moves (unwind-plays (moves-from b +black+ 2 4))]
    (assert* (in? '([2 4] [3 5]) moves)
             (in? '([2 4] [1 5]) moves)
             (not-in? '([2 4] [3 3]) moves)
             (not-in? '([2 4] [1 3]) moves))

    (let [[nx ny b2] (try-move b +black+ 2 4 [1 1])]
      (assert* (= [nx ny] [3 5])
               (= 0 (get-p b2 2 4))
               (= 1 (get-p b2 3 5))))

    (let [moves (unwind-plays (moves-from b +black+ 4 2))]
      (assert* (in? '([4 2] [5 3]) moves)
               (in? '([4 2] [3 3]) moves)
               (in? '([4 2] [5 1]) moves)
               (in? '([4 2] [3 1]) moves))))

  (let [moves (unwind-plays (moves-from b +red+ 4 6))]
    (assert* (in? '([4 6] [3 5]) moves)
             (in? '([4 6] [5 5]) moves)
             (not-in? '([4 6] [3 7]) moves)
             (not-in? '([4 6] [3 7]) moves))))

;; enumerators

(let [b  [[  0  0  1  0  0  0  0  0  ]
          [  0 -1  0 -1  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  1  0  0  0  1  0  ]
          [  0 -1  0 -1  0 -1  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]]]

  (assert (= (my-jumps b +black+)
                '(([2 0] ([4 2]) ([0 2]))
                  ([2 4] ([4 6]) ([0 6]))
                  ([6 4] ([4 6])))))
  (assert (= (my-jumps b +black+)
             (my-plays b +black+))))

(let [b  [[  1  0  1  0  1  0  1  0  ]
          [  0  1  0  1  0  1  0  1  ]
          [  1  0  1  0  1  0  1  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0 -1  0 -1  0 -1  0 -1  ]
          [ -1  0 -1  0 -1  0 -1  0  ]
          [  0 -1  0 -1  0 -1  0 -1  ]]]

  (assert (= (my-moves b +black+)
                '(([0 2] ([1 3]))
                  ([2 2] ([3 3]) ([1 3]))
                  ([4 2] ([5 3]) ([3 3]))
                  ([6 2] ([7 3]) ([5 3])))))
  (assert (= (my-moves b +black+) (my-plays b +black+))))

;; replaying

(let [b  [[  0  0  1  0  0  0  0  0  ]
          [  0 -1  0 -1  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0 -1  0 -1  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0 -1  0  0  ]
          [  0  0  0  0  0  0  0  0  ]
          [  0  0  0  0  0  0  0  0  ]]]

  (let [b2 (do-plays b +black+ '([2 0] [4 2] [6 4] [4 6]))]
    (assert (= b2  [[  0  0  0  0  0  0  0  0  ]
                    [  0 -1  0  0  0  0  0  0  ]
                    [  0  0  0  0  0  0  0  0  ]
                    [  0  0  0 -1  0  0  0  0  ]
                    [  0  0  0  0  0  0  0  0  ]
                    [  0  0  0  0  0  0  0  0  ]
                    [  0  0  0  0  1  0  0  0  ]
                    [  0  0  0  0  0  0  0  0  ]]))))

;; unwinding

(defn assert-unwound
  [wound unwound]
  (assert (= unwound (unwind-all wound))))

(assert-unwound
  '(([0 2] ([1 3])) ([2 2] ([3 3]) ([1 3])))
  '(([0 2] [1 3]) ([2 2] [3 3]) ([2 2] [1 3])))

(assert-unwound
  '(([2 0] ([4 2]) ([0 2])) ([2 4] ([4 6]) ([0 6])) ([6 4] ([4 6])))
  '(([2 0] [4 2]) ([2 0] [0 2]) ([2 4] [4 6]) ([2 4] [0 6]) ([6 4] [4 6])))
