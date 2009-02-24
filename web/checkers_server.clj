(ns web.checkers-server
  (:use [compojure.html :only (html)])
  (:use [compojure.http.servlet :only (defservlet)])
  (:use [compojure.http.routes :only (GET POST)])
  (:use [compojure.server.jetty :only (defserver start)]))

(def bow)


(defservlet checkers-servlet
  (GET "/checkers"
    [{"Content-Type" "text/html"}
      (html
        [:h1 "Hello World"])]))

(defserver checkers-server {:port 9090}
  "/*" checkers-servlet)

(start checkers-server)
