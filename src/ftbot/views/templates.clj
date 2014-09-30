(ns ftbot.views.templates
  (:require [hiccup.page :as hp]))

(def global-nav
    [:div {:class  "navbar navbar-inverse navbar-fixed-top"}
      [:div {:class "container"}
         [:div {:class "navbar-header"}
            [:a {:class "brand" :href "/"} "ftbot"]]
         [:div {:class "navbar-collapse "}
          [:ul {:class "nav"}
           [:li [:a {:href "/articles"} "記事一覧"]]
           [:li [:a {:href "/accounts"} "FB/Twitterアカウント"]]
           [:li [:a {:href "/schedules"} "配信スケジュール"]]]]]])

(defn base
  [title & [inc-js contents]]
  (hp/html5 {:ng-app "ftbot" :lang "jp"}
    [:head
      [:title title]
      (hp/include-css "/css/bootstrap-theme.min.css")
      (hp/include-css "/css/bootstrap.min.css")
      (hp/include-css "/css/screen.css" )
      (hp/include-js "//code.jquery.com/jquery-2.0.3.min.js")
      (hp/include-js "/js/bootstrap.min.js")
      (hp/include-js "//code.angularjs.org/1.2.16/angular.min.js")
      (hp/include-js "//code.angularjs.org/1.2.16/angular-resource.js")
      (hp/include-js "//code.angularjs.org/1.2.16/angular-route.js")
      (hp/include-js "/js/modules/angular-file-upload.js")
      (hp/include-js "/js/services/storage.js")
      (hp/include-js "/js/directives/directives.js")
      inc-js
    ]
      [:body
        global-nav
        [:div {:class "container"} contents]]))

(defn gen-tags [tag & values]
  (map #(vector tag %) values))

(defn verify-password-input
  [{:keys [form-name model-name elem-name label placeholder equals-model-name]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}") }
        [:label label]
        [:input {:type "password" :name elem-name
                 :ng-model model-name :required ""
                 :equals (str "{{" equals-model-name "}}")
                 :placeholder  placeholder
                 }]
       [:span  {:ng-show (str elem ".$error.required")
                :class "help-inline"}
        "必須入力です "
       ]
   ]
  )
)


(defn password-input
  [{:keys [form-name model-name elem-name label placeholder ]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}") }
        [:label label]
        [:input {:type "password" :name elem-name
                 :ng-model model-name :required ""
                 :placeholder  placeholder
                 }]
       [:span  {:ng-show (str elem ".$error.required")
                :class "help-inline"}
        "必須入力です "
       ]
       [:span  {:ng-show
                (str elem ".$error.equals && !" elem ".$pristine" )
                :class "help-inline"}
        "パスワードが一致しません"
       ]
   ]
  )
)

(defn time-input
  [{:keys [form-name model-name elem-name label placeholder equals-model-name]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}") }
        [:label label]
        [:input {:type "text" :name elem-name
                 :ng-model model-name :required ""
                 :time ""
                 :placeholder  placeholder
                 }]
       [:span  {:ng-show (str elem ".$error.required")
                :class "help-inline"}
        "必須入力です"
       ]
       [:span  {:ng-show
                (str elem ".$error.format && !" elem ".$pristine" )
                :class "help-inline"}
        "時刻は00:00〜24:00の形式で入力してください。"
       ]
   ]
  )
)


(defn required-input
  [{:keys [form-name model-name elem-name label placeholder equals-model-name]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}") }
       [:label label]
       [:input {:type "text" :name elem-name
                :ng-model model-name :required ""
                :placeholder  placeholder
                }]
       [:span  {:ng-show (str elem ".$error.required")
                :class "help-inline"}
        "必須入力です "
       ]
      ]
  ))


(defn required-combo-box
  [{:keys [form-name model-name elem-name label equals-model-name option-model]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}") }
       [:label label]
       [:select {:ng-model model-name
                 :required "true"
                 :ng-init 0
                 :ng-options (str "x as x.value for x in " option-model)
                 }
       [:span  {:ng-show (str elem ".$error.required")
                :class "help-inline"}
        "必須入力です "
       ]
      ]
   ]
  ))

(defn input
  [{:keys [form-name model-name elem-name label placeholder equals-model-name ng-show]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}")
           :ng-show ng-show
           }
       [:label label]
       [:input {:type "text" :name elem-name
                :ng-model model-name
                :placeholder  placeholder
                :ng-show ng-show
                }]
      ]
  ))

(defn checkbox
  [{:keys [form-name model-name elem-name label placeholder equals-model-name]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}") }
       [:label label]
       [:input {:type "checkbox" :name elem-name
                :ng-model model-name
                :placeholder  placeholder
                }]
      ]
  ))


(defn textarea
  [{:keys [rows cols text form-name model-name elem-name
           label placeholder equals-model-name ng-show]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}")
           :ng-show ng-show }
       [:label label]
       [:textarea {:rows rows :cols cols
                   :name elem-name
                   :ng-model model-name
                   :placeholder  placeholder
                   :ng-show ng-show
                  } text]
      ]
  ))


(defn required-textarea
  [{:keys [rows cols text form-name model-name elem-name label placeholder equals-model-name]}]
  (let [elem (str form-name "." elem-name)]
    [:div {:class "control-group"
           :ng-class (str "{error:" elem ".$invalid}") }
       [:label label]
       [:textarea {:rows rows :cols cols
                   :name elem-name
                   :ng-model model-name :required ""
                   :placeholder  placeholder
                  } text]
       [:span  {:ng-show (str elem ".$error.required")
                :class "help-inline"}
        "必須入力です "
       ]
      ]
  ))
