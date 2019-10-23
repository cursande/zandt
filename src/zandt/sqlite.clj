(ns zandt.sqlite
  (:require [clojure.java.jdbc :refer [get-connection]]
            [mount.core :refer [defstate
                                start
                                stop]]))

(def sqlite-db-spec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/zandt.sqlite"})

(def test-sqlite-db-spec
  {:classname  "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "db/zandt-test.sqlite"})

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
  "Will return a map with `connection-uri` string and `connection` Java object"
  (start #'db))

(defn close-sqlite-connection []
  "Will close the connection created with `establish-sqlite-connection`"
  (stop #'db))
