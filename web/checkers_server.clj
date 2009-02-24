(ns web.checkers-server
  (:use [compojure.html])
  (:use [compojure.file-utils])
  (:use [compojure.http.helpers])
  (:use [compojure.http.servlet])
  (:use [compojure.http.routes])
  (:use [compojure.server.jetty :only (defserver start stop)])
  (:use [checkers.rules])
  (:use [checkers.player]))

(defn main-layout
  [content]
  (html
    [:html
      [:head
        [:title "Checkers"]
        [:script {:type "text/javascript" :src "/checkers/s/js/checkers.js"}]]
      [:body
        [:h2 "checkers-clojure"]
        content]]))

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

(defservlet static-servlet
  (GET "/*"
    (serve-file "./web/public" (route :*))))

(defservlet checkers-servlet
  (GET "/"
    [{"Content-Type" "text/html"} (main-layout (board-table))]))

(defserver checkers-server
  {:port 9090}
  "/checkers/s/*" static-servlet
  "/checkers/*" checkers-servlet)

(start checkers-server)
