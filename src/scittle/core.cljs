(ns scittle.core
  (:refer-clojure :exclude [time])
  (:require [cljs.reader :refer [read-string]]
            [goog.object :as gobject]
            [goog.string]
            [sci.core :as sci]
            [scittle.impl.common :refer [cljns]]
            [scittle.impl.error :as error]))

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
(def rns (sci/create-ns 'cljs.reader nil))

(def namespaces
  {'clojure.core
   {'time (sci/copy-var time cljns)
    'system-time (sci/copy-var system-time cljns)
    'random-uuid random-uuid
    'read-string (sci/copy-var read-string rns)}
   'goog.object {'set gobject/set
                 'get gobject/get}})

(def !sci-ctx (atom (sci/init {:namespaces namespaces
                          :classes {'js js/window
                                    :allow :all}
                          :disable-arity-checks true})))


(def !last-ns (volatile! @sci/ns))

(defn- -eval-string [s]
  (sci/binding [sci/ns @!last-ns]
    (let [rdr (sci/reader s)]
      (loop [res nil]
        (let [form (sci/parse-next @!sci-ctx rdr)]
          (if (= :sci.core/eof form)
            (do
              (vreset! !last-ns @sci/ns)
              res)
            (recur (sci/eval-form @!sci-ctx form))))))))

(defn ^:export eval-string [s]
  (try (-eval-string s)
       (catch :default e
         (error/error-handler e (:src @!sci-ctx))
         (let [sci-error? (isa? (:type (ex-data e)) :sci/error)]
           (throw (if sci-error?
                    (or (ex-cause e) e)
                    e))))))

(defn register-plugin! [plug-in-name sci-opts]
  plug-in-name ;; unused for now
  (swap! !sci-ctx sci/merge-opts sci-opts))

(defn- eval-script-tags* [script-tags]
  (when-let [tag (first script-tags)]
    (if-let [text (not-empty (gobject/get tag "textContent"))]
      (let [scittle-id (str (gensym "scittle-tag-"))]
        (gobject/set tag "scittle_id" scittle-id)
        (swap! !sci-ctx assoc-in [:src scittle-id] text)
        (sci/binding [sci/file scittle-id]
          (eval-string text))
        (eval-script-tags* (rest script-tags)))
      (let [src (.getAttribute tag "src")
            req (js/XMLHttpRequest.)
            _ (.open req "GET" src true)
            _ (gobject/set req "onload"
                           (fn [] (this-as this
                                    (let [response (gobject/get this "response")]
                                      (gobject/set tag "scittle_id" src)
                                      ;; save source for error messages
                                      (swap! !sci-ctx assoc-in [:src src] response)
                                      (sci/binding [sci/file src]
                                        (eval-string response)))
                                    (eval-script-tags* (rest script-tags)))))]
        (.send req)))))

(defn ^:export eval-script-tags []
  (let [script-tags (js/document.querySelectorAll "script[type='application/x-scittle']")]
    (eval-script-tags* script-tags)))

(def auto-load-disabled? (volatile! false))

(defn ^:export disable-auto-eval
  "By default, scittle evaluates script nodes on the DOMContentLoaded
  event using the eval-script-tags function. This function disables
  that behavior."
  []
  (vreset! auto-load-disabled? true))

(js/document.addEventListener
 "DOMContentLoaded"
 (fn [] (when-not @auto-load-disabled? (eval-script-tags))), false)

(enable-console-print!)
(sci/alter-var-root sci/print-fn (constantly *print-fn*))

