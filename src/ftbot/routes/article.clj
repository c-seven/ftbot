(ns ftbot.routes.article
  (:use compojure.core)
  (:require [ftbot.views.templates :as template]
            [ftbot.views.articles :as articles-view]
            [ftbot.util :as util]
            [ftbot.models.articles :as db]
            [ftbot.models.accounts :as acc-db]
            [ftbot.models.facebook :as fb]
            [ftbot.models.twitter :as tw]
            [ring.util.io :as rio ]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [hiccup.page :as hp]
            [clojure-csv.core :as csv]
            [noir.response :as rs]
            ))

(defn article-list-page []
  (template/base
    "記事一覧"
    (hp/include-js "/js/controllers/articleCtrl.js")
    articles-view/articles-list-page))

(defn article-view-list []
  articles-view/articles-list-view)

(defn article-entry-form []
  articles-view/articles-edit-form)

(def csv-header
  [
    [:body "本文"]
    [:thumbnail_url "サムネイルURL"]
    [:name "画像タイトル"]
    [:caption "画像キャプション"]
    [:description "画像説明" ]
    [:is_image_post "FBに画像としてポスト"]
    [:can_publish_fb "FBに配信する"]
    [:can_publish_twitter "TWに配信する"]
    [:can_auto_publish  "自動配信する"]
    [:account_name  "アカウント名"]
    ]
  )

(defn download-csv [account-id]
  (let [
        account (acc-db/find-by-id account-id)
        as (map
            (fn [x]
               (->>
                  (map x (map first csv-header))
                  (map (fn [s] (str s)))))

             (map #(assoc % :account_name (:account_name account))
                    (db/find-all-articles account-id)))
        ]

  (rio/piped-input-stream
    (fn [os]
      (spit os
        (csv/write-csv
          (cons (map second csv-header)
                as)))
    )
    )
))

(defn load-articles [accountId]
  (if (empty? accountId)
    ({:status 400})
    (json/generate-string
      (->
      (db/find-all-articles accountId)
      ((fn [x]
        (map (fn [a]
               (update-in a [:body]
                 #(util/excerpt % 10))

               (update-in a [:thumbnail_url]
                 #(util/excerpt (or % "") 15))
               ) x)))
      ))
  )
)

(defn create-article [article-stream]
  (try
    (-> (json/parse-stream
          (io/reader article-stream)
          true)
        (db/sanitize)
        (db/register-articles))
    {:status 200}
    (catch Exception e
      {:status 400 :body {:errmsgs [(str e)]}}
      )))

(defn update-article [id article-stream]
  (try
    (-> (json/parse-stream
          (io/reader article-stream)
          true)
        (db/sanitize)
        (db/update-article (util/str->number id) )
        )
    {:status 200}
    (catch Exception e
      {:status 400 :body {:errmsgs [(str e)]}}
      )))

(defn delete-article [id ]
  (try
    (db/remove-article (util/str->number id) )
    {:status 200}
    (catch Exception e
      {:status 400 :body {:errmsgs [(str e)]}}
      )))

(defn post-fb
  [data]
  (let [data (json/parse-stream (io/reader data) true)
        article (:article data )
        ]
      (fb/post-fb article)
      {:status 200}
      ))

(defn tweet
  [data]
  (let [data (json/parse-stream (io/reader data) true)
        article (:article data )
        ]
      (tw/tweet article)
      {:status 200}
      ))

(defn validate
  [data]
  (let [data (json/parse-stream (io/reader data) true)
        {:keys [orgArticle newArticle] } data
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

(defroutes article-routes
  (context "/articles" []
           (GET "/" [] (article-list-page))
           (GET "/listform" [] (article-view-list))
           (GET "/entryform" [] (article-entry-form))
           (GET "/all/csv" [accountId]
                {:status 200
                 :headers {"Content-Type" "application/octet-stream"
                            "charset" "MS932"
                            "Content-Disposition" "attachement;filename=articles.csv"}
                 :body (download-csv accountId)

                 }
                )
           (GET "/all" {{accountId :accountId} :params {callback :callback} :params}
                (util/as-json (load-articles accountId) callback))

           (GET "/:id" {{id :id} :params {callback :callback} :params}
                (if-let [article (db/find-by-id id)]
                  (util/as-json {:article article} callback)
                  {:status 400}
                  ))

           (POST "/fbpost" {data :body} (post-fb data))
           (POST "/tweet" {data :body} (tweet data))
           (POST "/validate" {data :body} (validate data))
           (POST "/create" {article :body} (create-article article))
           (POST "/:id/update" {{id :id} :params article :body} (update-article id article))
           (DELETE "/:id" [id]  (delete-article id))
           ))

