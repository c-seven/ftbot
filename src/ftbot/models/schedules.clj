(ns ftbot.models.schedules
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [ftbot.models.schema :as schema]
            [ftbot.models.articles :as articles-entity]
            [ftbot.util :as util]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clj-time.local :as l]
            )
  )

(defdb db schema/db-spec)

(defentity publish_schedules)

(defn sanitize [maybe-dirty-schedule]
    (select-keys
        maybe-dirty-schedule
        [
        :id
        :account_id
        :schedule_span
        :schedule_time
        :available
        ]
))

(defn find-schedules [account-id]
  (select publish_schedules
      (where {:account_id account-id})
      (order :id)))

(defn find-by-id [id]
  (first
  (select publish_schedules (where {:id id}))))

(defn find-by [condition]
  (select publish_schedules
      (where condition)))

(defn calc-next-daily-schedule
  [base-datetime h m]
  (let [[b-h b-m] ((juxt t/hour t/minute) base-datetime)
        [year mon day] ((juxt t/year t/month t/day) base-datetime)
        base-ymdhm (t/date-time year mon day b-h b-m)
        candidate-ymdhm  (t/date-time year mon day h m)
       ]
    (if (t/after?
          candidate-ymdhm
          base-ymdhm)
        candidate-ymdhm
        (t/plus candidate-ymdhm (t/days 1))
        )
  ))

(def day-of-weeks
  {
  :mon 1
  :tue 2
  :wed 3
  :thu 4
  :fri 5
  :sat 6
  :sun 7
})

(defn get-next-week-day
  [base-date dow-num]
  (let [base-dow (t/day-of-week base-date)]
    (t/plus base-date
            (t/days
                 (+ (- dow-num base-dow )
                    (if (<= dow-num base-dow)
                      7 0
                    ))))
    )
  )

(defn calc-next-weekly-schedule
  [base-datetime h m w]
  (let [[b-h b-m] ((juxt t/hour t/minute) base-datetime)
        [year mon day] ((juxt t/year t/month t/day) base-datetime)
        base-ymdhm (t/date-time year mon day h m)
       ]

      (get-next-week-day base-ymdhm (w day-of-weeks))
    )
  )

(defn get-next-schedule-time
  [base-datetime schedule]
  (let [span (:schedule_span schedule)
        [h m] (vec (map util/str->int ((juxt #(subs % 0 2)
                     #(subs % 2)) (:schedule_time schedule))))
        ]
     (case span
         "d" (calc-next-daily-schedule base-datetime h m)
         "w_mon" (calc-next-daily-schedule base-datetime h m :mon)
         "w_tue" (calc-next-weekly-schedule base-datetime h m :tue)
         "w_wed" (calc-next-weekly-schedule base-datetime h m :wed)
         "w_thu" (calc-next-weekly-schedule base-datetime h m :thu)
         "w_fri" (calc-next-weekly-schedule base-datetime h m :fri)
         "w_sat" (calc-next-weekly-schedule base-datetime h m :sat)
         "w_sun" (calc-next-weekly-schedule base-datetime h m :sun)
    )
   ))

(defn get-next-publish-schedule
  [schedule]
  (if (:available schedule)
    (let [
          account-id (:account_id schedule)
          article-id
            (articles-entity/get-random-publish-article-id account-id)
          next-time (get-next-schedule-time (l/local-now) schedule)
          ]
      (when article-id
          {:next_scheduled_article_id article-id
           :next_schedule_time
           (tc/to-timestamp (l/to-local-date-time next-time))
           }
          )
      )
    {}
    ))

(defn update-schedule
  [schedule id]
  (update publish_schedules
      (set-fields
        (merge
          (assoc schedule :updated_at (util/now))
          (get-next-publish-schedule schedule))
        )
      (where {:id id}))
  )

(defn register-schedules
  [schedule]
   (insert publish_schedules
          (values
            (merge
              (assoc schedule
                :updated_at (util/now)
                :created_at (util/now))
              (get-next-publish-schedule schedule)
              ))
      )
  )


(defn publish-a-article
  [schedule]
  (future (articles-entity/publish schedule))
  (future
    (update-schedule schedule (:id schedule)))
  )

(defn publish-articles
  []
  (let [schedules
          (select publish_schedules
                  (where {:available true
                     :next_schedule_time [<= (sqlfn now)]
                     }))
       ]
    (doseq [each schedules]
      (publish-a-article each))
  ))

(defn remove-schedule
  [id]
  (delete publish_schedules
    (where {:id id})))

(defn remove-all-schedules
  [account-id]
  (delete publish_schedules
    (where {:account_id account-id})))


