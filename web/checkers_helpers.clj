(ns web.checkers-helpers
  (:use compojure.json)
  (:use checkers.rules)
  (:use checkers.player))

(defmacro with-session-position
  [[session] & exprs]
  `(let [~'side (~session :side) ~'board (~session :board)]
    (with-position [~'side ~'board]
      ~@exprs)))

(defn new-game
  [session]
  (dosync
    (alter session assoc
      :side +black+
      :board
        [[  1  0  1  0  1  0  1  0  ]
         [  0  1  0  1  0  1  0  1  ]
         [  1  0  1  0  1  0  1  0  ]
         [  0  0  0  0  0  0  0  0  ]
         [  0  0  0  0  0  0  0  0  ]
         [  0 -1  0 -1  0 -1  0 -1  ]
         [ -1  0 -1  0 -1  0 -1  0  ]
         [  0 -1  0 -1  0 -1  0 -1  ]]))
  (with-session-position [session]
    (json {:board board :plays (my-plays)})))
