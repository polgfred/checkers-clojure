(ns web.checkers-server
  (:use [compojure.html :only (html)])
  (:use [compojure.http.servlet :only (defservlet)])
  (:use [compojure.http.routes :only (GET POST)])
  (:use [compojure.server.jetty :only (defserver start)])
  (:use checkers.rules)
  (:use checkers.player))

(defn board-table
  []
  (let [side +black+
        board [[  1  0  1  0  1  0  1  0  ]
               [  0  1  0  1  0  1  0  1  ]
               [  1  0  1  0  1  0  1  0  ]
               [  0  0  0  0  0  0  0  0  ]
               [  0  0  0  0  0  0  0  0  ]
               [  0 -1  0 -1  0 -1  0 -1  ]
               [ -1  0 -1  0 -1  0 -1  0  ]
               [  0 -1  0 -1  0 -1  0 -1  ]]]
    (with-position [side board]
      [:table
        (for [row *board*]
          [:tr
            (for [cell row] [:td cell])])])))


(defservlet checkers-servlet
  (GET "/checkers"
    [{"Content-Type" "text/html"}
      (html (board-table))]))

(defserver checkers-server
  {:port 9090}
  "/*" checkers-servlet)

(start checkers-server)
