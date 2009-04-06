(ns web.checkers-server
  (:use [compojure.server.jetty :only [run-server]])
  (:use compojure.file-utils)
  (:use compojure.http.helpers)
  (:use compojure.http.servlet)
  (:use compojure.http.routes)
  (:use web.checkers-helpers))

(defroutes checkers-server
  ;; serves static pages - js/css/images
  (GET "/s/*" (serve-file "./web/public" (params :*)))
  
  ;; shut the server up about favicon
  (GET "/favicon.ico" "")
  
  ;; game actions that return JSON
  (GET "/new"  (game-new  session))
  (GET "/play" (game-play session (read-string (params :move))))
  
  ;; redirect root to a static page that will bootstrap via Ajax
  (GET "/" (redirect-to "/s/html/checkers.html")))

(run-server {:port 9090} "/*" (servlet checkers-server))
