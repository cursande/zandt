(ns zandt.transformer
  (:require [clojure.string :refer [lower-case]]))

(defn chat->user-data [message]
  "Returns a map containing user data"
  {:telegram_id (:id message)
   :username (:name message)})

(defn message->message-data [message user-id]
  "Returns a map containing message data"
  {:telegram_id (:id message)
   :user_id user-id
   :text (:text message)})

(defn message->words-and-frequencies [message user-id]
  "returns a sequence of maps with each word (case insensitive), the user id and the word count"
  (let [words (re-seq #"\w+" (-> message
                                 (get :text)
                                 (lower-case)))
        word-frequencies (frequencies words)
        telegram_id (:from_id message)]
    (map (fn [[word count]] {:word word
                             :user_id user-id
                             :count count})
         word-frequencies)))

(defn message->emojis-and-frequencies [message user-id]
  "returns a sequence of maps with each emoji, the user id and the emoji count"
  (let [emoji-matching-regex (re-pattern (str "\u00a9|"
                                              "\u00ae|"
                                              "[\u2000-\u3300]|"
                                              "\ud83c[\ud000-\udfff]|"
                                              "\ud83d[\ud000-\udfff]|"
                                              "\ud83e[\ud000-\udfff]+"))
        emojis (re-seq emoji-matching-regex (get message :text))
        emoji-frequencies (frequencies emojis)
        telegram_id (:from_id message)]
    (map (fn [[emoji count]] {:emoji emoji
                              :user_id user-id
                              :count count})
         emoji-frequencies)))
