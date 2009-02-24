(ns web.page-helpers
  (:use [compojure.html])
  (:use [compojure.html.page-helpers])
  (:use [checkers.rules])
  (:use [checkers.player]))

(defn main-layout
  [content]
  (html (doctype :html4)
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

