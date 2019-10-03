(ns zandt.repository
  (:require [clojure.java.jdbc :refer :all]
            [zandt.sqlite :refer [sqlite-db-spec db]]))

(def zandt-db-script "db/prepare_zandt_sqlite_db.sql")

(defn create-zandt-db []
  (try (execute! db (slurp zandt-db-script))
       (catch Exception e
         (println (.getMessage e))
         (throw e))))

(defn update-or-insert!
  "Updates columns or inserts a new row in the specified table"
  [db table row where-clause]
  (with-db-transaction [t-con db]
    (let [result (update! t-con table row where-clause)]
      (if (zero? (first result))
        (insert! t-con table row)
        result))))

(defn find-or-update-user! [user-data]
  "Returns the primary key for the found or created user"
  (let [telegram-id (get user-data :telegram_id)]
    (update-or-insert! db
                       :users
                       user-data
                       (format "WHERE telegram_id = %s"
                               telegram-id))))

;; (defn find-or-update-message! [message user-id]
;;   "Returns the primary key for the found or created message"

;;   )

;; (defn find-or-update-word! []
;;   "Will find the word by `word` and `user-id`, increment if found"

;;   )

;; (defn find-or-update-emoji! []
;;   "Will find the word by `emoji` and `user-id`, increment if found"

;;   )
