(ns ftbot.views.articles
  (:use ftbot.views.templates)
  (:require [hiccup.page :as hp]
            [hiccup.core :as hc]))

(def table-headers
  (gen-tags :th
     "編集"
     "ID"
     "記事" "サムネイルURL" "自動配信対象" "FB配信する"
     "TW配信する"  "作成日" "更新日" ))

(def articles-list-page [:div {:ng-view "" }])

(def articles-list-view
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
   [:a {:href "#/{{selectedAccount.id}}/new"} "新規記事作成"]
   "&nbsp;&nbsp;"
   [:a {:href "/articles/all/csv?accountId={{selectedAccount.id}}"} "CSVでダウンロード"]

   "&nbsp;&nbsp;"
   [:input {:name "file" :ng-file-select "" :type "file" :size 40} ]
   [:button { :type "button" :size 40 :value "アップロード"
            :ng-click "fileUpload()"} ]

   [:table {:class "table table-striped"}
     [:thead [:tr table-headers]]
     [:tbody
       [:tr {:ng-repeat "each in articles"}
        [:td [:a {:href "#/{{selectedAccount.id}}/{{each.id}}/edit"} "編集"]]
        [:td "{{each.id}}"]
        [:td "{{each.body}}"]
        [:td "{{each.thumbnail_url}}"]
        [:td "{{each.can_auto_publish}}"]
        [:td "{{each.can_publish_fb}}"]
        [:td "{{each.can_publish_twitter}}"]
        [:td "{{each.updated_at | date : 'yyyy-MM-dd HH:mm:ss'}}"]
        [:td "{{each.created_at | date : 'yyyy-MM-dd HH:mm:ss'}}"]
        ]
      ]]]))

(def articles-edit-form
  (hc/html
     [:dl  [:dt "アカウント:"] [:dd "{{selectedAccount.account_name}}"]]
     [:form {:name "article_form" :class "well"}

      (required-textarea
        {:form-name "article_form"
         :model-name "article.body"
         :label "本文"
         :cols 20
         :rows 20
         :elem-name "body"
         :placeholder "本文を入力してください"
         :text ""
         })

      (input {
            :form-name "article_form"
            :model-name "article.thumbnail_url"
            :label "画像URL"
            :elem-name "thumbnail_url"
            :placeholder "画像URL"
            :equals-model-name "article.thumbnail_url"})

      (input
        {:form-name "article_form"
         :model-name "article.name"
         :label "画像名"
         :elem-name "name"
         :placeholder "画像ファイル名を入力してください"})

      (checkbox {
            :form-name "article_form"
            :model-name "article.is_image_post"
            :label "FBに画像として投稿する"
            :elem-name "is_image_post"
            })

      (input
        {:form-name "article_form"
         :model-name "article.caption"
         :label "画像キャプション(FB)"
         :ng-show "!article.is_image_post"
         :elem-name "caption"
         :placeholder "画像キャプションを入力してください"})

      (textarea
        {:form-name "article_form"
         :model-name "article.description"
         :ng-show "!article.is_image_post"
         :label "画像説明(FB)"
         :elem-name "description"
         :placeholder "画像説明を入力してください"})

      (checkbox {
            :form-name "article_form"
            :model-name "article.can_auto_publish"
            :label "自動配信を行う"
            :elem-name "can_auto_publish"
            })

      (checkbox {
            :form-name "article_form"
            :model-name "article.can_publish_fb"
            :label "Facebookに配信する"
            :elem-name "can_publish_fb"
            })

      (checkbox {
            :form-name "article_form"
            :model-name "article.can_publish_twitter"
            :label "Twitterに配信する"
            :elem-name "can_publish_twitter"
            })

      [:a {:href "#/" :class "btn"} "キャンセル"]
      [:button {:ng-click "save()"
                :ng-disabled "article_form.$invalid"
                :class "btn btn-primary" }
               "保存"]

      [:button {:ng-click "postfb()"
                :ng-disabled "account_form.$invalid"
                :ng-show "canPostFb"
                :class "btn btn-primary" }
               "今すぐFacebookへ投稿する"]

      [:button {:ng-click "tweet()"
                :ng-disabled "account_form.$invalid"
                :ng-show "canTweet"
                :class "btn btn-primary" }
               "今すぐTweetする"]


      [:button {:ng-click "remove()"
                :ng-show "removable"
                :class "btn btn-danger"}
               "この記事を削除"]

    ]
  ))

