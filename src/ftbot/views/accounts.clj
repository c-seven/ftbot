(ns ftbot.views.accounts
  (:use ftbot.views.templates)
  (:require [hiccup.page :as hp]
            [hiccup.core :as hc]))

(def table-headers
  (gen-tags :th
     "編集"
     "ID" "アカウント名"
     "FB APP ID" "FBシークレット" "FBトークン"
     "TWユーザ名" "TWパスワード" "TWトークン"
     "作成日" "更新日" ))

(def accounts-list-page [:div {:ng-view "" }])

(defn token-view
  [token]

  [:div {:class "well"}
  [:label "下記のトークンをアカウント情報に入力してください"]
   [:input {:type "text" :value token}]
   ]
  )

(def accounts-list-view
  (hc/html
 [:div
   [:a {:href "#/new"} "新規作成" ]
   [:table {:class "table table-striped"}
     [:thead [:tr table-headers]]
     [:tbody
       [:tr {:ng-repeat "each in accounts"}
        [:td [:a {:href "#/{{each.id}}/edit"} "編集"]]
        [:td "{{each.id}}"]
        [:td "{{each.account_name}}"]
        [:td "{{each.fb_app_id}}"]
        [:td "********"]
        [:td "********"]
        [:td "{{each.twitter_username}}"]
        [:td "********"]
        [:td "********"]
        [:td "{{each.updated_at | date : 'yyyy-MM-dd HH:mm:ss'}}"]
        [:td "{{each.created_at | date : 'yyyy-MM-dd HH:mm:ss'}}"]
        ]
      ]]]))

(def accounts-edit-form
  (hc/html
     [:form {:name "account_form" :class "well"}
      (required-input
        {:form-name "account_form"
         :model-name "account.account_name"
         :label "アカウント名(ページ名など)"
         :elem-name "account_name"
         :placeholder "ページ名やブログ名を付けてください"})

      (required-input
        {:form-name "account_form"
         :model-name "account.fb_app_id"
         :label "FB App ID"
         :elem-name "fb_app_id"
         :placeholder "FacebookのAppIDを入力してください"})

      (password-input {
            :form-name "account_form"
            :model-name "account.fb_secret_id"
            :label "FBシークレット"
            :elem-name "fb_secret_id"
            :placeholder "FacebookのApp secretを入力してください"
            })

      (required-input
        {:form-name "account_form"
         :model-name "account.fb_page_id"
         :label "FB ページID"
         :elem-name "fb_page_id"
         :placeholder "FacebookのAppIDを入力してください"})

      [:button {
           :ng-click "openFbAuthPage()"
              :novalidate ""
               }
               "トークンを取得する"]
      (required-input
        {:form-name "account_form"
         :model-name "account.fb_token"
         :label "FBユーザトークン"
         :elem-name "fb_token"
         :placeholder "Facebookのユーザアクセストークン入力してください"})

      (required-input
        {:form-name "account_form"
         :model-name "account.twitter_api_key"
         :label "ツイッターAPIキー"
         :elem-name "twitter_api_key"
         :placeholder "ツイッターのAPI Keyを入力してください"})

      (required-input
        {:form-name "account_form"
         :model-name "account.twitter_api_secret"
         :label "ツイッターAPIシークレット"
         :elem-name "twitter_api_secret"
         :placeholder "ツイッターのAPI Secretを入力してください"})

      (required-input
        {:form-name "account_form"
         :model-name "account.twitter_token"
         :label "ツイッタートークン"
         :elem-name "twitter_token"
         :placeholder "ツイッターのアクセストークンを入力してください"})

      (required-input
        {:form-name "account_form"
         :model-name "account.twitter_token_secret"
         :label "ツイッタートークンシークレット"
         :elem-name "twitter_token_secret"
         :placeholder "ツイッターのアクセストークンシークレットを入力してください"})

      [:a {:href "#/" :class "btn"} "キャンセル"]
      [:button {:ng-click "save()"
                :ng-disabled "account_form.$invalid"
                :class "btn btn-primary" }
               "保存"]

      [:button {:ng-click "remove()"
                :ng-show "removable"
                :class "btn btn-danger"}
               "このアカウントを削除"]

    ]
  ))

