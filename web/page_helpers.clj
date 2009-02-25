(ns web.page-helpers
  (:use [compojure.html])
  (:use [compojure.html.page-helpers])
  (:use [compojure.json])
  (:use [checkers.rules])
  (:use [checkers.player]))

(defn plays-javascript
  []
  (javascript-tag (str "var plays = " (json (unwind-all (my-plays))) ";")))

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
      [:div#board
        (plays-javascript)
        [:table
          (for [row *board*]
            [:tr
              (for [cell row] [:td cell])])]])))

(defn main-layout
  [content]
  (html (doctype :html4)
    [:html
      [:head
        [:title "Checkers"]
        (include-css "/checkers/s/css/checkers.css")
        (include-js "/checkers/s/js/checkers.js")]
      [:body
        [:h2 "checkers-clojure"]
        content]]))

