(ns web.page-helpers
  (:use [compojure.html])
  (:use [compojure.html.page-helpers])
  (:use [compojure.json])
  (:use [checkers.rules])
  (:use [checkers.player]))

(defn plays-javascript
  []
  (javascript-tag (str "var plays = " (json (my-plays)) ";")))

(let [image-map {1 "pb.png" 2 "kb.png" -1 "pr.png" -2 "kr.png"}]
  (defn table-cell
    [x y p]
    (if (playable? x y)
      [:td.on
        (let [image-name (image-map p)]
          (if image-name [:img {:src (str "/checkers/s/images/" image-name)}] ""))]
      [:td.off ""])))

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
    [:div#board-container
      (with-position [side board] (plays-javascript))
      [:table#board
        (for [y (reverse (range +size+))]
          [:tr
            (for [x (range +size+)]
              (table-cell x y (with-board [board] (get-p x y))))])]]))

(defn main-layout
  [content]
  (html (doctype :html4)
    (xhtml-tag "en"
      [:head
        [:title "Checkers"]
        (include-css "/checkers/s/css/checkers.css")
        (include-js "/checkers/s/js/checkers.js")]
      [:body
        [:h2 "checkers-clojure"]
        content])))

