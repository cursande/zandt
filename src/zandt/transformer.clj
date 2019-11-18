(ns zandt.transformer
  (:require [clojure.string :refer [lower-case]]))

(defn telegram-user->user-data
  "Returns a map containing user data"
  [user-id user-name]
  {:telegram_id user-id
   :username user-name})

(defn message->message-data
  "Returns a map containing message data"
  [message user-id]
  {:telegram_id (:id message)
   :user_id user-id
   :text (:text message)})

(defn message->word-data
  "Returns a map containing word data"
  [[word frequency] user-id]
  {:word word
   :user_id user-id
   :frequency frequency})

(defn message->emoji-data
  "Returns a map containing emoji data"
  [[emoji frequency] user-id]
  {:emoji emoji
   :user_id user-id
   :frequency frequency})

(defn message->words-and-frequencies
  "returns a map with each distinct `word`(case insensitive and can include dashes or apostrophes)
   as a key, and the occurrence in the message as value."
  [message]
  (let [words (re-seq #"\b[a-z]+(?:['-]?[a-z]+)*\b" (-> message
                                                        (get :text)
                                                        (lower-case)))]
    (frequencies words)))

(defn message->emojis-and-frequencies
  "returns a map with each distinct `word`(case insensitive and can include dashes or apostrophes)
   as a key, and the occurrence in the message as value."
  [message]
  (let [emoji-matching-regex (re-pattern (str "^`"
                                              "\u00a9|"
                                              "\u00ae|"
                                              "[\u2000-\u3300]|"
                                              "\ud83c[\ud000-\udfff]|"
                                              "\ud83d[\ud000-\udfff]|"
                                              "\ud83e[\ud000-\udfff]+"))
        emojis (re-seq emoji-matching-regex (get message :text))]
    (frequencies emojis)))


(defn no-user-id? [message]
  (nil? (:from_id message)))

(defn message-is-not-text?
  "Checks the message and ensures it doesn't contain a link, sticker etc. Non-text messages
   seem to contain at least one vector, with a map containing a `:type` key."
  [message]
  (let [text (:text message)]
    (or (not (= (:type message) "message"))
        (coll? text))))

(defn message-can-be-stored?
  [message]
  (not (or (no-user-id? message)
           (message-is-not-text? message))))

(defn words-and-emojis-by-user
  "Returns a map, with the user's from_id as key, with the value being another map containing:
     - The name of the user
     - All words used in the chat, with their frequency
     - All emojis used in the chat, with their frequency

   Messages that cannot be processed yet will be filtered out."
  [messages]
  (reduce (fn [chat-map message]
            (if (message-can-be-stored? message)
              (let [from-id                (:from_id message)
                    words-and-frequencies  (message->words-and-frequencies message)
                    emojis-and-frequencies (message->emojis-and-frequencies message)
                    current-words          (-> chat-map (get from-id) :words)
                    current-emojis         (-> chat-map (get from-id) :emojis)]
                (assoc chat-map
                       from-id
                       {:name   (:from message)
                        :words  (merge-with + words-and-frequencies current-words)
                        :emojis (merge-with + emojis-and-frequencies current-emojis)}))
              chat-map))
          {}
          messages))
