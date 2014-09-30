(ns ftbot.models.articles
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [ftbot.models.schema :as schema]
            [ftbot.util :as util]
            [ftbot.models.facebook :as fb]
            [ftbot.models.twitter :as tw]
            )
  )

(defdb db schema/db-spec)

(defentity articles)

(defn sanitize [maybe-dirty-article]
    (select-keys
        maybe-dirty-article
        [
        :id
        :body
        :thumbnail_url
        :caption
        :name
        :description
        :is_image_post
        :can_publish_fb
        :can_publish_twitter
        :can_auto_publish
        :account_id
        ]
))

(defn find-all-articles [account-id]
  (select articles
      (where {:account_id account-id})
      (order :id)))

(defn find-by-id [id]
  (first (select articles (where {:id id}))))

(defn find-by [condition]
  (select articles
      (where condition)))

(defn register-articles
  [article]
  (insert articles
      (values
        (assoc article
          :updated_at (util/now)
          :created_at (util/now))
        )))

(defn update-article
  [article id]
  (update articles
      (set-fields
        (assoc article :updated_at (util/now)))
      (where {:id id})
      ))

(defn remove-article
  [id]
  (delete articles
    (where {:id id})))

(defn remove-all-articles
  [account-id]
  (delete articles
    (where {:account_id account-id})))

(defn get-random-publish-article-id
  "drop recent published articles for randamness"
  [account-id]
  (let [articles (select articles
                  (where {:account_id account-id
                          :can_auto_publish true
                          })
                  (order :last_published_at :DESC))
        drop-num (if (= 1 (count articles)) 0 (mod (count articles) 2))
        ]
    (first (shuffle (map :id (drop drop-num articles))))
  ))

(defn publish-to-fb
  [article]
  (when (:can_publish_fb article)
    (fb/post-fb article))
  )

(defn publish-to-twitter
  [article]
  (when (:can_publish_twitter article)
    (tw/tweet article))
  )

(defn publish
  [schedule]
  (let [article (find-by-id
                  (:next_scheduled_article_id schedule))
       ]
    (when article
      (do
        (future (publish-to-fb article))
        (future (publish-to-twitter article)))
      )
  ))


