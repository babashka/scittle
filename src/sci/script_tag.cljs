(ns sci.script-tag
  (:require [clojure.string :as str]
            [goog.object :as gobject]
            [goog.string]
            [sci.core :as sci]))

(defn kebab->camel [s]
  (str/replace s #"-[a-zA-Z0-9]"
               (fn [s]
                 (str/upper-case (.charAt s 1)))))

(defn defn-macro [_ _ fn-name & args]
  `(let [ns# (ns-name *ns*)]
     (clojure.core/defn ~fn-name ~@args)
     (println (str ns# "." '~fn-name))
     (sci.script-tag/-export ~fn-name (str ns# "." '~fn-name))))

(def ctx (sci/init {:namespaces {'sci.script-tag
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
                                                     (do (gobject/set prev fpart #js {})
                                                         (recur (rest parts)
                                                                (gobject/get prev fpart))))))
                                               (gobject/set js/window k f)))}
                                 'clojure.core {'println println}}
                    :classes {'js js/window
                              :allow :all}}))

(defn eval-string [s]
  (sci/eval-string* ctx
                    (str "(require '[sci.script-tag :refer :all])"
                         s)))

(js/document.addEventListener
 "DOMContentLoaded"
 (fn []
   (let [script-tags (js/document.querySelectorAll "script[type='application/x-sci']")]
     (run! (fn [script-tag]
             (let [text (gobject/get script-tag "textContent")]
               (eval-string text))) script-tags))), false)
