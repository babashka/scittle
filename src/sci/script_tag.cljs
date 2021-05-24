(ns sci.script-tag
  (:refer-clojure :exclude [defn])
  (:require [clojure.core :as c]
            [clojure.string :as str]
            [goog.object :as gobject]
            [goog.string]
            [sci.core :as sci]))

(c/defmacro defn [fn-name & args]
  (let [ns-sym (gensym "ns")]
    `(let [~ns-sym (ns-name *ns*)]
       (clojure.core/defn ~fn-name ~@args)
       ~(when (:export (meta fn-name))
          `(sci.script-tag/-export ~fn-name (str ~ns-sym "." '~fn-name))))))

(c/defn -export [f k]
  (let [k (munge k)
        parts (str/split k #"\.")]
    (loop [parts parts
           prev js/window]
      (let [fpart (first parts)]
        (cond (= "user" fpart)
              (recur (rest parts) prev)
              (= 1 (count parts))
              (gobject/set prev fpart f)
              :else
              (if-let [obj (gobject/get prev fpart)]
                (recur (rest parts) obj)
                (let [obj #js {}]
                  (gobject/set prev fpart obj)
                  (recur (rest parts)
                         obj))))))
    (gobject/set js/window k f)))

(def stns (sci/create-ns 'sci.script-tag nil))

(def namespaces
  {'sci.script-tag
   {'defn (sci/copy-var defn stns)
    '-export (sci/copy-var -export stns)}
   'clojure.core {'println (sci/copy-var println stns)
                  'prn     (sci/copy-var prn stns)}})

(def ctx (atom (sci/init {:namespaces namespaces
                          :classes {'js js/window
                                    :allow :all}})))

(c/defn eval-string [s]
  (sci/eval-string* @ctx
                    (str "(require '[sci.script-tag :refer :all])"
                         s)))

(c/defn register-plugin! [plug-in-name sci-opts]
  plug-in-name ;; unused for now
  (swap! ctx sci/merge-opts sci-opts))

(c/defn- load-contents [script-tags]
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
