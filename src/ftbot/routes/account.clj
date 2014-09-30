(ns ftbot.routes.account
  (:use compojure.core)
  (:require [ftbot.views.templates :as template]
            [ftbot.views.accounts :as accounts-view]
            [ftbot.util :as util]
            [ftbot.models.accounts :as db]
            [ftbot.models.schedules :as db-sche]
            [ftbot.models.articles :as db-art]
            [ftbot.models.facebook :as fb]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [hiccup.page :as hp]
            [korma.db :as kdb]
            ))

(defn fb-redirect-url
  [app-id secret]
 (str (util/base-iri)
      "/accounts/fbtoken/"
              app-id "/"
              secret ))

(defn account-list-page []
  (template/base
    "アカウント一覧"
    (hp/include-js  "/js/controllers/accountCtrl.js")
    accounts-view/accounts-list-page))

(defn fb-token-page [code app-id secret]
  (template/base
    "FBトークン"
    nil
    (accounts-view/token-view
      (.getToken
        (fb/.getOAuthAccessToken (fb/gen-facebook-simple app-id secret)
                                 code
                                  (fb-redirect-url app-id secret)
                                 )))))

(defn account-view-list []
  accounts-view/accounts-list-view)

(defn account-entry-form []
  accounts-view/accounts-edit-form)

(defn load-accounts []
  (json/generate-string (db/find-all-accounts)))

(defn create-account [account-stream]
    (-> (json/parse-stream
          (io/reader account-stream)
          true)
        (db/sanitize)
        (db/register-accounts))
    {:status 200}
  )

(defn update-account [id account-stream]
    (-> (json/parse-stream
          (io/reader account-stream)
          true)
        (db/sanitize)
        (db/update-account (util/str->number id))
        )
    {:status 200}
  )

(defn delete-account [id ]
  (let [account-id (util/str->number id)]
  (try
    (kdb/transaction
      (db-sche/remove-all-schedules account-id)
      (db-art/remove-all-articles account-id)
      (db/remove-account account-id)
      )
    {:status 200}
    (catch Exception e
      {:status 400 :body {:errmsgs [(str e)]}}
      ))))


(defn fb-auth-url
  [data]
  (let [data (json/parse-stream (io/reader data) true)
        {:keys [account] } data
        app-id     (account :fb_app_id)
        secret     (account :fb_secret_id)
        ]
    (if-not (every? identity [app-id
                              secret])
      (assoc {}
             :status 400
             :body {:success false :errmsgs ["不正なデータが渡されました"]})

      (->
        (assoc {} :url (fb/oauth-url (fb/gen-facebook
                                       account)
                                      (fb-redirect-url app-id secret)
                                     ))
        (#(assoc {} :body %))
      )
  )))

(defn validate
  [data]
  (let [data (json/parse-stream (io/reader data) true)
        {:keys [orgAccount newAccount] } data
        org-fb-app-id     (orgAccount :fb_app_id)
        new-fb-app-id     (newAccount :fb_app_id)
        org-fb-page-id    (orgAccount :fb_page_id)
        new-fb-page-id    (newAccount :fb_page_id)
        org-tw-app-id     (orgAccount :twitter_username)
        org-tw-token       (orgAccount :twitter_token)
        new-tw-token       (newAccount :twitter_token)
        ]
    (if-not (every? identity [
        new-fb-app-id
        new-fb-page-id
        new-tw-token
                              ])
      (assoc {}
             :status 400
             :body {:success false :errmsgs ["不正なデータが渡されました"]})
      (->
        (map vector
             [ (and (not (= org-fb-app-id new-fb-app-id)) (db/fb-app-id-exists? new-fb-app-id))
              (and (not (= org-fb-page-id new-fb-page-id)) (db/fb-page-id-exists? new-fb-page-id))
              (and (not (= org-tw-token new-tw-token)) (db/twitter-token-exists? new-tw-token))]
             [ "同じfacebook App IDが既に登録されています"
                "同じfacebook Page IDが既に登録されています"
              "同じtwitter Tokenが既に登録されています"])
        ((fn [xs] (filter #(first %) xs)))
        ((fn [errs]
           (if (empty? errs)
             {:success true}
             (assoc {} :success false :errmsgs (map second errs))
             )))
        (#(assoc {} :body %))
        )
      )))

(defroutes account-routes
  (context "/accounts" []
           (POST "/getfbauthurl" {data :body} (fb-auth-url data))
           (GET "/fbtoken/:app-id/:secret"
                [code app-id secret] (fb-token-page code app-id secret))

           (GET "/twitter"
                [code app-id secret] (fb-token-page code app-id secret))
           (GET "/" [] (account-list-page))
           (GET "/listform" [] (account-view-list))
           (GET "/entryform" [] (account-entry-form))


           (GET "/all" {params :params}
                (util/as-json (load-accounts) (params :callback)))

           (GET "/:id" {{id :id} :params {callback :callback} :params}
                (if-let [account (db/find-by-id id)]
                  (util/as-json {:account account} callback)
                  {:status 400}
                  ))

           (POST "/validate" {data :body} (validate data))

           (POST "/create" {account :body} (create-account account))
           (POST "/:id/update" {{id :id} :params account :body} (update-account id account))
           (DELETE "/:id" [id]  (delete-account id))
           ))

