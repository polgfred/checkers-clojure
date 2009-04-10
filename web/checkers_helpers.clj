(ns web.checkers-helpers
  (:use compojure.http.session)
  (:use compojure.json)
  (:use checkers.rules)
  (:use checkers.player))

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
  (println "new game")

  (let [b +new-board+ s +black+]
    (println (dump-board b))

    (write-session (assoc session :side s :board b))
    (json {:board b :side s :plays (my-plays b s)})))

(defn game-play
  [session move]
  (println "in game-play:")

  (let [b (session :board) s (session :side)]
    (println (dump-board b))

    (let [bx (do-plays b s move) sx (- s)]
      (println "your move =>" (seq move))
      (println (dump-board bx))

      (let [[_ mx] (best-play bx sx)
            bxx (do-plays bx sx mx)]
        (println "my move =>" mx)
        (println (dump-board bxx))

        (write-session (assoc session :board bxx))
        (json {:board bxx :side s :move mx :plays (my-plays bxx s)})))))
