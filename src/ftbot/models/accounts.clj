(ns ftbot.models.accounts
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [ftbot.models.schema :as schema]
            [ftbot.util :as util])
  )

(defdb db schema/db-spec)

(defentity accounts)

(defn sanitize [maybe-dirty-account]
    (select-keys
        maybe-dirty-account
        [:id
        :account_name
        :fb_app_id
        :fb_secret_id
        :fb_page_id
        :fb_token
        :twitter_api_key
        :twitter_api_secret
        :twitter_token
        :twitter_token_secret
        ]
))

(defn find-all-accounts []
  (select accounts
      (order :id)))

(defn find-by-id [id]
  (first (select accounts (where {:id id}))))

(defn find-by [condition]
  (select accounts
      (where condition)))

(defn register-accounts
  [account]
  (insert accounts
      (values
        (assoc account
          :updated_at (util/now)
          :created_at (util/now))
        )))

(defn update-account
  [account id]
  (update accounts
      (set-fields
        (assoc account :updated_at (util/now)))
      (where {:id id})))

(defn remove-account
  [id]
  (delete accounts
    (where {:id id})))

(defn fb-app-id-exists? [fb-app-id]
  (if (seq (find-by {:fb_app_id fb-app-id}))
      true
      false))

(defn fb-page-id-exists? [fb-page-id]
  (if (seq (find-by {:fb_page_id fb-page-id}))
      true
      false))

(defn twitter-token-exists? [token]
  (if (seq (find-by {:twitter_token token}))
      true
      false)
  )


