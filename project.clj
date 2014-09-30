(defproject ftbot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [lib-noir "0.8.3"]
                 [ring-server "0.3.1"]
                 [selmer "0.6.6"]
                 [com.taoensso/timbre "3.2.1"]
                 [com.taoensso/tower "2.0.2"]
                 [markdown-clj "0.9.44"]
                 [org.clojure/core.cache "0.6.3"]
                 [environ "0.5.0"]
                 [hiccup "1.0.5"]
                 [com.h2database/h2 "1.3.175"]
                 [com.taoensso/timbre "3.1.6"]
                 [com.taoensso/tower "2.0.2"]
                 [log4j
                  "1.2.17"
                  :exclusions
                  [javax.mail/mail
                   javax.jms/jms
                   com.sun.jdmk/jmxtools
                   com.sun.jmx/jmxri]]
                 [korma "0.3.1"]
                 [blackwater "0.0.9"]
                 [compojure "1.1.6"]
                 [cheshire "5.3.1"]
                 [selmer "0.6.6"]
                 [lib-noir "0.8.2"]
                 [clojurewerkz/quartzite "1.2.0"]
                 [clj-time "0.7.0"]
                 [org.facebook4j/facebook4j-core "2.1.0"]
                 [org.twitter4j/twitter4j-core "4.0.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 ]
  :repl-options {:init-ns ftbot.repl}
  :plugins [[lein-ring "0.8.10"]
            [lein-environ "0.5.0"]]
  :ring {:handler ftbot.handler/app
         :init    ftbot.handler/init
         :destroy ftbot.handler/destroy}
  :aot :all
  :profiles
  {:uberjar {:aot :all}
   :production {:ring {:open-browser? true
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.2.2"]]
         :env {:dev true}}}
  :min-lein-version "2.0.0")
