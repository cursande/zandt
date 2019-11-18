(ns zandt.repository-test
  (:require [zandt.repository :refer :all]
            [zandt.sqlite :refer [db]]
            [zandt.test-utils :refer [with-test-db]]
            [clojure.java.jdbc :refer [query]]
            [clojure.test :refer :all]))

(use-fixtures :once with-test-db)

(deftest test-create-or-update-user!
  "It creates or updates the user, and returns the primary key"
  (let [user-data {:username "Falla" :telegram_id 8888888888}]
    (create-or-update-user! user-data)
    (is (= (query db
                  ["SELECT username, telegram_id FROM users WHERE telegram_id = ?" (get user-data :telegram_id)]
                  {:result-set-fn first})
           {:username "Falla" :telegram_id 8888888888}))))

;; TODO: Subsequent tests necessarily rely on above implementations already existing due to foreign keys....yuck. I guess
;; Since there's no schematic constraint maybe it's fine to just insert the record as it is....

(deftest test-create-or-update-message!
  "It creates or updates the message, and returns the primary key"
(let [user-id (create-or-update-user! {:username "Falla" :telegram_id 8888888888})
      message-data {:user_id user-id :telegram_id  8888888888 :text "Hello Friends"}]
    (create-or-update-message! message-data)
    (is (= (query db
                  ["SELECT user_id, telegram_id, text FROM messages WHERE telegram_id = ?" (get message-data :telegram_id)]
                  {:result-set-fn first})
           {:user_id user-id :telegram_id 8888888888 :text "Hello Friends"}))))

(deftest test-update-or-initialize-word-frequency!
  (testing "If the record did not exist, it creates it and initialises frequency"
    (let [user-id (create-or-update-user! {:username "Falla"  :telegram_id 8888888888})
          word-data {:user_id user-id :word "bloob" :frequency 5}]

      (update-or-initialize-word-frequency! word-data)

      (is (= (query db
                    ["SELECT word, user_id, frequency FROM words WHERE word = ?" (get word-data :word)]
                    {:result-set-fn first})
             {:word "bloob" :user_id user-id :frequency 5}))

      (testing "If the record already exists, update the existing frequency"
        (let [repeat-word-data {:user_id user-id :word "bloob" :frequency 3}]

          (update-or-initialize-word-frequency! repeat-word-data)

          (is (= (query db
                        ["SELECT word, user_id, frequency FROM words WHERE word = ?" (get word-data :word)]
                        {:result-set-fn first})
                 {:word "bloob" :user_id user-id :frequency 8})))))))

(deftest test-update-or-initialize-emoji-frequency!
  (testing "If the record did not exist, it creates it and initialises frequency"
    (let [user-id (create-or-update-user! {:username "Falla"  :telegram_id 8888888888})
          emoji-data {:user_id user-id :emoji "☺" :frequency 2}]

      (update-or-initialize-emoji-frequency! emoji-data)

      (is (= (query db
                    ["SELECT emoji, user_id, frequency FROM emojis WHERE emoji = ?" (get emoji-data :emoji)]
                    {:result-set-fn first})
             {:emoji "☺" :user_id user-id :frequency 2}))

      (testing "If the record already exists, update the existing frequency"
        (let [repeat-emoji-data {:user_id user-id :emoji "☺" :frequency 1}]

          (update-or-initialize-emoji-frequency! repeat-emoji-data)

          (is (= (query db
                        ["SELECT emoji, user_id, frequency FROM emojis WHERE emoji = ?" (get emoji-data :emoji)]
                        {:result-set-fn first})
                 {:emoji "☺" :user_id user-id :frequency 3})))))))
