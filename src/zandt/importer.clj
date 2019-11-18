(ns zandt.importer
  (:require [cheshire.core :refer [parse-string]]
            [zandt.transformer :refer [telegram-user->user-data
                                       message->message-data
                                       message->word-data
                                       message->emoji-data
                                       words-and-emojis-by-user]]
            [zandt.repository :refer [create-zandt-db
                                      create-or-update-user!
                                      create-or-update-message!
                                      update-or-initialize-word-frequency!
                                      update-or-initialize-emoji-frequency!]]
            [zandt.sqlite :refer [db
                                  establish-sqlite-connection
                                  close-sqlite-connection]]
            [clojure.java.jdbc :refer :all]))

(defn json-string->data-map [export-file-path]
  (parse-string (slurp export-file-path) true))

(defn find-user-id-from-users-map
  "Searches keys from a user-map containing the `from_id` for each user, with the db primary
   key as the value. If not found, create the new key and create the user in the db.
   In either case, returns the primary key for the user."
  [from-id from users-map]
  (if (contains? @users-map from-id)
    (get @users-map from-id)
    (let [user-id (create-or-update-user! (telegram-user->user-data from-id from))]
      (swap! users-map assoc from-id user-id)
      user-id)))

(defn import-messages
  "Munges and inserts data from each chat"
  [chat users-map]
  (let [messages  (:messages chat)
        chat-map  (words-and-emojis-by-user messages)
        users     (keys chat-map)]
    (doseq [user users]
      (let [user-name (-> chat-map (get user) :name)
            user-id   (find-user-id-from-users-map user user-name users-map)
            words     (-> chat-map (get user) :words)
            emojis    (-> chat-map (get user) :emojis)]
        (doseq [message messages]
          (create-or-update-message! (message->message-data message user-id)))
        (doseq [word words]
          (update-or-initialize-word-frequency! (message->word-data word user-id)))
        (doseq [emoji emojis]
          (update-or-initialize-emoji-frequency! (message->emoji-data emoji user-id)))
        (prn-str "imported words and emojis for user " user)))))

(defn import-chats
  "Munges and inserts data into persistent data store"
  [data-map]
  (let [chats     (-> data-map :chats :list)
        users-map (atom {})]
    (doseq [chat chats] (import-messages chat users-map))
    ;; (shutdown-agents)
    (println "Successfully imported!")))

(defn -main [path]
  (let [export-data-map (json-string->data-map path)]
    (establish-sqlite-connection)
    (create-zandt-db)
    (import-chats export-data-map)
    (close-sqlite-connection)))
