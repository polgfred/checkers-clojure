(ns web.checkers-server
  (:use [compojure.html :only (html)])
  (:use [compojure.http.servlet :only (defservlet)])
  (:use [compojure.http.routes :only (GET POST)])
  (:use [compojure.server.jetty :only (defserver start)])
  (:use checkers.rules)
  (:use checkers.player))

(defn main-layout
  [content]
  (html
    [:head
      [:title "Checkers"]
      [:script {:type "text/javascript" :src "js/checkers.js"}]]
    [:body
      [:h2 "checkers-clojure"]
      content]))

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
  ; (GET "/"
  ;   (file))
  (GET "/"
    [{"Content-Type" "text/html"} (main-layout (board-table))]))

(defserver checkers-server
  {:port 9090}
  "/checkers/*" checkers-servlet)

(start checkers-server)
