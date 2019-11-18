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
        user-id  999]
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
                   :text "And how and what. Why? ☺"}]
      (is (= {"and" 2, "how" 1, "what" 1, "why" 1}
             (message->words-and-frequencies message))))))

  (testing "It counts words with apostrophes in them as a distinct word"
    (let [message {:id 99999
                   :type "message"
                   :date "2018-03-10T17:45:50"
                   :edited "1970-01-01T10:00:00"
                   :from "Some one"
                   :from_id 12345
                   :text "How's that?"}]
      (is (= (message->words-and-frequencies message)
             '({:word "how's"
                :message_id 1234
                :user_id 999
                :frequency 1}
               {:word "that"
                :message_id 1234
                :user_id 999
                :frequency 1})))))

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
      (is (= (message->emojis-and-frequencies message)
             {"☺" 1}))))
  (testing "It ignores backticks as emoji"
    (let [message {:id 99999
                   :type "message"
                   :date "2018-03-10T17:45:50"
                   :edited "1970-01-01T10:00:00"
                   :from "Some one"
                   :from_id 12345
                   :text "And how`s that go again?"}]
      (is (empty? (message->emojis-and-frequencies message))))))

(deftest test-words-and-emojis-by-user
  (testing "It builds a single map per user for all words and their frequencies in a chat"
    (let [chat-messages     [{:id 22222
                              :type "message"
                              :date "2018-03-10T17:45:50"
                              :edited "1970-01-01T10:00:00"
                              :from "Person One"
                              :from_id 2222
                              :text "What about it? 👌🐱"}
                             {:id 33333
                              :type "message"
                              :date "2018-03-10T17:46:50"
                              :edited "1970-01-01T10:00:00"
                              :from "Person Two"
                              :from_id 4444
                              :text "I like it. ☺"}
                             {:id 44444
                              :type "message"
                              :date "2018-03-10T17:47:50"
                              :edited "1970-01-01T10:00:00"
                              :from "Person One"
                              :from_id 2222
                              :text " What about that!🐱"}]]

      (is (= {2222 {:name "Person One"
                      :words  {"what"  2
                               "about" 2
                               "it"    1
                               "that"  1}
                      :emojis {}}
              4444 {:name "Person Two"
                      :words  {"i"    1
                               "like" 1
                               "it"   1}
                      :emojis {"☺" 1}}}
             (words-and-emojis-by-user chat-messages))))))
