(ns zandt.repository-test
  (:require [zandt.repository :refer [create-zandt-db
                                      update-or-insert!
                                      create-or-update-user!
                                      create-or-update-message!]]
            [zandt.sqlite :refer [db
                                  sqlite-db-spec
                                  test-sqlite-db-spec
                                  establish-sqlite-connection
                                  close-sqlite-connection]]
            [clojure.java.jdbc :refer [query]]
            [clojure.test :refer :all]))

(defn with-test-db [tests]
  (with-redefs [sqlite-db-spec test-sqlite-db-spec])
  (prn "### Setting up test sqlite db ###")
  (establish-sqlite-connection)
  (create-zandt-db)
  (tests)
  (prn "### tearing down sqlite db ###")
  (close-sqlite-connection))

(use-fixtures :once with-test-db)

(deftest test-create-or-update-user!
  "It creates or updates the user, and returns the primary key"
  (let [user-data {:username "Falla" :telegram_id 8888888888}]
    (create-or-update-user! user-data)
    (is (= (query db
                  ["SELECT username, telegram_id FROM users WHERE telegram_id = ?" (get user-data :telegram_id)])
           '({:username "Falla" :telegram_id 8888888888})))))

;; TODO: Below tests require a user to exist in the db anyway, but it seems awkward to have
;; tests that rely on the above function

(deftest test-create-or-update-message!
  "It creates or updates the message, and returns the primary key"
(let [message-data {:telegram_id  8888888888, :user_id 1 :text "Hello Friends"}
      user-id (create-or-update-user! {:username "Falla"  :telegram_id 8888888888})]
    (create-or-update-message! message-data user-id)
    (is (= (query db
                  ["SELECT user_id, telegram_id, text FROM messages WHERE telegram_id = ?" (get message-data :telegram_id)])
           (list {:user_id user-id :telegram_id 8888888888 :text "Hello Friends"})))))

(deftest test-initialize-or-increment-word!
  (testing "If the record did not exist, it creates it and initialises frequency"
    (let [word (initialize-or-increment-word! )
          user-id (create-or-update-user! {:username "Falla"  :telegram_id 8888888888})])
    ))

;; (deftest test-initialize-or-increment-emoji!)
