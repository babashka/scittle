(ns sci.script-tag
  (:refer-clojure :exclude [defn time])
  (:require [clojure.core :as c]
            [clojure.string :as str]
            [goog.object :as gobject]
            [goog.string]
            [sci.core :as sci]))

(c/defmacro time
  "Evaluates expr and prints the time it took. Returns the value of expr."
  [expr]
  `(let [start# (cljs.core/system-time)
         ret# ~expr]
     (prn (cljs.core/str "Elapsed time: "
                         (.toFixed (- (system-time) start#) 6)
                         " msecs"))
     ret#))

(c/defn export [k f]
  (let [k (munge k)
        parts (str/split k #"\.")]
    (loop [parts parts
           prev js/window]
      (when-first [fpart parts]
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
                         obj))))))))

(def stns (sci/create-ns 'sci.script-tag nil))
(def cljns (sci/create-ns 'clojure.core nil))

(def namespaces
  {'sci.script-tag
   {'export (sci/copy-var export stns)}
   'clojure.core
   {'println     (sci/copy-var println cljns)
    'prn         (sci/copy-var prn cljns)
    'system-time (sci/copy-var system-time cljns)
    'time        (sci/copy-var time cljns)}})

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

(def built-in (-> (eval-string "(map ns-name (all-ns))")
                  set
                  (disj 'user)))

(c/defn- load-contents [script-tags]
  (if-let [tag (first script-tags)]
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
        (.send req)))
    (eval-string (str/replace"
(run! (fn [ns]
        (let [nsn (ns-name ns)]
          (when-not (contains? '%s nsn)
            (run! (fn [var]
                    (let [m (meta var)]
                      (when (:export m)
                        (sci.script-tag/export (str nsn  \".\" (:name m)) @var))))
          (vals (ns-publics ns))))))
  (all-ns))" "%s" built-in))))

(js/document.addEventListener
 "DOMContentLoaded"
 (fn []
   (let [script-tags (js/document.querySelectorAll "script[type='application/x-sci']")]
     (load-contents script-tags))), false)
