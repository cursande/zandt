(ns zandt.importer
  (:require [cheshire.core :refer [parse-string]]
            [zandt.transformer :refer :all]
            [zandt.repository :refer :all]
            [zandt.sqlite :refer [establish-sqlite-connection
                                  close-sqlite-connection]]))

(defn json-string->data-map [export-file-path]
  (parse-string (slurp export-file-path) true))

(defn find-user-id-from-users-map [message users-map]
  "Searches keys from a user-map containing the `from_id` for each user, with the db primary
   key as the value. If not found, create the new key and create the user in the db.
   In either case, returns the primary key for the user."
  (let [from-id (:from_id message)]
    (if (contains? @users-map from-id)
      (get @users-map from-id)
      (let [user-id (create-or-update-user! (message->user-data message))]
        (do (swap! users-map assoc from-id user-id)
            user-id)))))

(defn message-is-not-text? [message]
  "Checks the message and ensures it doesn't contain a link, sticker etc. Non-text messages
   will contain at least one vector containing a map with a `:type` key."
  (let [text (:text message)]
    (or (coll? text)
        (contains? text :type))))

(defn import-messages [chat]
  "Munges and inserts data from each chat"
  (let [messages  (:messages chat)
        users-map (atom {})]
    (doseq [message messages]
      (if message-is-not-text? message)
      nil
      (let [user-id    (find-user-id-from-users-map message users-map)
            message-id (create-or-update-message! (message->message-data message user-id))
            words      (message->words-and-frequencies message message-id user-id)
            emojis     (message->emojis-and-frequencies message message-id user-id)]
        (doseq [word words]
          (update-or-initialize-word-frequency! word))
        (doseq [emoji emojis]
          (update-or-initialize-emoji-frequency! emoji))))))

(defn import-chats [data-map]
  "Munges and inserts data into persistent data store"
  (let [chats (get-in data-map '(:chats :list))]
      (doseq [chat chats] (import-messages chat)))
      (println "Successfully imported!"))

(defn -main [path]
  (let [export-data-map (json-string->data-map path)]
    (establish-sqlite-connection)
    (create-zandt-db)
    (import-chats export-data-map)
    (close-sqlite-connection)))
