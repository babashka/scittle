(ns scittle.nrepl
  (:require
   [clojure.edn :as edn]
   [sci.nrepl.server :as nrepl-server]))

(defn ws-url [host port path]
  (str "ws://" host ":" port "/" path))

(when-let [ws-port (.-SCITTLE_NREPL_WEBSOCKET_PORT js/window)]
  (set! (.-ws_nrepl js/window)
        (new js/WebSocket (ws-url (.-hostname (.-location js/window)) ws-port "_nrepl"))))

(when-let [ws (nrepl-server/nrepl-websocket)]
  (set! (.-onmessage ws)
        (fn [event]
          (nrepl-server/handle-nrepl-message (edn/read-string (.-data event)))))
  (set! (.-onerror ws)
        (fn [event]
          (js/console.log event))))
