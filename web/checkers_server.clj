(ns web.checkers-server
  (:use [compojure.file-utils])
  (:use [compojure.http.helpers])
  (:use [compojure.http.servlet])
  (:use [compojure.http.routes])
  (:use [compojure.server.jetty :only (defserver start stop)])
  (:use [web.page-helpers]))

(defserver checkers-server {:port 9090}
  "/checkers/s/*"
    (servlet
      (GET "/*"
        (serve-file "./web/public" (route :*))))
  "/checkers/*"
    (servlet
      (GET "/"
        [{"Content-Type" "text/html"} (main-layout (board-table))])))

(start checkers-server)
