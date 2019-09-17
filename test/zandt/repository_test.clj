(ns zandt.repository-test
  (:require [zandt.repository :refer [create-zandt-db
                                      update-or-insert!
                                      find-or-update-user!]]
            [zandt.sqlite :refer [sqlite-db-spec
                                  test-sqlite-db-spec
                                  establish-sqlite-connection
                                  close-sqlite-connection]]
            [clojure.java.io :refer [delete-file]]
            [clojure.java.jdbc :refer [query execute!]]
            [clojure.test :refer :all]))

(defn with-test-db [tests]
  (with-redefs
    [sqlite-db-spec test-sqlite-db-spec]
    sqlite-db-spec)
  (establish-sqlite-connection)
  (create-zandt-db)
  (tests)
  (close-sqlite-connection))

(use-fixtures :once with-test-db)

(deftest test-find-or-update-user!
  (let [user-data {:username "Falla",
                   :telegram_id 8888888888}]
    (find-or-update-user! user-data)
    (is (= (query test-sqlite-db-spec
                  "SELECT * FROM users WHERE telegram_id = ?"
                  (get user-data :telegram_id))
           "hi"))))

(deftest test-find-or-update-message!)
(deftest test-create-or-increment-word!)
(deftest test-create-or-increment-emoji!)

(deftest test-message->message-data
    (let [message {:id 99999
                        :type "message"
                        :date "2018-03-10T17:45:50"
                        :edited "1970-01-01T10:00:00"
                        :from "Some one"
                        :from_id 12345
                   :text "Random message"}
          user-id 999]
      (is (= (message->message-data message user-id)
             {:telegram_id 99999
              :user_id 999
              :text "Random message"}))))
