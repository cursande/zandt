(ns zandt.repository-test
  (:require [zandt.repository :refer [create-zandt-db
                                      update-or-insert!
                                      find-or-update-user!]]
            [zandt.sqlite :refer [db
                                  sqlite-db-spec
                                  test-sqlite-db-spec
                                  establish-sqlite-connection
                                  close-sqlite-connection]]
            [clojure.java.io :refer [delete-file]]
            [clojure.java.jdbc :refer [query]]
            [clojure.test :refer :all]))

(defn with-test-db [tests]
  (with-redefs
    [sqlite-db-spec test-sqlite-db-spec]
    sqlite-db-spec)
  (prn "### Setting up test sqlite db ###")
  (establish-sqlite-connection)
  (create-zandt-db)
  (tests)
  (prn "### tearing down sqlite db ###")
  (close-sqlite-connection))

(use-fixtures :once with-test-db)

(deftest test-find-or-update-user!
  (let [user-data {:username "Falla"
                   :telegram_id 8888888888}]
    (find-or-update-user! user-data)
    (is (= (query db
                  ["SELECT username, telegram_id FROM users WHERE telegram_id = ?"
                   (get user-data :telegram_id)])
           '({:username "Falla" :telegram_id 8888888888})))))

(deftest test-find-or-update-message!)
(deftest test-create-or-increment-word!)
(deftest test-create-or-increment-emoji!)
