(ns scittle.nrepl
  (:require
   [clojure.edn :as edn]
   [sci.nrepl.completions :refer [completions]]
   [sci.nrepl.info :refer [info]]
   [scittle.core :refer [!last-ns eval-string !sci-ctx]]))

(defn nrepl-websocket []
  (.-ws_nrepl js/window))

(defn nrepl-reply [{:keys [id session]} {:keys [ns] :as payload}]
  (.send (nrepl-websocket)
         (str
          (let [ns (or ns (str @!last-ns))]
            (assoc payload :id id :session session :ns ns)))))

(defn handle-nrepl-eval [{:keys [code] :as msg}]
  (let [[kind val] (try [::success (eval-string code)]
                        (catch :default e
                          [::error (str e)]))]
    (case kind
      ::success
      (do (nrepl-reply msg {:value (pr-str val)})
          (nrepl-reply msg {:status ["done"]}))
      ::error
      (do
        (nrepl-reply msg {:err (pr-str val)})
        (nrepl-reply msg {:ex (pr-str val)
                          :status ["error" "done"]})))))

(defn handle-nrepl-info [msg]
  (let [info (info (assoc msg :ctx @!sci-ctx))]
    (nrepl-reply msg info)))

(declare ops)

(defn
  handle-describe
  [msg]
  (nrepl-reply
   msg
   {:versions {"scittle-nrepl" {"major" "0"
                                "minor" "0"
                                "incremental" "1"}}
    :ops (zipmap
          (map
           name
           (concat
            (keys ops)
            ;; sci.nrepl browser_server.clj handles:
            #{:clone :load-file}
            ;; we are lying about close?
            #{"close"}))
          (repeat {}))
    :status ["done"]}))

(def ops
  "Operations supported by the nrepl server."
  {:eval handle-nrepl-eval
   :info handle-nrepl-info
   :eldoc handle-nrepl-info
   :lookup handle-nrepl-info
   :describe handle-describe
   :complete (fn [msg] (let [completions (completions (assoc msg :ctx @!sci-ctx))]
                         (nrepl-reply msg completions)))})

(defn handle-nrepl-message [msg]
  (if-let [handler (ops (:op msg))]
    (handler msg)
    (nrepl-reply (merge msg {:status ["error" "done"] :err "unkown-op"}) (assoc msg :ctx @!sci-ctx))))

(defn ws-url [host port path]
  (str "ws://" host ":" port "/" path))

(when-let [ws-port (.-SCITTLE_NREPL_WEBSOCKET_PORT js/window)]
  (set! (.-ws_nrepl js/window)
        (new js/WebSocket (ws-url (.-hostname (.-location js/window)) ws-port "_nrepl"))))

(when-let [ws (nrepl-websocket)]
  (prn :ws ws)
  (set! (.-onmessage ws)
        (fn [event]
          (handle-nrepl-message (edn/read-string (.-data event)))))
  (set! (.-onerror ws)
        (fn [event]
          (js/console.log event))))
