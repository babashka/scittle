(ns scittle.core
  (:refer-clojure :exclude [time])
  (:require [goog.object :as gobject]
            [goog.string]
            [sci.core :as sci]))

(clojure.core/defmacro time
  "Evaluates expr and prints the time it took. Returns the value of expr."
  [expr]
  `(let [start# (cljs.core/system-time)
         ret# ~expr]
     (prn (cljs.core/str "Elapsed time: "
                         (.toFixed (- (system-time) start#) 6)
                         " msecs"))
     ret#))

(def stns (sci/create-ns 'sci.script-tag nil))
(def cljns (sci/create-ns 'clojure.core nil))

(def namespaces
  {'clojure.core
   {'println     (sci/copy-var println cljns)
    'prn         (sci/copy-var prn cljns)
    'system-time (sci/copy-var system-time cljns)
    'time        (sci/copy-var time cljns)}})

(def ctx (atom (sci/init {:namespaces namespaces
                          :classes {'js js/window
                                    :allow :all}})))

(defn ^:export eval-string [s]
  (sci/eval-string* @ctx s))

(defn register-plugin! [plug-in-name sci-opts]
  plug-in-name ;; unused for now
  (swap! ctx sci/merge-opts sci-opts))

(defn load-contents [script-tags]
  (when-let [tag (first script-tags)]
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
   (let [script-tags (js/document.querySelectorAll "script[type='application/x-scittle']")]
     (load-contents script-tags))), false)
