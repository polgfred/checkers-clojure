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

(defn new-game
  [session]
  (let [session (assoc session :side +black+ :board +new-board+)]
    (write-session session)
    (with-session-position [session]
      (json {:side side :board board :plays (my-plays)}))))

(defn make-move
  [session move]
  (let [move ()]))
