(ns zandt.importer-test
  (:require [clojure.test :refer :all]
            [zandt.importer :refer [message->message-data
                                    message->word-and-frequency
                                    message->emoji-and-frequency]]))

;; TODO: Should I be trying to test more 'pure' functions, and only doing side-effecting
;; actions with the return values of these tested functions? Or should I build smaller private
;; namespace functions that I trust, and then either mock the db or build a test-connection to
;; test the main interface?
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

(deftest test-message->word-and-frequency
  (testing "It counts words, is case-insensitive and ignores emojis and punctuation"
    (let [message {:id 99999
                        :type "message"
                        :date "2018-03-10T17:45:50"
                        :edited "1970-01-01T10:00:00"
                        :from "Some one"
                        :from_id 12345
                        :text "And how and what. Why? ☺"}
          user-id 999]
      (is (= (message->word-and-frequency message user-id)
             '({
                :word "and"
                :user_id 999
                :count 2
                }
               {
                :word "how"
                :user_id 999
                :count 1
                }
               {
                :word "what"
                :user_id 999
                :count 1
                }
               {
                :word "why"
                :user_id 999
                :count 1
                }))))))

(deftest test-message->emoji-and-frequency
  (testing "It counts emojis, ignores all other words or characters"
    (let [message {:id 99999
                        :type "message"
                        :date "2018-03-10T17:45:50"
                        :edited "1970-01-01T10:00:00"
                        :from "Some one"
                        :from_id 12345
                        :text "And how and what. Why? ☺"}
          user-id 999]
      ;; TODO: Unicode hell: I *think* the problem is where emoji are encoded as two
      ;; or more characters (surrogate pairs). Looks like
      ;; Clojure shouldn't have an issue though so I'm confused.
      ;; https://lambdaisland.com/blog/2017-06-12-clojure-gotchas-surrogate-pairs
      ;; https://building.buildkite.com/updating-buildkite-for-emoji-4-0-e1c5d4b4583d
      ;; For time being, will just manually enter new emojis into regex-matcher
      (is (= (message->emoji-and-frequency message user-id)
             '({:emoji "☺" :user_id 999 :count 1}))))))
