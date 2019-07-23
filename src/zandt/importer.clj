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

(defn create-zandt-db []
  (try (db-do-commands sqlite-db-spec
                       (execute! (slurp "db/prepare_zandt_sqlite_db.sql")))
       (catch Exception e
         (println (.getMessage e)))))

(defn message->message-data [message user-id]
  "Returns a map containing user data"
  {:telegram_id (:id message)
   :user_id user-id
   :text (:text message)})

(defn create-user-id [telegram-id]
  "Returns the primary key for the newly created user"
  (insert!))

;; TODO: `user-id` should probably come from finding the user via the
;; `telegram-id` in the db, and then keeping track of the primary key there.
;; Should the id be passed in, after being found with another function?
(defn message->word-and-frequency [message user-id]
  "returns a sequence of maps with each word (case insensitive), the user id and the word count"
  (let [words (re-seq #"\w+" (-> message
                                 (get :text)
                                 (lower-case)))
        word-frequencies (frequencies words)
        telegram_id (:from_id message)]
    (map (fn [[word count]] {:word word
                             :user_id user-id
                             :count count})
         word-frequencies)))

(defn message->emoji-and-frequency [message user-id]
  "returns a sequence of maps with each emoji, the user id and the emoji count"
  (let [emoji-matching-regex #"☺" ; TODO: Work out how to best match on and store emojis
        emojis (re-seq emoji-matching-regex (get message :text))
        emoji-frequencies (frequencies emojis)
        telegram_id (:from_id message)]
    (map (fn [[emoji count]] {:emoji emoji
                              :user_id user-id
                              :count count})
         emoji-frequencies)))

