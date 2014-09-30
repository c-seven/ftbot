(ns ftbot.routes.schedule
  (:use compojure.core)
  (:require [ftbot.views.templates :as template]
            [ftbot.views.schedules :as schedules-view]
            [ftbot.util :as util]
            [ftbot.models.schedules :as db]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [hiccup.page :as hp]
            [clojure.stacktrace :as st]
            ))

(defn schedule-list-page []
  (template/base
    "スケジュール一覧"
    (hp/include-js "/js/controllers/scheduleCtrl.js")
    schedules-view/schedules-list-page))

(defn schedule-view-list []
  schedules-view/schedules-list-view)

(defn schedule-entry-form []
  schedules-view/schedules-edit-form)

(def spans
  {
   "d" "毎日"
   "w_mon" "毎週月曜日"
   "w_tue" "毎週火曜日"
   "w_wed" "毎週水曜日"
   "w_thu" "毎週木曜日"
   "w_fri" "毎週金曜日"
   "w_sat" "毎週土曜日"
   "w_sun" "毎週日曜日"
  })

(defn time-format[x]
  (str (subs x 0 2) ":" (subs x 2)))

(defn load-schedules [accountId]
  (if (empty? accountId)
    ({:status 400})
    (json/generate-string
      (->
      (db/find-schedules accountId)
      ((fn [x]
        (map (fn[a]
               (update-in a [:schedule_span]
               spans)) x)))

      ((fn [x]
        (map (fn[a]
               (update-in a [:schedule_time]
               time-format)) x)))
      ))
  )
)

(defn create-schedule [schedule-stream]
  (try
    (-> (json/parse-stream
          (io/reader schedule-stream)
          true)
        (db/sanitize)
        (db/register-schedules)
        )
    {:status 200}
    (catch Exception e
      {:status 400 :body {:errmsgs [(str e)]}}
      )))

(defn update-schedule [id schedule-stream]
  (try
    (-> (json/parse-stream
          (io/reader schedule-stream)
          true)
        (db/sanitize)
        (db/update-schedule (util/str->number id) )
        )
    {:status 200}
    (catch Exception e
      {:status 400 :body {:errmsgs [(str e)]}}
      )))

(defn delete-schedule [id ]
  (try
    (db/remove-schedule (util/str->number id) )
    {:status 200}
    (catch Exception e
      {:status 400 :body {:errmsgs [(str e)]}}
      )))

(defn validate
  [data]
  (let [data (json/parse-stream (io/reader data) true)
        {:keys [orgschedule newschedule] } data
        ]
      (->
        (map vector
             [false]
             ["エラーメッセージhere"])
        ((fn [xs] (filter #(first %) xs)))
        ((fn [errs]
           (if (empty? errs)
             {:success true}
             (assoc {} :success false :errmsgs (map second errs))
             )))
        (#(assoc {} :body %))
        )
      ))

(defroutes schedule-routes
  (context "/schedules" []
           (GET "/" [] (schedule-list-page))
           (GET "/listform" [] (schedule-view-list))
           (GET "/entryform" [] (schedule-entry-form))
           (POST "/validate" {data :body} (validate data))
           (POST "/create" {schedule :body} (create-schedule schedule))
           (GET "/all" {{accountId :accountId} :params {callback :callback} :params}
                (util/as-json (load-schedules accountId) callback))

           (POST "/:id/update" {{id :id} :params schedule :body} (update-schedule id schedule))
           (GET "/:id" {{id :id} :params {callback :callback} :params}
                (if-let [schedule  (db/find-by-id id)]
                  (util/as-json {:schedule schedule} callback)
                  {:status 400}
                  ))
           (DELETE "/:id" [id]  (delete-schedule id))
           ))

