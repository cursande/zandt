(ns zandt.test-utils
  (:require  [clojure.test :refer :all]
             [zandt.repository :refer [create-zandt-db]]
             [zandt.sqlite :refer [sqlite-db-spec
                                  test-sqlite-db-spec
                                  establish-sqlite-connection
                                  close-sqlite-connection]]))

(defn with-test-db [tests]
  (with-redefs [sqlite-db-spec test-sqlite-db-spec])
  (prn "### Setting up test sqlite db ###")
  (establish-sqlite-connection)
  (create-zandt-db)
  (tests)
  (prn "### tearing down sqlite db ###")
  (close-sqlite-connection))
