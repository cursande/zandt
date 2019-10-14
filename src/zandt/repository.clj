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

(defn primary-id [result]
  (if (map? (first result)) ; update will return a map if new record
    (-> result first vals first)
    (-> result first)))

(defn find-or-update-user! [user-data]
  "Returns the primary key for the found or created user"
  (let [telegram-id (get user-data :telegram_id)
        user        (update-or-insert! db
                                       :users
                                       user-data
                                       ["telegram_id = ?" telegram-id])]
    (primary-id user)))

;; (defn find-or-update-message! [message user-id]
;;   "Returns the primary key for the found or created message"

;;   )

;; (defn find-or-update-word! []
;;   "Will find the word by `word` and `user-id`, increment if found"

;;   )

;; (defn find-or-update-emoji! []
;;   "Will find the word by `emoji` and `user-id`, increment if found"

;;   )
