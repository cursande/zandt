(ns zandt.importer
  (:require [clojure.string :refer [trim-newline lower-case]]
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
  "Returns a map containing user data"
  {:telegram_id (:from_id message) :text (:text message)})

(defn message->word-and-frequency [message]
  "Returns a sequence of maps with each word (case insensitive), the external user id and the word count"
  (let [words (re-seq #"\w+" (-> message
                                 (get :text)
                                 (lower-case)))
        word-frequencies (frequencies words)]
    (map (fn [[word count]] {:word word
                             :telegram_id (:from_id message)
                             :count count})
         word-frequencies)))

(defn -main [arg]
  (let [export-data (json-string->data-map arg)]
    (create-zandt-db)))

(def export-data (json-string->data-map "./test/fixtures/minimal_export_data.json"))

(slurp "db/prepare_zandt_sqlite_db.sql")
