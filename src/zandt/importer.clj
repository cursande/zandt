(ns zandt.importer
  (:require [clojure.string :refer [trim-newline]]
            [cheshire.core :refer [parse-string]]
            [clojure.java.jdbc :refer :all]))

(def sqlite-db-spec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/zandt.db"})

(defn json-string->data-map [export-file-path]
  (parse-string (slurp export-file-path) true))

(defn create-zandt-db
  (try (db-do-commands sqlite-db-spec
                       (execute! (slurp "db/prepare_zandt_sqlite_db.sql")))
       (catch Exception e
         (println (.getMessage e)))))

(defn message->message-data [message]
  "Returns a map containing data to be inserted into the messages table"
  {:telegram_id (:from_id message) :text (:text message)})

;; TODO: Upsert/take current value of word
;; UPDATE OR IGNORE ... (increment current value by frequency from message)
;; INSERT OR IGNORE ... (add to default 0 the value by frequency from message)

(defn -main [arg]
  (let [export-data (json-string->data-map arg)]
    (create-zandt-db)))

(def export-data (json-string->data-map "./test/fixtures/minimal_export_data.json"))

(slurp "db/prepare_zandt_sqlite_db.sql")
