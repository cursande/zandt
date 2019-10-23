(ns zandt.transformer
  (:require [clojure.string :refer [lower-case]]))

(defn message->user-data [message]
  "Returns a map containing user data extracted from the message"
  {:telegram_id (:from_id message)
   :username (:from message)})

(defn message->message-data [message user-id]
  "Returns a map containing message data"
  {:telegram_id (:id message)
   :user_id user-id
   :text (:text message)})

(defn message->words-and-frequencies [message message-id user-id]
  "returns a sequence of maps with each word (case insensitive and can include dashes or apostrophes),
   the user id and the word count."
  (let [words (re-seq #"\b[a-z]+(?:['-]?[a-z]+)*\b" (-> message
                                                        (get :text)
                                                        (lower-case)))
        word-frequencies (frequencies words)
        telegram_id (:from_id message)]
    (map (fn [[word count]] {:word word
                             :message_id message-id
                             :user_id user-id
                             :frequency count})
         word-frequencies)))

(defn message->emojis-and-frequencies [message message-id user-id]
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
                              :message_id message-id
                              :user_id user-id
                              :frequency count})
         emoji-frequencies)))
