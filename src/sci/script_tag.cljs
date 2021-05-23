(ns sci.script-tag
  (:require [clojure.string :as str]
            [goog.object :as gobject]
            [goog.string]
            [sci.core :as sci]))

(defn kebab->camel [s]
  (str/replace s #"-[a-zA-Z0-9]"
               (fn [s]
                 (str/upper-case (.charAt s 1)))))

(defn- defn-macro [_ _ fn-name & args]
  `(let [ns# (ns-name *ns*)]
     (clojure.core/defn ~fn-name ~@args)
     (sci.script-tag/-export ~fn-name (str ns# "." '~fn-name))))

(def ctx (atom (sci/init {:namespaces {'sci.script-tag
                                       {'defn (with-meta defn-macro
                                                {:sci/macro true})
                                        '-export (fn [f k]
                                                   (let [parts (str/split k #"\.")]
                                                     (loop [parts parts
                                                            prev js/window]
                                                       (let [fpart (first parts)
                                                             fpart (kebab->camel fpart)]
                                                         (if (= 1 (count parts))
                                                           (gobject/set prev fpart f)
                                                           (if-let [obj (gobject/get prev fpart)]
                                                             (recur (rest parts) obj)
                                                             (let [obj #js {}]
                                                               (gobject/set prev fpart obj)
                                                               (recur (rest parts)
                                                                      obj))))))
                                                     (gobject/set js/window k f)))}
                                       'clojure.core {'println println}}
                          :classes {'js js/window
                                    :allow :all}})))

(defn eval-string [s]
  (sci/eval-string* @ctx
                    (str "(require '[sci.script-tag :refer :all])"
                         s)))

(defn merge-ctx [opts]
  (swap! ctx sci/merge-opts opts))

(defn- load-contents [script-tags]
  (when-first [tag script-tags]
    (if-let [text (not-empty (gobject/get tag "textContent"))]
      (do (eval-string text)
          (load-contents (rest script-tags)))
      (let [src (.getAttribute tag "src")
            req (js/XMLHttpRequest.)
            _ (.open req "GET" src true)
            _ (gobject/set req "onload"
                           (fn [] (this-as this
                                    (let [response (gobject/get this "response")]
                                      (eval-string response))
                                    (load-contents (rest script-tags)))))]
        (.send req)))))

(js/document.addEventListener
 "DOMContentLoaded"
 (fn []
   (let [script-tags (js/document.querySelectorAll "script[type='application/x-sci']")]
     (load-contents script-tags))), false)
