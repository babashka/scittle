(ns scittle.nrepl
  (:require
   [clojure.edn :as edn]
   [sci.nrepl.server :as nrepl-server]))

(defn ws-url [host port path]
  (str "ws://" host ":" port "/" path))

(defn- handle-eval [{:keys [code] :as msg}]
  (let [[kind val] (try [::success (nrepl-server/eval-string code)]
                        (catch :default e
                          [::error (str e)]))]
    (case kind
      ::success
      (if (instance? js/Promise val)
        (-> val
            (.then (fn [resolved]
                     (nrepl-server/nrepl-reply msg {:value (str "#<Promise " (pr-str resolved) ">")})
                     (nrepl-server/nrepl-reply msg {:status ["done"]})))
            (.catch (fn [rejected]
                      (nrepl-server/nrepl-reply msg {:err (str rejected)})
                      (nrepl-server/nrepl-reply msg {:ex (str rejected)
                                                      :status ["error" "done"]}))))
        (do (nrepl-server/nrepl-reply msg {:value (pr-str val)})
            (nrepl-server/nrepl-reply msg {:status ["done"]})))
      ::error
      (do
        (nrepl-server/nrepl-reply msg {:err (pr-str val)})
        (nrepl-server/nrepl-reply msg {:ex (pr-str val)
                                        :status ["error" "done"]})))))

(defn- handle-message [msg]
  (if (= :eval (:op msg))
    (handle-eval msg)
    (nrepl-server/handle-nrepl-message msg)))

(when-let [ws-port (.-SCITTLE_NREPL_WEBSOCKET_PORT js/window)]
  (set! (.-ws_nrepl js/window)
        (new js/WebSocket (ws-url (or (.-SCITTLE_NREPL_WEBSOCKET_HOST js/window)
                                      (.-hostname (.-location js/window)))
                                  ws-port "_nrepl"))))

(when-let [ws (nrepl-server/nrepl-websocket)]
  (set! (.-onmessage ws)
        (fn [event]
          (handle-message (edn/read-string (.-data event)))))
  (set! (.-onerror ws)
        (fn [event]
          (js/console.log event))))
