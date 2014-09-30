(ns ftbot.models.twitter
  (:require [ftbot.util :as util]
            [ftbot.models.accounts :as acc-db]
            [clojure.java.io :as io]
            )
  (:import (twitter4j Twitter TwitterFactory StatusUpdate)
            (twitter4j.auth AccessToken)
            (java.net URL)
           )
  )

(defn make-tweet [article account]
  (let [body  (article :body)
        tweet (StatusUpdate.  (str body))
        thumbnail (:thumbnail_url article)
        ]

    (println "==================== ")
    (println (empty? thumbnail))
    (if (empty? thumbnail)
      tweet
     (.media tweet (or (:name article) "")
             (io/input-stream
               (URL. thumbnail)))
      )
))

(defn tweet
  [article]
  (let [account (acc-db/find-by-id (:account_id article))
        [token secret] (map account
                              [:twitter_token
                               :twitter_token_secret
                               ])
        ]
      (doto (.getInstance (TwitterFactory.))
        (.setOAuthConsumer (account :twitter_api_key)
                           (account :twitter_api_secret))
        (.setOAuthAccessToken (AccessToken. token secret))
        (.updateStatus (make-tweet article account))
    ))
  )

