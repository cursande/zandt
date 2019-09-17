(ns zandt.sqlite
  (:require [clojure.java.jdbc :refer [get-connection]]
            [mount.core :refer [defstate start]]))

(def sqlite-db-spec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/zandt.sqlite"})

;; Track in-memory db connection, as by default sqlite will create a
;; new connection, execute a command and then close the connection, resulting
;; in the data being completely cleared from RAM.
;; https://grishaev.me/en/clj-sqlite/
(def db-uri "jdbc:sqlite::memory:")

(declare db)

(defn on-start []
  (let [spec {:connection-uri db-uri}
        conn (get-connection spec)]
    (assoc spec :connection conn)))

(defn on-stop []
  (-> db
      :connection
      .close)
  nil)

(defstate
  ^{:on-reload :noop}
  db
  :start (on-start)
  :stop (on-stop))

(defn establish-sqlite-connection []
  "Will return a map with `connection-uri` and `connection` Java object"
  start #'db)
