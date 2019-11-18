(ns zandt.importer-test
  (:require [clojure.test :refer :all]
            [zandt.transformer :refer [telegram-user->user-data]]
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
    (let [from-id 12345
          from    "Some one"
          users-map (atom {})]
      (find-user-id-from-users-map from-id from users-map)
      (is (= [from-id]
             (keys @users-map)))))

  (testing "Where the user already exists, it simply pulls the primary key from the user-map"
    (let [from-id 12345
          from    "Some one"
          user-id  (create-or-update-user! (telegram-user->user-data from-id from))
          users-map (atom {from-id user-id})]
      (is (= user-id
             (find-user-id-from-users-map from-id from users-map))))))

(deftest test-import-chats
  (testing "it imports chat data into a data store"
    (let [data-map (json-string->data-map "test/fixtures/minimal_export_data.json")]
      (import-chats data-map)

      (is (= {(keyword "count(*)") 4}
             (query db ["SELECT COUNT(*) FROM messages"] {:result-set-fn first})))

      (is (= #{{:username "Falla F"} {:username "Bolger"}}
             (set (query db ["SELECT username FROM users"]))))

      (is (= [{:frequency 2, :word "it"}
              {:frequency 2, :word "hey"}]
             (query db ["SELECT frequency, word FROM words WHERE user_id = 1 ORDER BY frequency DESC LIMIT 2"])))

      (is (= [{:frequency 2 :emoji "☺"}]
             (query db ["SELECT frequency, emoji FROM emojis WHERE user_id = 1 ORDER BY frequency DESC"]))))))
