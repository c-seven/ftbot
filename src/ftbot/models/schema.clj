(ns ftbot.models.schema
  (:require [clojure.java.jdbc :as sql]
            [noir.io :as io]
            [black.water.korma :refer [decorate-korma!]]
            ))

(def db-store-name "site.db")
(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :subname (str (io/resource-path) db-store-name)
              :user "sa"
              :password ""
              :make-pool? true
              :naming {:keys clojure.string/lower-case
                       :fields clojure.string/upper-case}})

(defn  initialized?
  []
  (.exists (new java.io.File (str (io/resource-path) db-store-name ".h2.db"))))

(defn create-accounts
  []
  (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :accounts
      [:id "integer primary key auto_increment not null"]
      [:account_name "varchar(1000)"]
      [:fb_app_id "varchar(1000) "]
      [:fb_secret_id "varchar(1000) "]
      [:fb_page_id "varchar(1000) "]
      [:fb_token "varchar(1000) "]
      [:twitter_api_key "varchar(1000)"]
      [:twitter_api_secret "varchar(1000)"]
      [:twitter_token "varchar(1000)"]
      [:twitter_token_secret "varchar(1000)"]
      [:updated_at :timestamp]
      [:created_at :timestamp]
      ))
      (sql/db-do-prepared
        db-spec
        "alter table accounts add constraint uniq_fbapp unique(FB_APP_ID) ")

      (sql/db-do-prepared
        db-spec
        "alter table accounts add constraint uniq_twitter unique(TWITTER_TOKEN)")
  )

(defn create-articles
  []
    (sql/db-do-commands db-spec
    (sql/create-table-ddl
      :articles
      [:id "integer primary key auto_increment not null"]
      [:body "varchar(1000) not null"]
      [:thumbnail_url "varchar(400)"]
      [:name "varchar(1000) "]
      [:caption "varchar(1000) "]
      [:description "varchar(1000) "]
      [:is_image_post "boolean default false"]
      [:can_publish_fb "boolean default true"]
      [:can_publish_twitter "boolean default true"]
      [:can_auto_publish "boolean default true"]
      [:account_id "integer references accounts (id)"]
      [:last_published_at :timestamp]
      [:updated_at :timestamp]
      [:created_at :timestamp]))
      (sql/db-do-prepared
        db-spec
        "create index articles_updated_idx on articles (updated_at)")

      (sql/db-do-prepared
        db-spec
        "create index articles_publish_idx on articles (can_auto_publish)"))

(defn create-publish-schedules
  []
    (sql/db-do-commands
        db-spec
    (sql/create-table-ddl
      :publish_schedules
      [:id "integer primary key auto_increment not null"]
      [:account_id "integer references accounts (id)"]
      [:schedule_span "varchar(10) not null"] ;; d, w_mon, w_tue etc
      [:schedule_time "varchar(4) not null"] ;; 1200 2400 etc
      [:available  "boolean default true"]
      [:next_schedule_time :timestamp]
      [:next_scheduled_article_id "integer" ]
      [:updated_at :timestamp]
      [:created_at :timestamp]))

      (sql/db-do-prepared
        db-spec
        "create index pub_sche_available on publish_schedules (available)"
      ))

(defn create-tables []
   ( create-accounts )
   ( create-articles )
   ( create-publish-schedules )
)

(defn  delete-schema
  []
  (.delete (new java.io.File (str (io/resource-path) db-store-name ".h2.db")))
  (.delete (new java.io.File (str (io/resource-path) db-store-name ".trace.db"))
           ))

(defn regen-schema []
  (delete-schema)
  (create-tables)
  )
