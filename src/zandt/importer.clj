;; This namespace is for importing raw export data into a sqlite
;; database
(ns zandt.importer
  (:require [cheshire.core :refer [parse-string]]
            [clojure.java.jdbc :refer :all]))

;; http://clojure-doc.org/articles/ecosystem/java_jdbc/using_sql.html
;; https://github.com/clojure/java.jdbc
;; https://grishaev.me/en/clj-sqlite/
(def sqlite-db-spec
  {:dbtype   "sqlite"
   :dbname    "zandt"})

(defn json-string->data-map [export-file-path]
  (parse-string (slurp export-file-path) true))

(defn update-or-insert-user-data [data-map]
  (let [user-data (-> data-map
                      (get "personal_information")
                      (select-keys :user_id :first_name :last_name))]
    (jdbc/insert! sqlite-db-spec :users {:telegram_id (:user_id user-data)
                                         :first_name (:first_name user-data)
                                         :last_name (:last_name user-data)})))

;; Add function that:
;;   - Inserts messages
;;   - Scans through and either inserts word or updates word count
;;   - Scans through and inserts emojis found in messages

(def export-data (json-string->data-map "./test/fixtures/minimal_export_data.json"))
