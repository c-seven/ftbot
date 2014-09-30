(ns ftbot.views.schedules
  (:use ftbot.views.templates)
  (:require [hiccup.page :as hp]
            [hiccup.core :as hc]))

(def table-headers
  (gen-tags :th
     "編集"
     "ID"
     "スケジュールスパン" "スケジュール時刻"
     "次回配信予定日" "配信予定記事ID"
     "有効" "作成日" "更新日" ))

(def schedules-list-page [:div {:ng-view "" }])

(def schedules-list-view
  (hc/html
  [:div
   [:label "アカウントを選択してください"]
   [:select {:ng-model "selectedAccount"
             :required "true"
             :ng-init 0
             :ng-options "x as x.value for x in accountsList"
             }
    ]
   ]
 [:div
   [:a {:href "#/{{selectedAccount.id}}/new"} "新規スケジュール作成"]
   [:table {:class "table table-striped"}
     [:thead [:tr table-headers]]
     [:tbody
       [:tr {:ng-repeat "each in schedules"}
        [:td [:a {:href "#/{{selectedAccount.id}}/{{each.id}}/edit"} "編集"]]
        [:td "{{each.id}}"]
        [:td "{{each.schedule_span}}"]
        [:td "{{each.schedule_time}}"]
        [:td "{{each.next_schedule_time | date : 'yyyy-MM-dd HH:mm:ss'}}"]
        [:td "{{each.next_scheduled_article_id}}"]
        [:td "{{each.available}}"]
        [:td "{{each.updated_at | date : 'yyyy-MM-dd HH:mm:ss'}}"]
        [:td "{{each.created_at | date : 'yyyy-MM-dd HH:mm:ss'}}"]
        ]
      ]]]))

(def schedules-edit-form
  (hc/html
     [:dl [:dt "アカウント:"] [:dd "{{selectedAccount.account_name}}"]]
     [:form {:name "schedule_form" :class "well"}

      (required-combo-box {
            :form-name "schedule_form"
            :model-name "schedule.span"
            :label "配信スパン"
            :elem-name "span"
            :option-model "spanList"
            })

      (time-input {
            :form-name "schedule_form"
            :model-name "schedule.time"
            :label "配信時刻"
            :elem-name "time"
            :placeholder "配信時刻を入力してください(例. 12:00)"})

      (checkbox {
            :form-name "schedule_form"
            :model-name "schedule.available"
            :label "有効"
            :elem-name "can_publish_twitter"
            })

      [:a {:href "#/" :class "btn"} "キャンセル"]
      [:button {:ng-click "save()"
                :ng-disabled "schedule_form.$invalid"
                :class "btn btn-primary" }
               "保存"]

      [:button {:ng-click "remove()"
                :ng-show "removable"
                :class "btn btn-danger"}
               "このスケジュールを削除"]
    ]
  ))

