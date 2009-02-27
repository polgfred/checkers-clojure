(ns web.page-helpers
  (:use compojure.html)
  (:use compojure.html.page-helpers)
  (:use compojure.http.helpers)
  (:use compojure.json)
  (:use checkers.rules)
  (:use checkers.player))

(defmacro with-vars
  [[session] & exprs]
  `(let [~'side (~session :side) ~'board (~session :board)]
    ~@exprs))

(defn plays-javascript
  []
  (javascript-tag (str "var plays = " (json (my-plays)) ";")))

(let [image-map {1 "pb.png" 2 "kb.png" -1 "pr.png" -2 "kr.png"}]
  (defn piece-image
    [p]
    (if-let [image-name (image-map p)]
      [:img {:src (str "/checkers/s/images/" image-name)}])))

(defn table-cell
  [x y p]
  (if (playable? x y)
    [:td.on  "" (piece-image p)]
    [:td.off ""]))

(defn board-table
  [board]
  [:div#board-container
    [:table#board
      (for [y (reverse (range +size+))]
        [:tr
          (for [x (range +size+)]
            (table-cell x y (get-p x y board)))])]])

(defn move-links
  [side board]
  (with-position [side board]
    (unordered-list
      (for [play (unwind-all (my-plays))]
        (link-to (url-params "/checkers/play" {:move (str play)})
          (h (str play)))))))

(defn control-area
  [side board]
  (if (= side +black+)
    [:div#control
      [:h4 "your move - black"]
      (move-links side board)]
    [:div#control
      [:h4 "my move - red"]
      (move-links side board)]))

(defn main-layout
  [side board]
  [{"Content-type" "text/html"}
    (html (doctype :html4)
      (xhtml-tag "en"
        [:head
          [:title "checkers-clojure"]
          (include-css "/checkers/s/css/checkers.css")
          (include-js "/checkers/s/js/checkers.js")]
        [:body
          [:h2 "checkers-clojure"]
          (board-table board)
          (control-area side board)]))])

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
  (redirect-to "/checkers/show"))

(defn show-game
  [session]
  (with-vars [session]
    (main-layout side board)))

(defn make-move
  [session move]
  (with-vars [session]
    (with-position [side board]
      (let [board (do-plays move)]
        (dosync
          (alter session assoc
            :side (- side)
            :board board)))))
  (redirect-to "/checkers/show"))
