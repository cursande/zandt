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

;; TODO: Add function that:
;;   - Processes each chat in a list
;;     - Inserts the user from the chat
;;     - Scans through, collecting each unique word and how frequently it occurs
;;       - Will check db if word exists,
;;         IF it already exists
;;           icrement the count of that word by its frequency in the message
;;         ELSE
;;           Insert the word with its frequency
;;     - Scans through and inserts emojis found in messages
;;       - Finds emojis with emoji matching regex e.g. "(<U\\+\\w+?>)"


(defn -main [arg]
  (let [export-data (json-string->data-map arg)]
    (create-zandt-db)))

(def export-data (json-string->data-map "./test/fixtures/minimal_export_data.json"))

(slurp "db/prepare_zandt_sqlite_db.sql")
