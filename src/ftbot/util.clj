(ns ftbot.util
  (:require [noir.io :as io]
            [noir.response :as response]
            [markdown.core :as md]))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (->>
    (io/slurp-resource filename)
    (md/md-to-html-string)))


(defn as-json [contents & callback]
  (if-not (or (empty? callback)  (apply empty? callback))
       (response/jsonp (first callback) contents)
       (response/json contents)
    ))

(defn str->number [s]
  (binding [*read-eval* false]
    (try
      (let [s (clojure.string/replace s #"^0+" "")
            s (if (empty? s) "0" s)
            n (read-string s)]
        (if (number? n) n nil))
      (catch Exception e (println e)))
    ))

(defn str->int [s]
  (if-let [n (str->number s)]
    (int n)
    nil
    ))

(defn now [] (new java.util.Date))

(defn excerpt
   [s len]
   (let [slen (.length s )]
     (if (<= slen len)
       s
       (str (subs s 0 len) "...")
     )))


(defn base-iri []
  (or (System/getenv "BASE_IRI") "http://localhost:3000"))

