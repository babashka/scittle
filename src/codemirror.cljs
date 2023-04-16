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

(defn parse-char [level pos]
  (case pos
    \( (inc level)
    \) (dec level)
    level))

(defn form-at-cursor
  "Takes the string of characters before cursor pos."
  [s]
  (let [run (rest (reductions parse-char 0 s))]
    (->> s
         (take (inc (count (take-while #(not= 0 %) run))))
         reverse
         (apply str))))

(defn eval-at-cursor [viewer]
  (let [cursor-pos (some-> cm .-state .-selection .-main .-head)
        code (some-> cm .-state .-doc str)]
    (let [region (form-at-cursor (reverse (take cursor-pos code)))
          region (if (nil? region) nil (eval-string region))]
      (if (nil? region) nil (reset! last-result region)))
    (update-editor! (str (subs code 0 cursor-pos)
                         (when-not (= "" (:result @last-result)) " => ")
                         (:result @last-result)
                         (reset! eval-tail (subs code cursor-pos (count code))))
                    cursor-pos)
    (.dispatch cm #js{:selection #js{:anchor cursor-pos :head   cursor-pos}}))
  true)

(defn code-str [s]
  (str (rest (read-string (str "(do " s ")")))))

(defn code-seq [s]
  (map str (rest (read-string (str "(do " s ")")))))

;; but we really want to find the center points, not the start points.

(defn find-center [[start s]]
  [(+ start (int (/ (count s) 2))) s])

;; then just pick the one with the closest center point to the cursor,
;; and evaluate it!

(defn abs [v]
  (if (neg? v) (- v) v))

(defn top-level [s pos]
  (first (nfirst
          (sort-by #(abs (- pos (first %)))
                   (map find-center
                        (map vector
                             (map #(str/last-index-of (code-str s) %) (code-seq s))
                             (code-seq s)))))))

(defn eval-top-level [viewer]
  (let [code (some-> cm .-state .-doc str)
        cursor-pos (some-> cm .-state .-selection .-main .-head)
        result (reset! last-result (eval-string (top-level code cursor-pos)))]
    (update-editor! (str (subs code 0 cursor-pos)
                         (when-not (= "" (:result @last-result)) " => ")
                         (:result result)
                         (subs code cursor-pos))
                    cursor-pos))
  true)

(defn eval-cell [viewer]
  (let [code (some-> cm .-state .-doc str)]
    (reset! last-result (eval-string (str "(do " (.-doc (.-state viewer)) " )")))
    (update-editor! (str code
                         (when-not (= "" (:result @last-result)) " => ")
                         (:result @last-result))
                    (count code)))
  true)

(defn clear-eval []
  (let [code       (some-> cm .-state .-doc str)
        cursor-pos (some-> cm .-state .-selection .-main .-head)
        result     @last-result
        splits     (str/split code #" => ")]
    (when (not= "" @last-result)
      (update-editor! (str (first splits) (subs (last splits) (count (str (:result result)))))
                      cursor-pos)
      (reset! last-result "")
      (reset! eval-tail ""))))

(def extension
  (.of js/cv.keymap
       (clj->js  [{:key (str "Alt-Enter")
                   :run #(eval-cell %)}
                  {:key "Mod-Enter"
                   :run #(eval-top-level %)}
                  {:key "Shift-Enter"
                   :run #(eval-at-cursor %)}
                  {:key "Escape"
                   :run clear-eval}
                  {:key "ArrowLeft"
                   :run clear-eval}
                  {:key "ArrowRight"
                   :run clear-eval}
                  {:key "ArrowUp"
                   :run clear-eval}
                  {:key "ArrowDown"
                   :run clear-eval}])))

(def cm
  (let [doc "(def n 7)

(defn r []
  (map inc (range n)))"]
    (js/cm.EditorView. #js {:doc doc
                            :extensions #js [js/cm.basicSetup, (js/lc.clojure), (.highest js/cs.Prec extension)]
                            :parent (js/document.querySelector "#app")})))

(set! (.-cm_instance js/globalThis) cm)
