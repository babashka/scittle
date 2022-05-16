(ns scittle.nrepl
  (:require
   [clojure.edn :as edn]
   [sci.nrepl.completions :refer [completions]]
   [scittle.core :refer [!last-ns eval-string !sci-ctx]]))

(defn nrepl-websocket []
  (.-ws_nrepl js/window))

(defn nrepl-reply [{:keys [id session]} payload]
  (.send (nrepl-websocket)
         (str (assoc payload :id id :session session :ns (str @!last-ns)))))

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

(defn handle-nrepl-message [msg]
  (case (:op msg)
    :eval (handle-nrepl-eval msg)
    :complete (let [completions (completions (assoc msg :ctx @!sci-ctx))]
                (nrepl-reply msg completions))))

(when (.-SCITTLE_BROWSER_REPL_PROXY_PORT js/window)
  (set! (.-ws_nrepl js/window)
        (new js/WebSocket "ws://localhost:1340/_nrepl")))

(when-let [ws (nrepl-websocket)]
  (prn :ws ws)
  (set! (.-onmessage ws)
        (fn [event]
          (handle-nrepl-message (edn/read-string (.-data event)))))
  (set! (.-onerror ws)
        (fn [event]
          (js/console.log event))))
