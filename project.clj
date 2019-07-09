(defproject zandt "0.1.0-SNAPSHOT"
  :description "For processing, analysing and presenting Telegram export data."
  :url "https://github.com/cursande/zandt"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.xerial/sqlite-jdbc "3.23.1"]
                 [cheshire "5.8.0"]]
  :main zandt.importer
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
