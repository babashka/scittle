(require '[clojure.string :as str])

(declare cm)

(defn eval-string [s]
  (when-some [code (not-empty (str/trim s))]
    (try {:result (js/scittle.core.eval_string code)}
         (catch js/Error e
           {:error (str (.-message e))}))))

(defonce last-result (atom ""))
(defonce eval-tail (atom nil))

(defn update-editor! [text cursor-pos]
  (let [end (count (some-> cm .-state .-doc str))]
    (.dispatch cm #js{:changes #js{:from 0 :to end :insert text}
                            :selection #js{:anchor cursor-pos :head cursor-pos}})))

(defn eval-at-cursor [viewer]
  (let [cursor-pos (some-> cm .-state .-selection .-main .-head)
        code (first (str/split (str (some-> cm .-state .-doc str)) #" => "))]
    (let [region (str "(do " (.-doc (.-state viewer)) " )")
          region (if (nil? region) nil (eval-string region))]
      (if (nil? region) nil (reset! last-result region)))
    (update-editor! (str (subs code 0 cursor-pos)
                         (when-not (= "" (:result @last-result)) " => ")
                         (:result @last-result)
                         (reset! eval-tail (subs code cursor-pos (count code))))
                    cursor-pos)
    (.dispatch cm
               #js{:selection #js{:anchor cursor-pos
                                  :head   cursor-pos}})))

(defn eval-top-level [viewer]
  (let [region (str "(do " (.-doc (.-state viewer)) " )")
        region (if (nil? region) nil (eval-string region))]
    (if (nil? region) nil (reset! last-result region)))
  true)

(defn eval-cell [viewer]
  (reset! last-result (eval-string (str "(do " (.-doc (.-state viewer)) " )")))
  true)

(defn clear-eval []
  (when (not= "" @last-result)
    (reset! last-result "")
    (let [code (-> cm
                   (some-> .-state .-doc str)
                   str
                   (str/split #" => ")
                   first
                   (str @eval-tail))
          cursor-pos (some-> cm .-state .-selection .-main .-head)]
      (update-editor! code (min cursor-pos (count code))))))

(def extension
  (.of js/cv.keymap
       (clj->js  [{:key (str "Alt-Enter")
                   :run #(eval-cell %)}
                  {:key  "Mod-Enter"
                   :run #(eval-top-level %)}
                  {:key "Shift-Enter"
                   :run #(eval-at-cursor %)}
                  {:key "Escape" :run clear-eval}
                  {:key "ArrowLeft" :run clear-eval}
                  {:key "ArrowRight" :run clear-eval}])))

(def cm
  (let [doc (str/trim "
(map inc (range 8))
")]
    (js/cm.EditorView. #js {:doc doc
                            :extensions #js [js/cm.basicSetup, (js/lc.clojure), (.highest js/cs.Prec extension)]
                            :parent (js/document.querySelector "#app")})))

(set! (.-cm_instance js/globalThis) cm)
