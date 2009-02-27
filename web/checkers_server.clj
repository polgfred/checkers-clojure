(ns web.checkers-server
  (:use [compojure.server.jetty :only (defserver start stop)])
  (:use compojure.file-utils)
  (:use compojure.http.helpers)
  (:use compojure.http.servlet)
  (:use compojure.http.routes)
  (:use web.page-helpers))

(defserver checkers-server
  {:port 9090}

  ;; serves static pages - js/css/images
  "/checkers/s/*"
    (servlet
      (GET "/*"     (serve-file "./web/public" (route :*))))

  ;; handles controller actions
  "/checkers/*"
    (servlet
      (GET "/new"   (new-game  session))
      (GET "/show"  (show-game session))
      (GET "/play"  (make-move session (read-string (params :move))))))

(start checkers-server)
