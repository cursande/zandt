(ns zandt.importer-test
  (:require [clojure.test :refer :all]
            [zandt.importer :refer [message->message-data]]))

;; TODO: Should I be trying to test more 'pure' functions, and only doing side-effecting
;; actions with the return values of these tested functions? Or should I build smaller private
;; namespace functions that I trust, and then either mock the db or build a test-connection to
;; test the main interface?
(deftest test-message->message-data
    (let [test-message {:id 99999
                        :type "message"
                        :date "2018-03-10T17:45:50"
                        :edited "1970-01-01T10:00:00"
                        :from "Some one"
                        :from_id 12345
                        :text "Random message"}]
      (is (= (message->message-data test-message)
             {:telegram_id 12345 :text "Random message"}))))

(deftest test-message->word-and-frequency
  (testing "It counts words, is case-insensitive and ignores emojis"
    (let [test-message {:id 99999
                        :type "message"
                        :date "2018-03-10T17:45:50"
                        :edited "1970-01-01T10:00:00"
                        :from "Some one"
                        :from_id 12345
                        :text "And how and what. Why? ☺️ "}]
      (is (= (message->word-and-frequency test-message)
             #{{:word "and" :count 2}
                {:word "how" :count 1}
                {:word "what" :count 1}
                {:word "why" :count 1}})))))
