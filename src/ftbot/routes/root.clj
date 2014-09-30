(ns ftbot.routes.root
  (:use compojure.core)
  (:require [ftbot.views.templates :as template]
            [ftbot.util :as util]))


(defn top []
  (template/base "トップ"
                 nil
             [:div
               [:h1 "Facebook/Twitterボット"]
               [:span "アカウント登録後 記事登録 スケジュール登録を行ってください"]]

                 ))

(defroutes root-routes
  (GET "/" [] (top)))

