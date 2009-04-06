(ns web.checkers-helpers
  (:use compojure.http.session)
  (:use compojure.json)
  (:use checkers.rules)
  (:use checkers.player))

(defmacro with-session-position
  [[session] & exprs]
  `(let [~'side (~session :side) ~'board (~session :board)]
    (with-position [~'side ~'board]
      ~@exprs)))

(def +new-board+ [[  1  0  1  0  1  0  1  0  ]
                  [  0  1  0  1  0  1  0  1  ]
                  [  1  0  1  0  1  0  1  0  ]
                  [  0  0  0  0  0  0  0  0  ]
                  [  0  0  0  0  0  0  0  0  ]
                  [  0 -1  0 -1  0 -1  0 -1  ]
                  [ -1  0 -1  0 -1  0 -1  0  ]
                  [  0 -1  0 -1  0 -1  0 -1  ]])

(defn game-new
  [session]
  (println "in game-new:")
  (with-position [+black+ +new-board+]
    (println "new game")
    (println (dump-board *board*))
    (write-session (assoc session :side *side* :board *board*))
    (json {:side *side* :board *board* :plays (my-plays)})))

(defn game-play
  [session move]
  (println "in game-play:")
  (with-session-position [session]
    (let [board (do-plays move)]
      (println "your move")
      (println (dump-board board))
      (with-position [(- side) board]
        (let [move (second (best-play))
              board (do-plays move)]
          (println "my move")
          (println (dump-board board))
          (with-position [side board]
            (write-session (assoc session :board board))
            (json {:side side :board board :plays (my-plays)})))))))
