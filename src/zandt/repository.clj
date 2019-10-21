(ns zandt.repository
  (:require [clojure.java.jdbc :refer :all]
            [zandt.sqlite :refer [db]]))

(def zandt-db-scripts ["db/tables/users.sql"
                       "db/tables/messages.sql"
                       "db/tables/words.sql"
                       "db/tables/emojis.sql"])

(defn create-zandt-db []
  (doseq [script zandt-db-scripts]
    (try (execute! db (slurp script))
         (prn (str script " successfully executed"))
         (catch Exception e
           (prn (.getMessage e))))))

(defn update-or-insert!
  "Updates columns or inserts a new row in the specified table"
  [db table row where-clause]
  (with-db-transaction [t-con db]
    (let [result (update! t-con table row where-clause)]
      (if (zero? (first result))
        (insert! t-con table row)
        result))))

(defn update-or-initialize-frequency!
  "Finds and increments record frequency, or initialises new record with frequency"
  [db table row where-clause]
  (let [[column-parameters & values] where-clause
        select-statement (vec (concat [(str "SELECT frequency FROM " (name table) " WHERE " column-parameters)] values))
        result (query db
                      select-statement
                      {:result-set-fn first})
        new-frequency (+ (get result :frequency 0)
                         (get row :frequency))]
    (if (seq result)
      (update! db table (assoc result :frequency new-frequency) where-clause)
      (insert! db table row))))

(defn primary-id [result]
  (if (map? (first result)) ; update will return a map if found record
    (-> result first vals first)
    (-> result first)))

(defn create-or-update-user! [user-data]
  "Returns the primary key for the found or created user"
  (let [telegram-id (get user-data :telegram_id)
        user        (update-or-insert!
                     db
                     :users
                     user-data
                     ["telegram_id = ?" telegram-id])]
    (primary-id user)))

(defn create-or-update-message! [message-data user-id]
  "Returns the primary key for the found or created message"
  (let [telegram-id (get message-data :telegram_id)
        message     (update-or-insert!
                     db
                     :messages
                     message-data
                     ["telegram_id = ?" telegram-id])]
    (primary-id message)))

(defn update-or-initialize-word-frequency! [word-data message-id user-id]
  "Will search for word by `word` and `user-id`.
   If found: adds new `frequency` to the existing value.
   If not found: adds new record, setting `frequency` with passed in `frequency`."
  (let [word (update-or-initialize-frequency!
              db
              :words
              word-data
              ["word = ? AND user_id = ?" (get word-data :word) user-id])]
    (primary-id word)))

(defn update-or-initialize-emoji-frequency! [emoji-data message-id user-id]
  "Will find the word by `emoji` and `user-id`.
   If found: adds new `frequency` to the existing value.
   If not found: adds new record, setting `frequency` with passed in `frequency`."
  (let [emoji (update-or-initialize-frequency!
              db
              :emojis
              emoji-data
              ["emoji = ? AND user_id = ?" (get emoji-data :emoji) user-id])]
    (primary-id emoji)))
