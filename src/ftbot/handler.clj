(ns ftbot.handler
  (:require [compojure.core :refer [defroutes]]
            [ftbot.routes.root :refer [root-routes]]
            [ftbot.routes.account :refer [account-routes]]
            [ftbot.routes.article :refer [article-routes]]
            [ftbot.routes.schedule :refer [schedule-routes]]
            [ftbot.middleware :as middleware]
            [noir.util.middleware :refer [app-handler]]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [ftbot.models.schema :as schema]
            [ftbot.models.schedules :as sch]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.cron
               :refer [schedule cron-schedule]]
            ))

(defjob PublishJob
  [ctx]
  (sch/publish-articles))

(defn setup-publish-scheduler
  []
  (let [job (j/build
              (j/of-type PublishJob)
              (j/with-identity (j/key "jobs.pub.1")))
        trigger (t/build
                  (t/with-identity (t/key "trigger.1"))
                  (t/start-now)
                  (t/with-schedule
                    (schedule
                      (cron-schedule "*/3 * * ? * *"))))
        ]

  (qs/schedule job trigger)
  ))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "<h1>ページが見つかりませんでした。</h1><a href=\"/\">トップへ戻る</a>"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/appender-fn})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "ftbot.log" :max-size (* 512 1024) :backlog 10})

  (if (env :dev) (parser/cache-off!))
  (if-not (schema/initialized?) (schema/create-tables))
  (timbre/info "ftbot started successfully")
  (qs/initialize)
  (qs/start)
  (setup-publish-scheduler)
  )

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (qs/shutdown)
  (timbre/info "ftbot is shutting down..."))

(def app (app-handler
           [root-routes
            account-routes
            article-routes
            schedule-routes
            app-routes]
           ;; add custom middleware here
           :middleware [middleware/template-error-page
                        middleware/log-request]
           ;; add access rules here
           :access-rules []
           ;; serialize/deserialize the following data formats
           ;; available formats:
           ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
           :formats [:json-kw :edn]))

