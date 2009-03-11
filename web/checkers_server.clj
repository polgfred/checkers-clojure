(ns web.checkers-server
  (:use [compojure.server.jetty :only (defserver start stop)])
  (:use compojure.file-utils)
  (:use compojure.http.helpers)
  (:use compojure.http.servlet)
  (:use compojure.http.routes)
  (:use web.checkers-helpers))

(defserver checkers-server
  {:port 9090}

  "/checkers/s/*"
    (servlet
      ;; serves static pages - js/css/images
      (GET "/*"     (serve-file "./web/public" (route :*))))

  "/checkers/*"
    (servlet
      ;; redirect root to a static page that will bootstrap via Ajax
      (GET "/"      (redirect-to "/checkers/s/html/checkers.html"))
      
      ;; actions that return JSON
      (GET "/new"   (new-game  session))))

(start checkers-server)
