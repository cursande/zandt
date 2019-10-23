(ns zandt.importer-test
  (:require [clojure.test :refer :all]
            [zandt.transformer :refer [message->user-data]]
            [zandt.repository :refer [create-or-update-user!]]
            [clojure.java.jdbc :refer [query]]
            [zandt.sqlite :refer [db]]
            [zandt.test-utils :refer [with-test-db]]
            [zandt.importer :refer [import-chats
                                    json-string->data-map
                                    find-user-id-from-users-map]]))

(use-fixtures :once with-test-db)

(deftest test-find-user-id-from-users-map
  (testing "Where the user's id has not already been stored in the map, it creates the user
            and stores them in the map"
    (let [message {:id 99999
                   :type "message"
                   :date "2018-03-10T17:45:50"
                   :edited "1970-01-01T10:00:00"
                   :from "Some one"
                   :from_id 12345
                   :text "Random message"}
          user-map (atom {})]
      (find-user-id-from-users-map message user-map)
      (is (= (keys @user-map)
             (list (:from_id message))))))
  (testing "Where the user already exists, it simply pulls the primary key from the user-map"
    (let [message  {:id 99999
                    :type "message"
                    :date "2018-03-10T17:45:50"
                    :edited "1970-01-01T10:00:00"
                    :from "Some one"
                    :from_id 12345
                    :text "Random message"}
          user-id  (create-or-update-user! (message->user-data message))
          user-map (atom {(:from_id message) user-id})]
      (is (= (find-user-id-from-users-map message user-map)
             user-id)))))

(deftest test-import-chats
  (testing "it imports chat data into a data store"
    (let [data-map (json-string->data-map "test/fixtures/minimal_export_data.json")]
      (import-chats data-map)

      (is (= (query db ["SELECT COUNT(*) FROM messages"] {:result-set-fn first})
             {(keyword "count(*)") 4}))

      (is (= (set (query db ["SELECT username FROM users"]))
             #{{:username "Falla F"} {:username "Bolger"}}))

      (is (= (query db ["SELECT frequency, word FROM words WHERE user_id = 1 ORDER BY frequency DESC LIMIT 3"])
             [{:frequency 2, :word "hey"}
              {:frequency 2, :word "it"}
              {:frequency 1, :word "how's"}]))

      (is (= (query db ["SELECT frequency, emoji FROM emojis WHERE user_id = 1 ORDER BY frequency"])
             [{:frequency 2 :emoji "☺"}])))))
