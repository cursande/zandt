(ns zandt.transformer-test
  (:require [zandt.transformer :refer :all]
            [clojure.test :refer :all]))

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

(deftest test-message->words-and-frequencies
  (testing "It counts words, is case-insensitive and ignores emojis and punctuation"
    (let [message {:id 99999
                        :type "message"
                        :date "2018-03-10T17:45:50"
                        :edited "1970-01-01T10:00:00"
                        :from "Some one"
                        :from_id 12345
                        :text "And how and what. Why? ☺"}
          user-id 999]
      (is (= (message->words-and-frequencies message user-id)
             '({:word "and"
                :user_id 999
                :count 2}
               {:word "how"
                :user_id 999
                :count 1}
               {:word "what"
                :user_id 999
                :count 1}
               {:word "why"
                :user_id 999
                :count 1}))))))

(deftest test-message->emojis-and-frequencies
  (testing "It counts emojis, ignores all other words or characters"
    (let [message {:id 99999
                        :type "message"
                        :date "2018-03-10T17:45:50"
                        :edited "1970-01-01T10:00:00"
                        :from "Some one"
                        :from_id 12345
                        :text "And how and what. Why? ☺"}
          user-id 999]
      ;; https://lambdaisland.com/blog/2017-06-12-clojure-gotchas-surrogate-pairs
      ;; https://building.buildkite.com/updating-buildkite-for-emoji-4-0-e1c5d4b4583d
      (is (= (message->emojis-and-frequencies message user-id)
             '({:emoji "☺" :user_id 999 :count 1}))))))
